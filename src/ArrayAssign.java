package AST;

import AST.Visitor.Visitor;

public class ArrayAssign extends Statement {

	public Identifier identifier;
	public Exp exp1;
	public Exp exp2;

	public ArrayAssign(Identifier identifier, Exp exp1, Exp exp2, int lineNumber) {
		super(lineNumber);
		this.identifier = identifier; 
		this.exp1 = exp1; 
		this.exp2 = exp2;
	}

	@Override
	public void accept(Visitor visitor) {
		visitor.visit(this);
	}

}

