package AST.Visitor;

import AST.*;

public interface Visitor {

	// Estrutural
	public void visit(Program node);
	public void visit(MainClass node);
	public void visit(ClassDeclSimple node);
	public void visit(ClassDeclExtends node);
	public void visit(VarDecl node);
	public void visit(MethodDecl node);
	public void visit(Formal node);
	public void visit(Block node);

	// Declaração
	public void visit(IntArrayType node);
	public void visit(BooleanType node);
	public void visit(IntegerType node);
	public void visit(IdentifierType node);
	public void visit(Null node);

	// Operacional basico
	public void visit(Print node);
	public void visit(Call node);
	public void visit(Assign node);
	public void visit(ArrayAssign node);
	public void visit(ArrayLookup node);
	public void visit(ArrayLength node);
	
	// Operacional objeto
	public void visit(IdentifierExp node);
	public void visit(This node);
	public void visit(NewArray node);
	public void visit(NewObject node);
	public void visit(InstanceOf node);
	
	// Matematico
	public void visit(Plus node);
	public void visit(Minus node);
	
	// Loop
	public void visit(Times node);
	public void visit(While node);
	
	// Logico
	public void visit(If node);
	public void visit(EqualEqual node);
	public void visit(NotEqual node);
	public void visit(True node);
	public void visit(False node);
	public void visit(Not node);
	public void visit(And node);
	public void visit(LessThan node);
	public void visit(GreatThan node);
	public void visit(GreatThanEqual node);

	// Valor 
	public void visit(Identifier node);
	public void visit(IntegerLiteral node);
	public void visit(FloatLiteral node);
	public void visit(FloatType node);
	public void visit(LessThanEqual node);
	public void visit(DoubleLiteral node);
	public void visit(DoubleType node);

}
