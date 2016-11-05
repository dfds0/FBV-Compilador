package AST.Visitor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import AST.*;
import Semantic.*;;

public class VerifyTypeVisitor implements Visitor {
	private TypeVisitor declaredTypes;

	private Node previousNode;
	private Node currentNode;
	private NodeType rValueType;
	private String lastId;

	private int returnValue;

	public VerifyTypeVisitor(TypeVisitor declaredTypes) {
		super();
		this.declaredTypes = declaredTypes;
		this.lastId = "";
		returnValue = 0;
	}

	public int getReturnValue() {
		return returnValue;
	}

	public void visit(Program program) {
		program.classDeclList.accept(this);
	}

	// -----------------------------------------------------------------
	//  Structural operation 
	// -----------------------------------------------------------------
	public void visit(ClassDeclSimple classDeclSimple) {

		String className = classDeclSimple.identifier.name;

		Map<String, ClassNode> classes = this.declaredTypes.getClasses();
		ClassNode classNode = (ClassNode) classes.get(className);

		if(classNode == null) {
			errorUnknownSymbol(className, classes);
			return;
		}

		errorUnknownType(className, classNode);      

		classDeclSimple.varDeclList.accept(this);
		this.currentNode = classNode;
		classDeclSimple.methodDeclList.accept(this);
		this.currentNode = classNode;
	}

	public void visit(ClassDeclExtends classDeclExtends) {

		String className = classDeclExtends.identifier1.name;

		Map<String, ClassNode> classes = this.declaredTypes.getClasses();
		ClassWithParentNode classWithParentNode = (ClassWithParentNode) classes.get(className);

		if(classWithParentNode == null) {
			errorUnknownSymbol(className, classes);
			return;
		}

		errorUnknownType(className, classWithParentNode);

		classDeclExtends.varDeclList.accept(this);
		this.currentNode = classWithParentNode;
		classDeclExtends.methodDeclList.accept(this);
		this.currentNode = classWithParentNode;
	}


	public void visit(MethodDecl methodDecl) {

		String className = methodDecl.identifier.name;

		ClassNode currentNode = (ClassNode) this.currentNode;
		MethodNode declMethod = (MethodNode) currentNode.getMembers().get(className);

		if(declMethod == null) {
			returnValue = 1;
			System.out.print("[METHOD ERROR]: \tline " + methodDecl.getLine());
			System.out.println(": " + className + " not found!");
			return;
		}

		NodeType expecRetType = declMethod.getReturnType().getType();
		NodeType foundRetType = nodeTypeOf(methodDecl.type);
		if(!expecRetType.equals(foundRetType)) {
			returnValue = 1;
			System.out.print("[METHOD ERROR]: \tline" + methodDecl.getLine());
			System.out.print(": " + methodDecl.identifier + " returnType mismatch, ");
			System.out.print("Exected type: " + expecRetType);
			System.out.println(" Found type: " + foundRetType);
		}

		List<Node> params = declMethod.getParameters();
		FormalList formals = methodDecl.formalList;
		for(int i = 0; i < formals.size(); i++) {
			Formal formal = formals.elementAt(i);
			NodeType expecParamType = params.get(i).getType();
			NodeType foundParamType = nodeTypeOf(formal.type);

			if(!expecParamType.equals(foundParamType)) {
				returnValue = 1;
				System.out.print("[METHOD ERROR]:\tline " + methodDecl.getLine());
				System.out.print(": Exected param type=" + expecParamType);
				System.out.println(" Found param type=" + foundParamType);
			}
		}

		this.previousNode = currentNode;
		this.currentNode = declMethod;

		methodDecl.varDeclList.accept(this);
		methodDecl.statementList.accept(this);
		methodDecl.exp.accept(this);

		if (!declMethod.getReturnType().getType().equals(rValueType)) {
			returnValue = 1;
			System.err.println("[METHOD ERROR]: Invalid return type!");
			System.err.println("    Got " + rValueType);
			System.err.println(" Wanted " + declMethod.getReturnType().getType());
		}
	}

	public void visit(Formal formal) {
		Map expecMethodVars = ((MethodNode)this.currentNode).getLocalVariables();
		NodeType nodeType = nodeTypeOf(formal.type);
		expecMethodVars.put(formal.identifier.name, nodeType);
	}

	// -----------------------------------------------------------------
	//  Basic functions 
	// -----------------------------------------------------------------
	public void visit(If ifNode) {
		ifNode.exp.accept(this);

		if(!this.rValueType.equals(NodeType.BOOLEAN)) {
			returnValue = 1;
			System.out.print("[IF ERROR]: \tline " + ifNode.getLine() + ":");
			System.out.print(" non-boolean condition type="+this.rValueType);
			System.out.println(" from expression=" + ifNode.exp);
		}

		ifNode.statement1.accept(this);
		ifNode.statement2.accept(this);
	}

	public void visit(While whileNode) {
		whileNode.exp.accept(this);

		if(!this.rValueType.equals(NodeType.BOOLEAN)) {
			returnValue = 1;
			System.out.print("[WHILE ERROR]: \tline " + whileNode.getLine());
			System.out.println(": CONDITION NOT OF BOOLEAN TYPE!");
		}

		whileNode.statement.accept(this);
	}

	public void visit(Print print) {
		print.exp.accept(this);

		if (!this.rValueType.equals(NodeType.INT)) {
			returnValue = 1;
			System.out.print("[PRINT ERROR]: \tline " + print.getLine());
			System.out.println(": WE CAN ONLY PRINT INTEGERS");
		}
	}

	public void visit(Assign assign) {
		Map<String, Node> scope;
		ClassNode classNode;

		if(this.currentNode instanceof ClassNode) {
			classNode = (ClassNode) this.currentNode;
			scope = new HashMap(classNode.getMembers());

		} else {
			MethodNode methodNode = (MethodNode) this.currentNode;
			scope = new HashMap(methodNode.getLocalVariables());

			List<Node> params = methodNode.getParameters();
			Map<Integer, String> paramPos = methodNode.getParametersPositions();

			int index = 0;
			for(Node node: params) {
				String id = paramPos.get(index);
				scope.put(id, node);
			}

		}

		Node node = scope.get(assign.identifier.name);

		ClassWithParentNode classWithParentNode = null;
		if(node == null) {
			Node prevNode = this.previousNode;
			classNode = (ClassNode) this.previousNode;

			System.out.println( classNode.getName() );
			if(prevNode instanceof ClassWithParentNode) {
				classWithParentNode = (ClassWithParentNode) prevNode;
				System.out.println(classWithParentNode.getParent());
			}

			while(node == null && classWithParentNode != null) {
				scope = classNode.getMembers();
				prevNode = this.declaredTypes.getClasses().get(classWithParentNode.getParent());
			}

			if(node == null) {
				scope = classNode.getMembers();
				node = scope.get(assign.identifier.name);
			}

			if(node == null) {
				return;
			}
		}

		NodeType expecType = node.getType(); 

		assign.exp.accept(this);

		NodeType foundType = this.rValueType;

		if(!expecType.equals(foundType)) {
			returnValue = 1;
			System.out.print("[ASSIGN ERROR]: \tline " + assign.getLine());
			System.out.print(": " + assign.identifier + " expecs " + expecType);
			System.out.println(" found " + foundType);
		}
	}

	// -----------------------------------------------------------------
	//  Array & Object operation 
	// -----------------------------------------------------------------
	public void visit(ArrayAssign arrayAssign) {
		arrayAssign.exp1.accept(this);
		if(!this.rValueType.equals(NodeType.INT)) {
			returnValue = 1;
			System.out.print("[ARRAYASSIGN ERROR]: \tline " + arrayAssign.getLine());
			System.out.println(": INDEX NOT INT TYPE!");
		}
	}

	public void visit(ArrayLookup arryLookup) {
		arryLookup.exp1.accept(this);
		if (!this.rValueType.equals(NodeType.INTARRAY)) {
			returnValue = 1;
			System.out.print("[ARRAYLOOKUP ERROR]: \tline " + arryLookup.getLine());
			System.out.println(": CAN ONLY DEREFERENCE ARRAYS");
		}

		arryLookup.exp2.accept(this);
		if (!this.rValueType.equals(NodeType.INT)) {
			returnValue = 1;
			System.out.print("[ARRAYLOOKUP ERROR]: \tline " + arryLookup.getLine());
			System.out.println(": ARRAY INDEX MUST BE INTEGER");
		}
	}

	public void visit(ArrayLength arrayLength) {
		arrayLength.exp.accept(this);
		if(!this.rValueType.equals(NodeType.INTARRAY)) {
			returnValue = 1;
			System.out.print("[ARRAYLENGTH ERROR]: \tline " + arrayLength.getLine());
			System.out.println(": INDEX NOT INT TYPE!");
		}

		rValueType = NodeType.INT;
	}

	public void visit(NewArray newArray) {
		newArray.exp.accept(this);
		if(!this.rValueType.equals(NodeType.INT) &&
				!this.rValueType.equals(NodeType.FLOAT) &&
				!this.rValueType.equals(NodeType.DOUBLE)) {
			returnValue = 1;
			System.out.print("[NEW_ARRAY ERROR]: \tline " + newArray.getLine());
			System.out.println(": SIZE NOT A NUMBER TYPE");
		}

		rValueType = NodeType.INTARRAY;
	}

	public void visit(NewObject newObject) {
		if (!declaredTypes.getClasses().containsKey(newObject.identifier.name)) {
			returnValue = 1;
			System.out.print("[NEWOBJECT ERROR: \tline " + newObject.getLine());
			System.out.println(": UNDEFINED CLASS " + newObject.identifier.name);
		}
		this.rValueType = NodeType.CLASS;
	}

	// -----------------------------------------------------------------
	//  ULA:  argument1 operation argument2 
	// -----------------------------------------------------------------
	public void visit(Not notNode) {
		notNode.exp.accept(this);

		if(!this.rValueType.equals(NodeType.BOOLEAN)) {
			returnValue = 1;
			System.out.print("[NOT ERROR]: \tline " + notNode.getLine());
			System.out.print(": Exp " + notNode.exp + " is not boolean");
			System.out.println("\tFound rValue=" + this.rValueType);
		}
		this.rValueType = NodeType.BOOLEAN;
	}

	public void visit(And andNode) {
		andNode.exp1.accept(this);
		if(!this.rValueType.equals(NodeType.BOOLEAN)) {
			returnValue = 1;
			System.out.print("[AND ERROR]: line " + andNode.getLine());
			System.out.println(": ARG1 NOT NUMBER");
		}

		andNode.exp2.accept(this);
		if(!this.rValueType.equals(NodeType.BOOLEAN)) {
			returnValue = 1;
			System.out.print("[AND ERROR]: line " + andNode.getLine());
			System.out.println(": ARG2 NOT NUMBER");
		}

		this.rValueType = NodeType.BOOLEAN;
	}

	public void visit(LessThan lessThan) {
		lessThan.exp1.accept(this);
		checkArgument(lessThan.getLine(), "LESSTHAN", true);

		lessThan.exp2.accept(this);
		checkArgument(lessThan.getLine(), "LESSTHAN", false);

		this.rValueType = NodeType.BOOLEAN;
	}

	public void visit(Plus plus) {
		plus.exp1.accept(this);
		NodeType arg1 = this.rValueType;
		checkArgument(plus.getLine(), "PLUS", true);

		plus.exp1.accept(this);
		NodeType arg2 = this.rValueType;
		checkArgument(plus.getLine(), "PLUS", false);

		updateRValueType(arg1, arg2);
	}

	public void visit(Minus minus) {
		minus.exp1.accept(this);
		NodeType arg1 = this.rValueType;
		checkArgument(minus.getLine(), "MINUS", true);

		minus.exp1.accept(this);
		NodeType arg2 = this.rValueType;
		checkArgument(minus.getLine(), "MINUS", false);

		updateRValueType(arg1, arg2);
	}

	public void visit(Times times) {
		times.exp1.accept(this);
		NodeType arg1 = this.rValueType;
		checkArgument(times.getLine(), "TIMES", true);

		times.exp1.accept(this);
		NodeType arg2 = this.rValueType;
		checkArgument(times.getLine(), "TIMES", false);

		updateRValueType(arg1, arg2);
	}

	public void visit(GreatThan greatThan) {
		greatThan.exp1.accept(this);
		checkArgument(greatThan.getLine(), "GREATTHAN", true);

		greatThan.exp1.accept(this);
		checkArgument(greatThan.getLine(), "GREATTHAN", false);

		this.rValueType = NodeType.BOOLEAN;
	}

	public void visit(GreatThanEqual greatThanEqual) {
		greatThanEqual.exp1.accept(this);
		checkArgument(greatThanEqual.getLine(), "GREATTHANEQUAL", true);

		greatThanEqual.exp1.accept(this);
		checkArgument(greatThanEqual.getLine(), "GREATTHANEQUAL", false);

		this.rValueType = NodeType.BOOLEAN;
	}

	public void visit(LessThanEqual lessThanEqual) {
		lessThanEqual.exp1.accept(this);
		checkArgument(lessThanEqual.getLine(), "LESSTHANEQUAL", true);

		lessThanEqual.exp1.accept(this);
		checkArgument(lessThanEqual.getLine(), "LESSTHANEQUAL", false);

		this.rValueType = NodeType.BOOLEAN;	
	}

	public void visit(EqualEqual equalEqual) {
		equalEqual.exp1.accept(this);
		checkArgument(equalEqual.getLine(), "EQUALEQUAL", true);

		equalEqual.exp1.accept(this);
		checkArgument(equalEqual.getLine(), "EQUALEQUAL", false);

		this.rValueType = NodeType.BOOLEAN;
	}

	public void visit(NotEqual notEqual) {
		notEqual.exp1.accept(this);
		checkArgument(notEqual.getLine(), "NOTEQUAL", true);

		notEqual.exp1.accept(this);
		checkArgument(notEqual.getLine(), "NOTEQUAL", false);

		this.rValueType = NodeType.BOOLEAN;		
	}

	public void visit(Call call) {
		call.exp.accept(this);

		String callId = this.lastId;                            
		MethodNode methodNode = (MethodNode) this.currentNode;    
		ClassNode classNode = (ClassNode) this.previousNode;      

		Map<String, Node> classScope = classNode.getMembers();
		Map<String, Node> methodScope = methodNode.getLocalVariables();
		Map<Integer, String> parametersPositions = methodNode.getParametersPositions();

		int index = 0;
		for(Node node: methodNode.getParameters()) {
			methodScope.put(parametersPositions.get(index++), node);
		}

		Node node = null;

		if (methodScope.containsKey(callId)) { node = methodScope.get(callId); }
		if (classScope.containsKey(callId)) { node = classScope.get(callId); }

		if(node != null && node.getType().equals(NodeType.CLASS)) {
			this.previousNode = this.declaredTypes.getClasses().get(node.getIam());
		}

		Node caller = this.previousNode;
		Map<String, Node> scope = ((ClassNode)caller).getMembers();
		Node myNode = scope.get(call.identifier.name);

		call.expList.accept(this);

		if(myNode != null) {
			this.rValueType = ((MethodNode)myNode).getReturnType().getType();

		} else if(caller instanceof ClassWithParentNode) { 
			ClassWithParentNode classWithParentNode = null;       
			Node prevNode = caller;

			while(myNode == null && prevNode instanceof ClassWithParentNode) {
				classNode = (ClassNode) prevNode;
				classWithParentNode = (ClassWithParentNode) prevNode;
				scope = classNode.getMembers();

				myNode = scope.get(call.identifier.name);
				prevNode = this.declaredTypes.getClasses().get(classWithParentNode.getParent());
			}

			if(myNode == null) { 
				classNode = (ClassNode) prevNode;
				scope = classNode.getMembers();
				myNode = scope.get(call.identifier.name);
			}

			if(myNode == null) {
				returnValue = 1;
				System.out.print("[LOOKUP ERROR]: \tline " + call.getLine());
				System.out.print(": couldn't find " + call.identifier + "in");
				System.out.println(" scope containing" + scope.keySet());
				return;
			}
			this.rValueType = ((MethodNode)myNode).getReturnType().getType();
		} else {
			returnValue = 1;
			System.out.print("[CALL ERROR]: \tline " + call.getLine());
			System.out.print(": did not find " + call.identifier.name + " in ");
			System.out.println("the scope of "+caller);
		}
	}

	public void visit(IdentifierExp identifierExp) {
		Map<String, Node> scope;
		this.lastId = identifierExp.name;

		scope = ((MethodNode) this.currentNode).getLocalVariables();
		Node myNode = scope.get(identifierExp.name);

		MethodNode methodNode = null;
		ClassNode classNode = null;

		if(myNode == null) {
			if(this.currentNode instanceof MethodNode) { 
				methodNode = (MethodNode) this.currentNode;
				List<Node> parameters = methodNode.getParameters();
				Map<Integer, String> parametersPositions = methodNode.getParametersPositions();

				for(int i = 0; i < parameters.size(); i++) { 
					String param = parametersPositions.get(i);
					if(param.equals(identifierExp.name)) {
						this.rValueType = parameters.get(i).getType();
						return;
					}
				}

				myNode = methodNode.getLocalVariables().get(identifierExp.name);
				if(myNode != null) {
					this.rValueType = myNode.getType();
					return;
				}
			}
			if(this.previousNode instanceof ClassNode) {
				classNode = (ClassNode) this.previousNode;
				scope = classNode.getMembers();
				myNode = scope.get(identifierExp.name);

				if(myNode != null) {
					this.rValueType = myNode.getType();
					if (myNode instanceof MethodNode) {
						methodNode = (MethodNode) myNode;
						this.rValueType = methodNode.getReturnType().getType();
					}

					return;

				}
			}
			returnValue = 1;
			System.out.print("[IDENTIFIER_EXP ERROR]: \tline " + identifierExp.getLine());
			System.out.print(": " + identifierExp.name + " not found! in KeySet=");
			System.out.println(methodNode.getLocalVariables());
			return;
		}
		this.rValueType = myNode.getType();
	}

	public void visit(Identifier identifier) {
		Map<String, Node> scope;
		MethodNode methodNode = null;

		if(this.previousNode instanceof ClassNode) {
			scope = ((ClassNode)this.previousNode).getMembers();
		} else {
			methodNode = (MethodNode) this.currentNode;
			scope = methodNode.getLocalVariables();
		}

		Node myNode = scope.get(identifier.name);
		if(myNode == null) {
			if(this.currentNode instanceof MethodNode) { 
				methodNode = (MethodNode) this.currentNode;;
				List<Node> paramList = methodNode.getParameters();
				Map<Integer, String> paramPos = methodNode.getParametersPositions();

				for(int i = 0; i < paramList.size(); i++) {
					String param = paramPos.get(i);
					if(param.equals(identifier.name)) {
						this.rValueType = paramList.get(i).getType();
						return;
					}
				}
			}
			returnValue = 1;
			System.out.print("[IDENTIFIER ERROR]: line " + identifier.getLine());
			System.out.print(": " + identifier.name + " not found in scope containing");
			System.out.println("Keys=" + scope.keySet());
			return;
		}
		this.rValueType = myNode.getType();
	}

	// -----------------------------------------------------------------
	//
	// -----------------------------------------------------------------

	public void visit(This thisNode) {
		this.rValueType = NodeType.CLASS;
	}

	public void visit(FloatLiteral floatLiteral) {
		this.rValueType = NodeType.FLOAT;
	}

	public void visit(FloatType floatType) {
		this.rValueType = nodeTypeOf(floatType);
	}

	public void visit(DoubleLiteral doubleLiteral) {
		this.rValueType = NodeType.DOUBLE;
	}

	public void visit(DoubleType doubleType) {
		this.rValueType = nodeTypeOf(doubleType);
	}

	public void visit(InstanceOf instanceOf) {
		this.rValueType = NodeType.BOOLEAN;
	}

	public void visit(Null nullNode) {
		this.rValueType = NodeType.UNKNOWN;
	}

	public void visit(IntegerLiteral integerLiteral) {
		this.rValueType = NodeType.INT;
	}

	public void visit(True trueNode) {
		this.rValueType = NodeType.BOOLEAN;
	}

	public void visit(False falseNode) {
		this.rValueType = NodeType.BOOLEAN;
	}

	public void visit(IntArrayType intArrayType) {
		this.rValueType = nodeTypeOf(intArrayType);
	}

	public void visit(BooleanType booleanType) {
		this.rValueType = nodeTypeOf(booleanType);
	}

	public void visit(IntegerType integerType) {
		this.rValueType = nodeTypeOf(integerType);
	}

	public void visit(IdentifierType identifierType) {
		this.rValueType = nodeTypeOf(identifierType);
	}

	public void visit(Block block) {
		block.statementList.accept(this);
	}

	// -----------------------------------------------------------------
	//  Empty
	// -----------------------------------------------------------------
	public void visit(MainClass mainClass) { }
	public void visit(VarDecl n) { }


	// -----------------------------------------------------------------
	//  Util
	// -----------------------------------------------------------------
	private NodeType nodeTypeOf(Type type) {
		if (type instanceof BooleanType) {
			return NodeType.BOOLEAN;
		} else if (type instanceof FloatType) {
			return NodeType.FLOAT;
		} else if (type instanceof DoubleType) {
			return NodeType.DOUBLE;
		} else if (type instanceof IntegerType) {
			return NodeType.INT;
		} else if (type instanceof IntArrayType) {
			return NodeType.INTARRAY;
		} else if (type instanceof IdentifierType) {
			return NodeType.CLASS;
		} else {
			return NodeType.UNKNOWN;
		}
	}

	private void errorUnknownSymbol(String className, Map<String, ClassNode> classes) {
		returnValue = 1;
		System.out.print("[CLASS ERROR]: " + className);
		System.out.println(" not in symbol table");

		Node unknownClass = new Node(NodeType.UNKNOWN);
		classes.put(className, (ClassNode) unknownClass);
	}

	private void errorUnknownType(String className, ClassNode classNode) {
		NodeType noteType = classNode.getType();
		if(noteType != NodeType.CLASS && noteType != NodeType.UNKNOWN) {
			returnValue = 1;
			System.out.print("[CLASS ERROR]: " + className);
			System.out.println(" declared as " + noteType);
		}
	}

	private void checkArgument(int line, String typeName, boolean arg1) {
		String argText = arg1 ? ": ARG1 NOT NUMBER" : ": ARG2 NOT NUMBER";
		if(!this.rValueType.equals(NodeType.INT) &&
				!this.rValueType.equals(NodeType.FLOAT) &&
				!this.rValueType.equals(NodeType.DOUBLE)) {
			returnValue = 1;
			System.out.print("[" + typeName + " ERROR]: line " + line);
			System.out.println(argText);
		}
	}

	private void updateRValueType(NodeType arg1, NodeType arg2) {
		if(arg1.equals(NodeType.FLOAT) || arg2.equals(NodeType.FLOAT)) {
			this.rValueType = NodeType.FLOAT;
		} else {
			this.rValueType = NodeType.INT;
		}
	}
}
