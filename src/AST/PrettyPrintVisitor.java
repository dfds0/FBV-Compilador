package AST.Visitor;

import AST.*;

public class PrettyPrintVisitor implements Visitor {
	
	public void visit(Program program) {
		program.mainClass.accept(this);
		for (int i = 0; i < program.classDeclList.size(); i++) {
			program.classDeclList.elementAt(i).accept(this);
		}
	}

	// ------------
	//  Structural 
	// ------------
	
	/**
	 * Print: Main
	 */
	public void visit(MainClass mainClass) {
		System.out.print("class ");
		mainClass.identifier1.accept(this);
		
		System.out.println(" {");
		System.out.print("  public static void main (String [] ");
		mainClass.identifier2.accept(this);
		
		System.out.println(") {");
		System.out.print("    ");
		mainClass.statement.accept(this);
		
		System.out.println("  }");
		System.out.println("}");
	}

	/**
	 * Print: class
	 */
	public void visit(ClassDeclSimple classDeclSimple) {
		System.out.print("class ");
		classDeclSimple.identifier.accept(this);
		System.out.println(" { ");
		for (int i = 0; i < classDeclSimple.varDeclList.size(); i++) {
			System.out.print("  ");
			classDeclSimple.varDeclList.elementAt(i).accept(this);
			if (i + 1 < classDeclSimple.varDeclList.size()) {
				// Print empty
			}
		}
		for (int i = 0; i < classDeclSimple.methodDeclList.size(); i++) {
			classDeclSimple.methodDeclList.elementAt(i).accept(this);
		}
		// Print empty
		System.out.println("}");
	}

	/**
	 * Print: extends
	 */
	public void visit(ClassDeclExtends classDeclExtends) {
		System.out.print("class ");
		classDeclExtends.identifier1.accept(this);
		
		System.out.println(" extends ");
		classDeclExtends.identifier2.accept(this);
		
		System.out.println(" { ");
		for (int i = 0; i < classDeclExtends.varDeclList.size(); i++) {
			System.out.print("  ");
			classDeclExtends.varDeclList.elementAt(i).accept(this);
			if (i + 1 < classDeclExtends.varDeclList.size()) {
				// Print empty
			}
		}
		
		for (int i = 0; i < classDeclExtends.methodDeclList.size(); i++) {
			// Print 
			classDeclExtends.methodDeclList.elementAt(i).accept(this);
		}
		// Print empty
		System.out.println("}");
	}

	/**
	 * Print 1: type identifier; 
	 * Print 2: type identifier = exp;
	 */
	public void visit(VarDecl varDecl) {
		if(varDecl.exp == null){
			varDecl.type.accept(this);
			System.out.print(" ");
			varDecl.identifier.accept(this);
			System.out.print(";");
			
		}else{
			varDecl.type.accept(this);
			System.out.print(" ");
			varDecl.identifier.accept(this);
			System.out.print(" = ");
			varDecl.exp.accept(this);
			System.out.print(";");
		}
	}

	/**
	 * Print: method
	 */
	public void visit(MethodDecl methodDecl) {
		System.out.print("  public ");
		methodDecl.type.accept(this);
		
		System.out.print(" ");
		methodDecl.identifier.accept(this);
		System.out.print(" (");
		
		for (int i = 0; i < methodDecl.formalList.size(); i++) {
			methodDecl.formalList.elementAt(i).accept(this);
			if (i + 1 < methodDecl.formalList.size()) {
				System.out.print(", ");
			}
		}
		
		System.out.println(") { ");
		for (int i = 0; i < methodDecl.varDeclList.size(); i++) {
			System.out.print("    ");
			methodDecl.varDeclList.elementAt(i).accept(this);
			System.out.println("");
		}
		
		for (int i = 0; i < methodDecl.statementList.size(); i++) {
			System.out.print("    ");
			methodDecl.statementList.elementAt(i).accept(this);
			if (i < methodDecl.statementList.size()) {
				System.out.println("");
			}
		}
		
		System.out.print("    return ");
		methodDecl.exp.accept(this);
		System.out.println(";");
		System.out.print("  }");
	}

	/**
	 * Print: type identifier;
	 */
	public void visit(Formal formal) {
		formal.type.accept(this);
		System.out.print(" ");
		formal.identifier.accept(this);
	}

	/**
	 * Print: identifierType
	 */
	public void visit(IdentifierType identifierType) {
		System.out.print(identifierType.name);
	}

	/**
	 * Print: { statement.. } 
	 */
	public void visit(Block block) {
		System.out.println("{ ");
		for (int i = 0; i < block.statementList.size(); i++) {
			System.out.print("      ");
			block.statementList.elementAt(i).accept(this);
			System.out.println();
		}
		System.out.print("    } ");
	}

	/**
	 * Print: if ( exp ) statement1 else statement2
	 */
	public void visit(If ifNode) {
		System.out.print("if (");
		ifNode.exp.accept(this);
		
		System.out.println(") ");
		System.out.print("    ");
		ifNode.statement1.accept(this);
		
		System.out.println();
		System.out.print("    else ");
		ifNode.statement2.accept(this);
	}

	/**
	 * Print: while ( exp ) statement
	 */
	public void visit(While whileNode) {
		System.out.print("while (");
		whileNode.exp.accept(this);
		
		System.out.print(") ");
		whileNode.statement.accept(this);
	}

	/**
	 * Print: print ( exp );
	 */
	public void visit(Print print) {
		System.out.print("System.out.println(");
		print.exp.accept(this);
		System.out.print(");");
	}

	/**
	 * Print: identifier = exp;
	 */
	public void visit(Assign assign) {
		assign.identifier.accept(this);
		System.out.print(" = ");
		
		assign.exp.accept(this);
		System.out.print(";");
	}

	/**
	 * Print: ( exp + exp ) 
	 */
	public void visit(Plus plus) {
		System.out.print("(");
		plus.exp1.accept(this);
		
		System.out.print(" + ");
		plus.exp2.accept(this);
		System.out.print(")");
	}

	/**
	 * Print: ( exp - exp )
	 */
	public void visit(Minus minus) {
		System.out.print("(");
		minus.exp1.accept(this);
		
		System.out.print(" - ");
		minus.exp2.accept(this);
		System.out.print(")");
	}

	/**
	 * Print: ( exp * exp )
	 */
	public void visit(Times times) {
		System.out.print("(");
		times.exp1.accept(this);
		
		System.out.print(" * ");
		times.exp2.accept(this);
		System.out.print(")");
	}

	/**
	 * Print: exp.identifier(arg1, arg2.., argN) 
	 */
	public void visit(Call call) {
		call.exp.accept(this);
		System.out.print(".");
		
		call.identifier.accept(this);
		System.out.print("(");
		
		for (int i = 0; i < call.expList.size(); i++) {
			call.expList.elementAt(i).accept(this);
			if (i + 1 < call.expList.size()) {
				System.out.print(", ");
			}
		}
		System.out.print(")");
	}

	// -------------
	//  Logical 
	// -------------
	
	/**
	 * Print: ( exp1 > exp2 )
	 */
	public void visit(GreatThan greatThan) {
		System.out.print("(");
		greatThan.exp1.accept(this);
		
		System.out.print(" > ");
		greatThan.exp2.accept(this);
		System.out.print(")");
	}

	/**
	 * Print: ( exp1 >= exp2 )
	 */
	public void visit(GreatThanEqual greatThanEqual) {
		System.out.print("(");
		greatThanEqual.exp1.accept(this);
		
		System.out.print(" >= ");
		greatThanEqual.exp2.accept(this);
		System.out.print(")");
	}

	/**
	 * Print: ( exp1 == exp2 )
	 */
	public void visit(EqualEqual equalEqual) {
		System.out.print("(");
		equalEqual.exp1.accept(this);
		
		System.out.print(" == ");
		equalEqual.exp2.accept(this);
		System.out.print(")");
	}

	/**
	 * Print: ( exp1 != exp2 )
	 */
	public void visit(NotEqual notEqual) {
		System.out.print("(");
		notEqual.exp1.accept(this);
		
		System.out.print(" != ");
		notEqual.exp2.accept(this);
		System.out.print(")");
	}
	
	/**
	 * Print: ( exp1 instanceof exp2 )
	 */
	public void visit(InstanceOf instanceOf) {
		System.out.print("(");
		instanceOf.exp1.accept(this);
		
		System.out.print(" instanceof ");
		instanceOf.exp2.accept(this);
		System.out.print(")");
	}
	
	/**
	 * Print: ( exp1 <= exp2 )
	 */
	public void visit(LessThanEqual lessThanEqual) {
		System.out.print("(");
		lessThanEqual.exp1.accept(this);
		
	    System.out.print(" <= ");
	    lessThanEqual.exp2.accept(this);
	    System.out.print(")");
	}

	/**
	 * Print: ( exp && exp )
	 */
	public void visit(And and) {
		System.out.print("(");
		and.exp1.accept(this);
		
		System.out.print(" && ");
		and.exp2.accept(this);
		System.out.print(")");
	}

	/**
	 * Print: ( exp < exp )
	 */
	public void visit(LessThan lessThan) {
		System.out.print("(");
		lessThan.exp1.accept(this);
		
		System.out.print(" < ");
		lessThan.exp2.accept(this);
		System.out.print(")");
	}


	// --------------
	//  Data
	// --------------
	
	/**
	 * Print: null
	 */
	public void visit(Null nullNode) {
		System.out.print("null");		
	}
	
	/**
	 * Print: boolean
	 */
	public void visit(BooleanType booleanType) {
		System.out.print("boolean");
	}

	/**
	 * Print: int
	 */
	public void visit(IntegerType integerType) {
		System.out.print("int");
	}
	
	/**
	 * Print: !
	 */
	public void visit(Not notNode) {
		System.out.print("!");
		notNode.exp.accept(this);
	}

	/**
	 * Print: name
	 */
	public void visit(Identifier identifier) {
		System.out.print(identifier.name);
	}

	/**
	 * Print: 0.0f
	 */
	public void visit(FloatLiteral floatLiteral) {
		System.out.print(floatLiteral.value);
	}

	/**
	 * Print: float
	 */
	public void visit(FloatType floatType) {
		System.out.print("float");
	}
	
	/**
	 * Print 0.0d
	 */
	public void visit(DoubleLiteral doubleLiteral) {
		System.out.print(doubleLiteral.value);
	}

	/**
	 * Print: double
	 */
	public void visit(DoubleType doubleType) {
		System.out.print("double");
	}
	
	/**
	 * Print: value 
	 */
	public void visit(IntegerLiteral intergerLiteral) {
		System.out.print(intergerLiteral.value);
	}

	/**
	 * Print: true
	 */
	public void visit(True trueNode) {
		System.out.print("true");
	}

	/**
	 * Print: false
	 */
	public void visit(False falseNode) {
		System.out.print("false");
	}

	/**
	 * Print: 'name'
	 */
	public void visit(IdentifierExp identifierExp) {
		System.out.print(identifierExp.name);
	}

	/**
	 * Print: this
	 */
	public void visit(This thisNode) {
		System.out.print("this");
	}
	
	// --------------
	//  Object & Array 
	// --------------

	/**
	 * Print: new int [ exp ]
	 */
	public void visit(NewArray newArray) {
		System.out.print("new int [");
		newArray.exp.accept(this);
		System.out.print("]");
	}

	/**
	 * Print: int []
	 */
	public void visit(IntArrayType intArrayType) {
		System.out.print("int []");
	}
	
	/**
	 * Print: ind [ exp ] = exp;
	 */
	public void visit(ArrayAssign arrayAssign) {
		arrayAssign.identifier.accept(this);
		System.out.print("[");
		
		arrayAssign.exp1.accept(this);
		System.out.print("] = ");
		
		arrayAssign.exp2.accept(this);
		System.out.print(";");
	}
	
	/**
	 * Print: exp1[ exp2 ]
	 */
	public void visit(ArrayLookup arrayLookup) {
		arrayLookup.exp1.accept(this);
		System.out.print("[");
		
		arrayLookup.exp2.accept(this);
		System.out.print("]");
	}

	/**
	 * Print: exp.length
	 */
	public void visit(ArrayLength arrayLength) {
		arrayLength.exp.accept(this);
		System.out.print(".length");
	}
	
	/**
	 * Print: new Name()
	 */
	public void visit(NewObject newObject) {
		System.out.print("new ");
		System.out.print(newObject.identifier.name);
		System.out.print("()");
	}
	
}
