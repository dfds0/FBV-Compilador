package AST;

import AST.Visitor.Visitor;

public class FloatType extends Type {

	public FloatType(int lineNumber) {
		super(lineNumber);
	}

	@Override
	public void accept(Visitor visitor) {
		visitor.visit(this);
	}

}
