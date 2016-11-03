package AST;

import AST.Visitor.Visitor;

public class DoubleLiteral extends Exp {

	public int value;

	public DoubleLiteral(int value, int lineNumber) {
		super(lineNumber);
		this.value = value;
	}

	@Override
	public void accept(Visitor visitor) {
		visitor.visit(this);
	}

}
