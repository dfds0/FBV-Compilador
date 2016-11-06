package CodeGenerator;

import java.util.*;
import AST.*;
import AST.Visitor.*;
import Semantic.*;

public class IntermediateCode implements Visitor {
	
	private List<String> code;
	private String currentClass;
	private String currentMethod;
	private TypeVisitor declaredTypes;
	private Map<String, Integer> currentMethodParameters;
	private Map<String, Integer> currentMethodVariables;
	private int lastLabel;
	private Map<String, Map> vTable;
	private String lastSeenType;

	public IntermediateCode(TypeVisitor declaredTypes) {
		super();
		this.code = new ArrayList<String>();
		this.currentClass = null;
		this.currentMethod = null;
		this.currentMethodParameters = null;
		this.declaredTypes = declaredTypes;
		this.lastLabel = 0;
		this.vTable = null;
		this.lastSeenType = null;
	}

	public List<String> getCode() {
		return code;
	}

	private String getLabel() {
		String rv = "L" + lastLabel;
		++lastLabel;
		return rv;
	}

	/*
	 * vTable: key=class name, value=(method name=>slot number)
	 */
	private void createVTables() {
		this.vTable = new HashMap<String, Map>();

		Map classesMap = this.declaredTypes.getClasses();
		Iterator classesIterator = classesMap.entrySet().iterator();

		Map.Entry classesMapEntry;
		String className;
		ClassNode classNode;
		Map methodsMap;
		List<ClassNode> classNodes;
		HashMap<String, Integer> classTable;

		while(classesIterator.hasNext()) {
			classesMapEntry = (Map.Entry)classesIterator.next();
			className 		= (String)classesMapEntry.getKey();
			classNode 		= (ClassNode)classesMapEntry.getValue();

			methodsMap = collectVtableMethods(className, classNode, classesMap);

			classNodes = createClsRelList(classNode, classesMap);
			classTable = constructTableEntry(className, methodsMap, classNodes, classNode);

			this.vTable.put(className, classTable);
		}
	}

	/*
	 * clsRel is used to start formatting vtable entry based on older classes
	 *
	 *      VTable Representation:
	 *          Foo$$:
	 *              .long 0 # null parent   [slot 0]
	 *              .long Foo$MethodA       [slot 1]
	 *              .long Foo$MethodB       [slot 2]
	 *          Bar$$
	 *              .long Foo$$             [slot 0]
	 *              .long Foo$MethodA       [slot 1]
	 *              .long Bar$MethodB       [slot 2]
	 *
	 * @return: HashMap, key=method, value=slot #
	 */
	private HashMap<String, Integer> constructTableEntry(String className, Map<String, String> methodsMap, 
			List<ClassNode> classNodes, ClassNode classNode) {

		HashMap<String, Integer> classTable = new HashMap<String, Integer>();
		int slotNumber = 1;
		code.add(className + ":");

		HashSet<String> recordedMethods = new HashSet<String>();
		Iterator classNodeIterator = classNodes.iterator();

		// First loop
		ClassNode currentClassNode;
		Map<String, MethodNode> currentMethodMap;
		Iterator currentMethodIterator;

		// Second loop
		String methodName;
		String methEntry;

		while(classNodeIterator.hasNext()) {
			currentClassNode = (ClassNode)classNodeIterator.next();
			currentMethodMap = getMethods(currentClassNode);
			currentMethodIterator = currentMethodMap.keySet().iterator();

			while(currentMethodIterator.hasNext()) {
				methodName = (String)currentMethodIterator.next();
				if(!recordedMethods.contains(methodName)) {
					methEntry = methodsMap.get(methodName);
					code.add("    " + methEntry);
					classTable.put(methodName, new Integer(slotNumber++));
					recordedMethods.add(methodName);
				}
			}
		}

		return classTable;
	}

	/*
	 * Collects all the methods visible by a class, as well as the code
	 *      source of each method
	 */
	private Map<String, String> collectVtableMethods(String className, ClassNode classNode, Map classMap) {
		Map<String, String> methods = new HashMap<String, String>();

		String currentClassName = className;
		ClassNode currentClassNode = classNode;

		Map<String, MethodNode> methodMap;
		Iterator methodIterator;
		String methodName;

		while(currentClassNode instanceof ClassWithParentNode) {
			methodMap = getMethods(currentClassNode);
			methodIterator = methodMap.keySet().iterator();

			while(methodIterator.hasNext()) {
				methodName = (String)methodIterator.next();
				if(!methods.containsKey((String)methodName)) {
					methods.put(methodName, currentClassName + "$" + methodName);
				}
			}

			currentClassName = ((ClassWithParentNode)currentClassNode).getParent();
			currentClassNode = (ClassNode)classMap.get(currentClassName);
		}

		// finish adding methods from base class...
		methodMap = getMethods(currentClassNode);
		methodIterator = methodMap.keySet().iterator();
		while(methodIterator.hasNext()) {
			methodName = (String)methodIterator.next();
			if(!methods.containsKey(methodName)) {
				methods.put(methodName, currentClassName + "$" + methodName);
			}
		}
		return methods;
	}

	/*
	 * Get all methods associated with a class
	 */
	private Map<String, MethodNode> getMethods(ClassNode classNode) {
		Map<String, MethodNode> methodMap = new HashMap<String, MethodNode>();

		Map<String, Node> attrsMap = classNode.getMembers();
		Iterator attrsIterator = attrsMap.entrySet().iterator();
		Map.Entry attrMapEntry;
		String methodName;
		MethodNode method;

		while(attrsIterator.hasNext()) {
			attrMapEntry = (Map.Entry) attrsIterator.next();
			if(attrMapEntry.getValue() instanceof MethodNode) {
				methodName = (String)attrMapEntry.getKey();
				method = (MethodNode)attrMapEntry.getValue();
				methodMap.put(methodName, method);
			}
		}
		return methodMap;
	}

	/*
	 * Construct List: This is used to backtrack to make sure we vtable
	 *      entries are aligned uniformly according the the ancestors.
	 *
	 *      [GreatGrandpa] -> [Grandpa] -> [pa] -> [me]
	 *          ^
	 *        HEAD
	 */
	private List<ClassNode> createClsRelList(ClassNode classNode, Map<String, ClassNode> classes) {
		ClassNode currentClassNode = classNode;
		List<ClassNode> classNodeList = new LinkedList<ClassNode>();
		classNodeList.add(classNode);

		ClassWithParentNode classWithParentNode;
		ClassNode nextClassNode;

		while(currentClassNode instanceof ClassWithParentNode) {
			classWithParentNode = (ClassWithParentNode) currentClassNode;
			nextClassNode = classes.get(classWithParentNode.getParent());
			classNodeList.add(0, nextClassNode);
			currentClassNode = nextClassNode;
		}
		return classNodeList;
	}

	public Map<String, Integer> getMethodParameterOffsets(String className, String methodName) {
		MethodNode methodNode = (MethodNode) declaredTypes.getClasses().get(className).getMembers().get(methodName);
		Map<Integer, String> parameterPositions = methodNode.getParametersPositions();
		Map<String, Integer> rv = new HashMap<String, Integer>();

		for (Map.Entry<Integer, String> entry : parameterPositions.entrySet()) {
			rv.put(entry.getValue(), entry.getKey());
		}

		return rv;
	}

	public Map<String, Integer> getMethodVariableOffsets(String className, String methodName) {
		MethodNode methodNode = (MethodNode) declaredTypes.getClasses().get(className).getMembers().get(methodName);
		String[] localVariables = methodNode.getLocalVariables().keySet().toArray(new String[0]);

		Arrays.sort(localVariables);

		Map<String, Integer> rv = new HashMap<String, Integer>();
		int i = 0;
		for (String localvar : localVariables) {
			rv.put(localVariables[i], i);
			++i;
		}

		return rv;
	}

	public Map<String, Integer> getInstanceVariableOffsets(String className) {
		Map<String, ClassNode> classes = declaredTypes.getClasses();

		Map<String, Integer> rv = new HashMap<String, Integer>();
		int currentPosition = 1; // 0 has the vtable pointer
		ClassNode classNode = classes.get(className);

		ClassWithParentNode classWithParentNode;
		while (true) {
			for (Map.Entry<String, Node> entry : classNode.getMembers().entrySet()) {
				if (!(entry.getValue() instanceof MethodNode)) {
					rv.put(entry.getKey(), currentPosition++);
				}
			}

			if (classNode instanceof ClassWithParentNode) {
				classWithParentNode = (ClassWithParentNode) classNode;
				classNode = classes.get(classWithParentNode.getParent());
			} else {
				break;
			}
		}

		return rv;
	}

	public void visit(Program program) {
		createVTables();
		program.mainClass.accept(this);
		program.classDeclList.accept(this);
	}

	public void visit(MainClass mainClass) {
		code.add("main: ");
		code.add("    call " + mainClass.statement.statementToString());
	}

	public void visit(ClassDeclSimple classDeclSimple) {
		currentClass = classDeclSimple.identifier.name;

		classDeclSimple.varDeclList.accept(this);
		classDeclSimple.methodDeclList.accept(this);

		currentClass = null;
	}

	public void visit(ClassDeclExtends classDeclExtends) {
		currentClass = classDeclExtends.identifier1.name;

		classDeclExtends.varDeclList.accept(this);
		classDeclExtends.methodDeclList.accept(this);

		currentClass = null;
	}

	public void visit(MethodDecl methodDecl) {
		currentMethod = methodDecl.identifier.name;

		code.add(currentClass + "$" + currentMethod + ":");

		currentMethodParameters = new HashMap<String, Integer>();
		FormalList params = methodDecl.formalList;
		int paramsCount = params.size();
		for (int i = 0; i < paramsCount; ++i) {
			currentMethodParameters.put(params.elementAt(i).identifier.name, i);
		}

		currentMethodVariables = new HashMap<String, Integer>();
		VarDeclList localVariables = methodDecl.varDeclList;
		int variablesCount = localVariables.size();
		for (int i = 0; i < variablesCount; ++i) {
			currentMethodVariables.put(localVariables.elementAt(i).identifier.name, i);
		}

		methodDecl.statementList.accept(this);
		code.add("return " + methodDecl.exp.expToValue());

		currentMethodParameters = null;
		currentMethod = null;
	}

	public void visit(Block block) {
		block.statementList.accept(this);
	}

	public void visit(If ifNode) {
		code.add("    if " + ifNode.exp.expToCondition() + " goto ifTrue");
		code.add("        " + ifNode.statement2.statementToString() + ";");
		code.add("        goto endElse");
		code.add("    ifTrue:");
		code.add("        " + ifNode.statement1.statementToString() + ";");
		code.add("    endElse:");
	}

	public void visit(While whileNode) {
		code.add("    while: if " + whileNode.exp.expToCondition() + " goto endWhile");
		code.add("        " + whileNode.statement.statementToString());
		code.add("        goto while:");
		code.add("   endWhile:");
	}

	public void visit(Print print) {
		code.add("    print " + print.exp.expToValue());
	}

	public void visit(Assign assign) {
		assign.exp.accept(this);
		code.add("    " + assign.identifier.name);
	}

	public void visit(ArrayAssign arrayAssign) {
		arrayAssign.exp2.accept(this);
		code.add("    " + arrayAssign.exp1);
	}

	public void visit(And andNode) {
		code.add("    " + andNode.exp1.expToCondition() + " && " + andNode.exp2.expToCondition());
	}

	public void visit(LessThan lessThan) {
		code.add(lessThan.exp1.expToValue() + " < " + lessThan.exp2.expToValue());
	}

	public void visit(Plus plus) {
		code.add(plus.exp1.expToValue() + " + " + plus.exp2.expToValue());
	}

	public void visit(Minus minus) {
		code.add(minus.exp1.expToValue() + " - " + minus.exp2.expToValue());
	}

	public void visit(Times times) {
		code.add(times.exp1.expToValue() + " * " + times.exp2.expToValue());
	}

	public void visit(ArrayLookup arrayLookup) {
		code.add("    " + arrayLookup.exp1);
		code.add("    " + arrayLookup.exp2);
	}

	public void visit(ArrayLength arrayLength) {
		code.add("    " + arrayLength.exp);
	}

	public void visit(Call call) {
		code.add("    call " + call.identifier.name);
	}

	public void visit(IntegerLiteral integerLiteral) {
		code.add("    " + integerLiteral.value);
	}

	public void visit(True trueNode) {
		code.add("    true");
	}

	public void visit(False falseNode) {
		code.add("    false");
	}

	public void visit(IdentifierExp identifierExp) {
		code.add("    " + identifierExp.name);
	}

	public void visit(This thisNode) {
		code.add("    " + thisNode);
		lastSeenType = currentClass;
	}

	public void visit(NewArray newArray) {
		newArray.exp.accept(this);
		code.add("    " + newArray);
	}

	public void visit(NewObject newObject) {
		code.add("    " + newObject.identifier.name);
		lastSeenType = newObject.identifier.name;
	}

	public void visit(Not notNode) {
		getLabel(); // update for true
		getLabel(); // update for false

		notNode.exp.accept(this);
		code.add("    " + notNode.exp);
	}

	@Override
	public void visit(GreatThan greatThan) {
		getLabel(); // update for true
		getLabel(); // update for false

		greatThan.exp1.accept(this);
		code.add("    " + greatThan.exp1);
		greatThan.exp2.accept(this);
		code.add("    > " + greatThan.exp2);
	}

	@Override
	public void visit(GreatThanEqual greatThanEqual) {
		getLabel(); // update for true
		getLabel(); // update for false

		greatThanEqual.exp1.accept(this);
		code.add("    " + greatThanEqual.exp1);
		greatThanEqual.exp2.accept(this);
		code.add("    >= " + greatThanEqual.exp2);
	}

	@Override
	public void visit(LessThanEqual lessThanEqual) {
		getLabel(); // update for true
		getLabel(); // update for false

		lessThanEqual.exp1.accept(this);
		code.add("    " + lessThanEqual.exp1);
		lessThanEqual.exp2.accept(this);
		code.add("    <= " + lessThanEqual.exp2);
	}

	@Override
	public void visit(EqualEqual equalEqual) {
		getLabel(); // update for true
		getLabel(); // update for false

		equalEqual.exp1.accept(this);
		code.add("    " + equalEqual.exp1);
		equalEqual.exp2.accept(this);
		code.add("    == " + equalEqual.exp2);
	}

	@Override
	public void visit(NotEqual notEqual) {
		getLabel(); // update for true
		getLabel(); // update for false

		notEqual.exp1.accept(this);
		code.add("    " + notEqual.exp1);
		notEqual.exp2.accept(this);
		code.add("    != " + notEqual.exp2);
	}

	public void visit(VarDecl n) { }
	
	public void visit(Formal n) { }

	public void visit(IntArrayType n) { }

	public void visit(BooleanType n) { }

	public void visit(IntegerType n) { }

	public void visit(IdentifierType n) { }
	
	public void visit(Identifier n) { }

	public void visit(FloatLiteral n) { }

	public void visit(FloatType n) { }

	public void visit(DoubleLiteral n) { }

	public void visit(DoubleType n) { }

	public void visit(Null n) { }

	public void visit(InstanceOf node) { }
}

