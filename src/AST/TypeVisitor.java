package AST.Visitor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import AST.*;
import Semantic.*;

public class TypeVisitor implements Visitor {

	private Map<String, ClassNode> classes;
	private ClassNode currentClass;
	private MethodNode currentMethod;

	public TypeVisitor() {
		super();

		classes = new HashMap<String, ClassNode>();
		currentClass = null;
		currentMethod = null;
	}

	public Map<String, ClassNode> getClasses() {
		return classes;
	}

	public void printTypes() {
		ClassWithParentNode classWithParentNode = null;
		MethodNode methodNode = null;
		List<Node> parameterTypes = null;
		Map<Integer, String> parametersPositions = null;
		Node value = null;
		
		for (Map.Entry<String, ClassNode> entry : classes.entrySet()) {
			ClassNode classNode = entry.getValue();

			if (classNode instanceof ClassWithParentNode) {
				classWithParentNode = (ClassWithParentNode) classNode;
				System.out.println(entry.getKey() + " extends " + classWithParentNode.getParent());

			} else {
				System.out.println(entry.getKey());
			}

			for (Map.Entry<String, Node> inner : classNode.getMembers().entrySet()) {
				value = inner.getValue();

				System.out.println("    " + inner.getKey() + ": " + value.getType().toString());

				if (value instanceof MethodNode) {
					methodNode = (MethodNode) value;
					parameterTypes = methodNode.getParameters();
					parametersPositions = methodNode.getParametersPositions();

					System.out.println("        Parameters:");
					for (Map.Entry<Integer, String> param : parametersPositions.entrySet()) {
						System.out.println("            " + param.getValue() + ": " + 
								parameterTypes.get(param.getKey()).getType().toString());
					}

					System.out.println("        Local Variables:");
					for (Map.Entry<String, Node> var : methodNode.getLocalVariables().entrySet()) {
						System.out.println("            " + var.getKey() + ": " + var.getValue().getType());
					}
				}
			}
		}
	}

	public void visit(Program program) {
		program.classDeclList.accept(this);
	}

	public void visit(ClassDeclSimple classDeclSimple) {
		ClassNode node = new ClassNode(classDeclSimple.identifier.name);
		currentClass = node;
		classes.put(classDeclSimple.identifier.name, node);

		classDeclSimple.varDeclList.accept(this);
		classDeclSimple.methodDeclList.accept(this);

		currentClass = null;
	}

	public void visit(ClassDeclExtends classDeclExtends) {
		ClassWithParentNode node = new ClassWithParentNode(classDeclExtends.identifier1.name, classDeclExtends.identifier2.name);
		currentClass = node;
		classes.put(classDeclExtends.identifier1.name, node);

		classDeclExtends.varDeclList.accept(this);
		classDeclExtends.methodDeclList.accept(this);

		currentClass = null;
	}

	public void visit(VarDecl varDecl) {
		Node node = new Node(nodeTypeOf(varDecl.type));
		if(varDecl.type instanceof IdentifierType) {
			node.iam = ((IdentifierType)varDecl.type).name;
		}

		if (currentMethod != null) {
			currentMethod.getLocalVariables().put(varDecl.identifier.name, node);
		} else {
			currentClass.getMembers().put(varDecl.identifier.name, node);
		}
	}

	public void visit(MethodDecl methodDecl) {
		MethodNode node = new MethodNode(new Semantic.Node(nodeTypeOf(methodDecl.type)));
		currentMethod = node;
		currentClass.getMembers().put(methodDecl.identifier.name, node);

		methodDecl.formalList.accept(this);
		methodDecl.varDeclList.accept(this);

		currentMethod = null;
	}

	public void visit(Formal formal) {
		List<Node> parameters = currentMethod.getParameters();

		Semantic.Node node = new Semantic.Node(nodeTypeOf(formal.type));
		if(formal.type instanceof IdentifierType) {
			node.iam = ((IdentifierType)formal.type).name;
		}

		parameters.add(node);
		currentMethod.getParametersPositions().put(parameters.size() - 1, formal.identifier.name);
	}

	public void visit(MainClass n) { }
	public void visit(IntArrayType n) { }
	public void visit(BooleanType n) { }
	public void visit(IntegerType n) { }
	public void visit(IdentifierType n) { }
	public void visit(Block n) { }
	public void visit(If n) { }
	public void visit(While n) { }
	public void visit(Print n) { }
	public void visit(Assign n) { }
	public void visit(ArrayAssign n) { }
	public void visit(And n) { }
	public void visit(LessThan n) { }
	public void visit(Plus n) { }
	public void visit(Minus n) { }
	public void visit(Times n) { }
	public void visit(ArrayLookup n) { }
	public void visit(ArrayLength n) { }
	public void visit(Call n) { }
	public void visit(IntegerLiteral n) { }
	public void visit(True n) { }
	public void visit(False n) { }
	public void visit(IdentifierExp n) { }
	public void visit(This n) { }
	public void visit(NewArray n) { }
	public void visit(NewObject n) { }
	public void visit(Not n) { }
	public void visit(Identifier n) { }
	public void visit(FloatLiteral n) { }
	public void visit(FloatType n) { }
	public void visit(GreatThan n) { }
	public void visit(GreatThanEqual n) { }
	public void visit(LessThanEqual n) { }
	public void visit(DoubleLiteral n) { }
	public void visit(DoubleType n) { }
	public void visit(EqualEqual n) { }
	public void visit(NotEqual n) { }
	public void visit(InstanceOf n) { }
	public void visit(Null n) { }

	private static NodeType nodeTypeOf(Type t) {
		if (t instanceof BooleanType) {
			return NodeType.BOOLEAN;
		} else if (t instanceof FloatType) {
			return NodeType.FLOAT;
		} else if (t instanceof DoubleType) {
			return NodeType.DOUBLE;
		} else if (t instanceof IntegerType) {
			return NodeType.INT;
		} else if (t instanceof IntArrayType) {
			return NodeType.INTARRAY;
		} else if (t instanceof IdentifierType) {
			return NodeType.CLASS;
		} else {
			return NodeType.UNKNOWN;
		}
	}


}