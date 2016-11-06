package CodeGenerator;

import java.util.*;
import java.util.Map.Entry;

import AST.*;
import AST.Visitor.*;
import Semantic.*;

public class CodeGenerator implements Visitor {

	private List<String> code;
	private String currentClass;
	private String currentMethod;
	private TypeVisitor declaredTypes;
	private Map<String, Integer> currentMethodParameters;
	private Map<String, Integer> currentMethodVariables;
	private int lastLabel;
	private Map<String, Map> vTable;
	private String lastSeenType;
	private IntermediateCode intermediateCode;

	public CodeGenerator(TypeVisitor declaredTypes, IntermediateCode intermediate) {
		super();
		this.code = new ArrayList<String>();
		this.currentClass = null;
		this.currentMethod = null;
		this.currentMethodParameters = null;
		this.declaredTypes = declaredTypes;
		this.lastLabel = 0;
		this.vTable = null;
		this.lastSeenType = null;
		this.intermediateCode = intermediate;
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

		Map<String, ClassNode> classes = this.declaredTypes.getClasses();
		Iterator<Entry<String, ClassNode>> classIterator = classes.entrySet().iterator();
		Map.Entry<String, ClassNode> mapEntry;
		String className;
		ClassNode classNode;
		Map<String, String> methods;
		List<ClassNode> classNodeList;
		HashMap<String, Integer> clsVTable;

		while(classIterator.hasNext()) {
			mapEntry = classIterator.next();
			className = mapEntry.getKey();
			classNode = mapEntry.getValue();

			methods = collectVtableMethods(className, classNode, classes);
			classNodeList = createClsRelList(classNode, classes);
			clsVTable = constructTableEntry(className, methods, classNodeList, classNode);
			this.vTable.put(className, clsVTable);
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
	private HashMap<String, Integer> constructTableEntry(String className, Map<String, String> methodMap,
			List<ClassNode> classNodeList, ClassNode classNode) {

		HashMap<String, Integer> clsVTable = new HashMap<String, Integer>();
		int slotNumber = 1;

		String classNameParent = "";
		ClassWithParentNode classWithParentNod;
		if(classNode instanceof ClassWithParentNode) {
			classWithParentNod = (ClassWithParentNode) classNode;
			classNameParent = classWithParentNod.getParent() + "$$";
		} else {
			classNameParent = "0";
		}

		code.add(className + "$$:");
		code.add("    .long " + classNameParent);

		HashSet<String> recordedMethods = new HashSet<String>();
		Iterator<ClassNode> classNodeIterator = classNodeList.iterator();

		// Loop 1
		ClassNode currentClassNode;
		Map<String, MethodNode> currentMethodMap;
		Iterator currentMethodIterator;
		// Loop 2
		String meth;
		String methEntry;

		// TODO trocar por um for
		while(classNodeIterator.hasNext()) {
			currentClassNode = classNodeIterator.next();
			currentMethodMap = getMethods(currentClassNode);
			currentMethodIterator = currentMethodMap.keySet().iterator();

			// TODO trocar por um for
			while(currentMethodIterator.hasNext()) {
				meth = (String)currentMethodIterator.next();

				if(!recordedMethods.contains(meth)) {
					methEntry = methodMap.get(meth);
					code.add("    .long " + methEntry);
					clsVTable.put(meth, new Integer(slotNumber++));
					recordedMethods.add(meth);
				}
			}
		}
		return clsVTable;
	}

	/*
	 * Collects all the methods visible by a class, as well as the code
	 *      source of each method
	 */
	private Map<String, String> collectVtableMethods(String className, ClassNode classNode, Map<String, ClassNode> classes) {
		Map<String, String> methods = new HashMap<String, String>();

		String currentClassName = className;
		ClassNode currentClassNode = classNode;

		Map<String, MethodNode> currentMethodMap;
		Iterator<String> methodIterator;
		String methodName;

		while(currentClassNode instanceof ClassWithParentNode) {
			currentMethodMap = getMethods(currentClassNode);
			methodIterator = currentMethodMap.keySet().iterator();
			while(methodIterator.hasNext()) {
				methodName = methodIterator.next();
				if(!methods.containsKey(methodName)) {
					methods.put(methodName, currentClassName + "$" + methodName);
				}
			}

			currentClassName = ((ClassWithParentNode)currentClassNode).getParent();
			currentClassNode = classes.get(currentClassName);
		}

		// finish adding methods from base class...
		currentMethodMap = getMethods(currentClassNode);
		methodIterator = currentMethodMap.keySet().iterator();
		while(methodIterator.hasNext()) {
			methodName = methodIterator.next();
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

		Map<String, Node> allAttrs = classNode.getMembers();
		Iterator<Entry<String, Node>> allAttrsIterator = allAttrs.entrySet().iterator();
		Entry<String, Node> attr_entry;
		String methodName;
		MethodNode methodNode;

		while(allAttrsIterator.hasNext()) {
			attr_entry = allAttrsIterator.next();
			if(attr_entry.getValue() instanceof MethodNode) {
				methodName = attr_entry.getKey();
				methodNode = (MethodNode) attr_entry.getValue();
				methodMap.put(methodName, methodNode);
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
	private List<ClassNode> createClsRelList(ClassNode classNode, Map<String, ClassNode> classMap) {
		ClassNode currentClassNode = classNode;
		List<ClassNode> classNodeList = new LinkedList<ClassNode>();
		classNodeList.add(classNode);

		ClassWithParentNode classWithParentNode;
		ClassNode nextClassNode;

		while(currentClassNode instanceof ClassWithParentNode) {
			classWithParentNode = (ClassWithParentNode) currentClassNode;
			nextClassNode = classMap.get(classWithParentNode.getParent());
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
		code.add("    .text");
		code.add("    .global asm_main");
		code.add("");
		code.add("asm_main:");
		mainClass.statement.accept(this);
		code.add("    ret");
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
		code.add("    pushl %ebp");
		code.add("    movl %esp, %ebp");
		code.add("    subl $" + (4 * methodDecl.varDeclList.size()) + ", %esp");
		code.add("    pushl %ecx");

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

		// Return value. Left at eax.
		methodDecl.exp.accept(this);

		code.add("    addl $" + (1 + 4 * methodDecl.varDeclList.size()) + ", %esp");
		code.add("    movl %ebp, %esp");
		code.add("    popl %ebp");
		code.add("    ret");

		currentMethodParameters = null;
		currentMethod = null;
	}

	public void visit(Block block) {
		block.statementList.accept(this);
	}

	public void visit(If ifNode) {
		String labelElse = getLabel();
		String labelEnd = getLabel();

		ifNode.exp.accept(this);
		code.add("    cmpl $0, %eax");
		code.add("    je " + labelElse);
		ifNode.statement1.accept(this);
		code.add("    jmp " + labelEnd);
		code.add(labelElse + ":");
		ifNode.statement2.accept(this);
		code.add(labelEnd + ":");
	}

	public void visit(While whileNode) {
		String labelStart = getLabel();
		String labelTest = getLabel();

		code.add("    jmp " + labelTest);
		code.add(labelStart + ":");
		whileNode.statement.accept(this);
		code.add(labelTest + ":");
		whileNode.exp.accept(this);
		code.add("    cmpl $0, %eax");
		code.add("    jne " + labelStart);
	}

	public void visit(Print print) {
		print.exp.accept(this);
		code.add("    pushl %eax");
		code.add("    call put");
		code.add("    addl $4, %esp");
		code.add("    movl (%esp), %ecx");
	}

	public void visit(Assign assign) {
		assign.exp.accept(this);

		String name = assign.identifier.name;

		Map<String, Integer> paramOffsets = getMethodParameterOffsets(currentClass, currentMethod);
		Map<String, Integer> localVars = getMethodVariableOffsets(currentClass, currentMethod);
		Map<String, Integer> instanceVars = getInstanceVariableOffsets(currentClass);

		if (paramOffsets.containsKey(name)) {
			int offset = 4 * (1 + paramOffsets.size() - paramOffsets.get(name));
			code.add("    # parameter " + name);
			code.add("    movl %eax, " + offset + "(%ebp)");

		} else if (localVars.containsKey(name)) {
			code.add("    # local var " + name);
			code.add("    movl %eax, " + (-4 * (1 + localVars.get(name))) + "(%ebp)");
		} else if (instanceVars.containsKey(name)) {
			code.add("    # instance var " + name);
			code.add("    movl %eax, " + (4 * instanceVars.get(name)) + "(%ecx)");
		} else {
			System.err.println("SHOULD NOT HAVE GOTTEN HERE!!");
		}
	}

	public void visit(ArrayAssign arrayAssign) {
		arrayAssign.exp2.accept(this);
		code.add("    pushl %eax");

		arrayAssign.exp1.accept(this);
		code.add("    pushl %eax");

		String name = arrayAssign.identifier.name;

		Map<String, Integer> paramOffsets = getMethodParameterOffsets(currentClass, currentMethod);
		Map<String, Integer> localVars = getMethodVariableOffsets(currentClass, currentMethod);
		Map<String, Integer> instanceVars = getInstanceVariableOffsets(currentClass);

		code.add("    popl %edx");
		code.add("    popl %eax");
		if (paramOffsets.containsKey(name)) {
			int offset = 4 * (1 + paramOffsets.size() - paramOffsets.get(name));
			code.add("    # parameter " + name);
			code.add("    movl " + offset + "(%ebp), %ecx");

		} else if (localVars.containsKey(name)) {
			code.add("    # local var " + name);
			code.add("    movl " + (-4 * (1 + localVars.get(name))) + "(%ebp), %ecx");

		} else if (instanceVars.containsKey(name)) {
			code.add("    # instance var " + name);
			code.add("    movl " + (4 * instanceVars.get(name)) + "(%ecx), %ecx");
		} else {
			System.err.println("SHOULD NOT HAVE GOTTEN HERE!!");
		}
		code.add("    shl $2, %edx");
		code.add("    addl %ecx, %edx");
		code.add("    # DAVIDEBUG edx now contains array item addr");
		code.add("    movl %eax, (%edx)");
		code.add("    movl (%esp), %ecx");
	}

	public void visit(And andNode) {
		String labelFalse = getLabel();
		String labelEnd = getLabel();

		andNode.exp1.accept(this);
		code.add("    cmpl $0, %eax");
		code.add("    je " + labelFalse);
		andNode.exp2.accept(this);
		code.add("    cmpl $0, %eax");
		code.add("    je " + labelFalse);
		code.add("    movl $1, %eax");
		code.add("    jmp " + labelEnd);
		code.add(labelFalse + ":");
		code.add("    movl $0, %eax");
		code.add(labelEnd + ":");
	}

	public void visit(LessThan lessThan) {
		String labelTrue = getLabel();
		String labelEnd = getLabel();

		lessThan.exp1.accept(this);
		code.add("    pushl %eax");
		lessThan.exp2.accept(this);
		code.add("    popl %edx");
		code.add("    cmpl %eax, %edx");
		code.add("    jl " + labelTrue);
		code.add("    movl $0, %eax");
		code.add("    jmp " + labelEnd);
		code.add(labelTrue + ":");
		code.add("    movl $1, %eax");
		code.add(labelEnd + ":");
	}

	public void visit(Plus plus) {
		plus.exp1.accept(this);
		code.add("    pushl %eax");
		plus.exp2.accept(this);
		code.add("    popl %edx");
		code.add("    addl %edx, %eax");
	}

	public void visit(Minus minus) {
		minus.exp1.accept(this);
		code.add("    pushl %eax");
		minus.exp2.accept(this);
		code.add("    movl %eax, %edx");
		code.add("    popl %eax");
		code.add("    subl %edx, %eax");
	}

	public void visit(Times times) {
		times.exp1.accept(this);
		code.add("    pushl %eax");
		times.exp2.accept(this);
		code.add("    popl %edx");
		code.add("    imul %edx, %eax");
	}

	public void visit(ArrayLookup arrayLookup) {
		arrayLookup.exp1.accept(this);
		code.add("    pushl %eax");
		arrayLookup.exp2.accept(this);
		code.add("    popl %edx");
		code.add("    shl $2, %eax");
		code.add("    # DAVIDEBUG eax now contains array item addr");
		code.add("    addl %edx, %eax");
		code.add("    movl (%eax), %eax");
	}

	public void visit(ArrayLength arrayLength) {
		arrayLength.exp.accept(this);
		code.add("    movl -4(%eax), %eax");
	}

	public void visit(Call call) {
		// Add parameters to stack
		ExpList params = call.expList;
		for (int i = params.size() - 1; i >= 0; --i) {
			params.elementAt(i).accept(this);
			code.add("    pushl %eax");
		}

		// Get invocant
		call.exp.accept(this);
		code.add("    movl %eax, %ecx");

		String typeOfReturnValue = null;
		Map<String, ClassNode> classes = declaredTypes.getClasses();

		Node retType = null;
		ClassNode classNode;
		Map<String, Node> members;
		MethodNode methodNode;
		ClassWithParentNode classWithParentNode;

		if (classes.containsKey(lastSeenType)) {
			classNode = classes.get(lastSeenType);

			while (classNode != null) {
				members = classNode.getMembers();
				if (members.containsKey(call.identifier.name)) {
					methodNode = (MethodNode) members.get(call.identifier.name);
					retType = methodNode.getReturnType();
					break;

				} else if (classNode instanceof ClassWithParentNode) {
					classWithParentNode = (ClassWithParentNode) classNode;
					classNode = classes.get(classWithParentNode.getParent());

				} else {
					break;
				}
			}

			if (retType instanceof ClassNode) {
				typeOfReturnValue = ((ClassNode)retType).getName();
			}

		}

		Map<String, Integer> clsVTable = this.vTable.get(lastSeenType);
		int slotNumber = (Integer)clsVTable.get(call.identifier.name);

		code.add("    movl (%eax), %eax");
		code.add("    addl $" + (slotNumber * 4) + ", %eax");
		code.add("    movl (%eax), %eax");
		code.add("    call *%eax");

		code.add("    addl $" + (4 * params.size()) + ", %esp");
		code.add("    movl (%esp), %ecx");

		if (typeOfReturnValue != null) {
			lastSeenType = typeOfReturnValue;
		}
	}

	public void visit(IntegerLiteral integerLiteral) {
		code.add("    movl $" + integerLiteral.value + ", %eax");
	}

	public void visit(True trueNode) {
		code.add("    movl $1, %eax");
	}

	public void visit(False falseNode) {
		code.add("    movl $0, %eax");
	}

	public void visit(IdentifierExp identifierExp) {
		String name = identifierExp.name;

		Map<String, Integer> paramOffsets = getMethodParameterOffsets(currentClass, currentMethod);
		Map<String, Integer> localVars = getMethodVariableOffsets(currentClass, currentMethod);
		Map<String, Integer> instanceVars = getInstanceVariableOffsets(currentClass);

		Map<String, ClassNode> classNodeMap = declaredTypes.getClasses();
		ClassNode classNode = classNodeMap.get(currentClass);
		MethodNode method = (MethodNode)(classNode.getMembers().get(currentMethod));

		Node node;
		ClassWithParentNode classWithParentNode;

		if (paramOffsets.containsKey(name)) {
			int offset = 4 * (1 + paramOffsets.size() - paramOffsets.get(name));
			code.add("    # parameter " + name);
			code.add("    movl " + offset + "(%ebp), %eax");

			node = method.getParameters().get(paramOffsets.get(name));
			lastSeenType = node.iam;

		} else if (localVars.containsKey(name)) {
			code.add("    # local var " + name);
			code.add("    movl " + (-4 * (1 + localVars.get(name))) + "(%ebp), %eax");

			node = method.getLocalVariables().get(name);
			lastSeenType = node.iam;

		} else if (instanceVars.containsKey(name)) {
			code.add("    # instance var " + name);
			code.add("    movl " + (4 * instanceVars.get(name)) + "(%ecx), %eax");

			while (true) {
				for (Map.Entry<String, Node> entry : classNode.getMembers().entrySet()) {
					node = entry.getValue();
					if (entry.getKey().equals(name) && node.getType() == NodeType.CLASS) {
						lastSeenType = node.iam;
						break;
					}
				}

				if (classNode instanceof ClassWithParentNode) {
					classWithParentNode = (ClassWithParentNode) classNode;
					classNode = classNodeMap.get(classWithParentNode.getParent());
				} else {
					break;
				}
			}
		} else {
			System.err.println("SHOULD NOT HAVE GOTTEN HERE!!");
		}
	}

	public void visit(This thisNode) {
		code.add("    movl %ecx, %eax");
		lastSeenType = currentClass;
	}

	public void visit(NewArray newArray) {
		newArray.exp.accept(this);
		code.add("    pushl %eax");
		code.add("    addl $1, %eax");
		code.add("    shl $2, %eax");
		code.add("    pushl %eax");
		code.add("    call mjmalloc");
		code.add("    addl $4, %esp");
		code.add("    popl %edx");
		code.add("    movl %edx, (%eax)");
		code.add("    movl (%esp), %ecx");
		code.add("    addl $4, %eax");
	}

	public void visit(NewObject newObject) {
		int objectSize = 4; // Space for vtable pointer

		Map<String, ClassNode> classNodeMap = declaredTypes.getClasses();
		ClassNode classNode = classNodeMap.get(newObject.identifier.name);
		ClassWithParentNode classWithParentNode;
		while (true) {
			for (Node member : classNode.getMembers().values()) {
				if (!(member instanceof MethodNode)) {
					objectSize += 4;
				}
			}

			if (classNode instanceof ClassWithParentNode) {
				classWithParentNode = (ClassWithParentNode) classNode;
				classNode = classNodeMap.get(classWithParentNode.getParent());
			} else {
				break;
			}
		}

		code.add("    pushl $" + objectSize);
		code.add("    call mjmalloc");
		code.add("    addl $4, %esp");
		code.add("    movl (%esp), %ecx");
		code.add("    leal " + newObject.identifier.name + "$$, %ebx");
		code.add("    movl %ebx, (%eax)");

		lastSeenType = newObject.identifier.name;
	}

	public void visit(Not not) {
		String labelTrue = getLabel();
		String labelEnd = getLabel();

		not.exp.accept(this);
		code.add("    cmpl $0, %eax");
		code.add("    je " + labelTrue);
		code.add("    movl $0, %eax");
		code.add("    jmp " + labelEnd);
		code.add(labelTrue + ":");
		code.add("    movl $1, %eax");
		code.add(labelEnd + ":");
	}

	public void visit(GreatThan greatThan) {
		String labelTrue = getLabel();
		String labelEnd = getLabel();

		greatThan.exp1.accept(this);
		code.add("    pushl %eax");
		greatThan.exp2.accept(this);
		code.add("    popl %edx");
		code.add("    cmpl %eax, %edx");
		code.add("    jg " + labelTrue);
		code.add("    movl $0, %eax");
		code.add("    jmp " + labelEnd);
		code.add(labelTrue + ":");
		code.add("    movl $1, %eax");
		code.add(labelEnd + ":");

	}

	public void visit(GreatThanEqual greatThanEqual) {
		String labelTrue = getLabel();
		String labelEnd = getLabel();

		greatThanEqual.exp1.accept(this);
		code.add("    pushl %eax");
		greatThanEqual.exp2.accept(this);
		code.add("    popl %edx");
		code.add("    cmpl %eax, %edx");
		code.add("    jge " + labelTrue);
		code.add("    movl $0, %eax");
		code.add("    jmp " + labelEnd);
		code.add(labelTrue + ":");
		code.add("    movl $1, %eax");
		code.add(labelEnd + ":");

	}

	public void visit(LessThanEqual lessThanEqual) {
		String labelTrue = getLabel();
		String labelEnd = getLabel();

		lessThanEqual.exp1.accept(this);
		code.add("    pushl %eax");
		lessThanEqual.exp2.accept(this);
		code.add("    popl %edx");
		code.add("    cmpl %eax, %edx");
		code.add("    jle " + labelTrue);
		code.add("    movl $0, %eax");
		code.add("    jmp " + labelEnd);
		code.add(labelTrue + ":");
		code.add("    movl $1, %eax");
		code.add(labelEnd + ":");
	}

	public void visit(EqualEqual equalEqual) {
		String labelTrue = getLabel();
		String labelEnd = getLabel();

		equalEqual.exp1.accept(this);
		code.add("    pushl %eax");
		equalEqual.exp2.accept(this);
		code.add("    popl %edx");
		code.add("    cmpl %eax, %edx");
		code.add("    je " + labelTrue);
		code.add("    movl $0, %eax");
		code.add("    jmp " + labelEnd);
		code.add(labelTrue + ":");
		code.add("    movl $1, %eax");
		code.add(labelEnd + ":");
	}

	public void visit(NotEqual notEqual) {
		String labelTrue = getLabel();
		String labelEnd = getLabel();

		notEqual.exp1.accept(this);
		code.add("    pushl %eax");
		notEqual.exp2.accept(this);
		code.add("    popl %edx");
		code.add("    cmpl %eax, %edx");
		code.add("    jne " + labelTrue);
		code.add("    movl $0, %eax");
		code.add("    jmp " + labelEnd);
		code.add(labelTrue + ":");
		code.add("    movl $1, %eax");
		code.add(labelEnd + ":");

	}

	public void visit(Formal n) { }

	public void visit(IntArrayType n) { }

	public void visit(BooleanType n) { }

	public void visit(IntegerType n) { }

	public void visit(IdentifierType n) { }

	public void visit(VarDecl n) { }

	public void visit(Identifier n) { }

	public void visit(FloatLiteral n) { }

	public void visit(FloatType n) { }

	public void visit(DoubleLiteral n) { }

	public void visit(DoubleType n) { }

	public void visit(Null n) {}

	public void visit(InstanceOf node) { }
}

