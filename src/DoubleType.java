package AST;

import AST.Visitor.Visitor;

public class DoubleType extends Type {
	
	public DoubleType(int lineNumber) {
		super(lineNumber);
	}

	@Override
	public void accept(Visitor visitor) {
		visitor.visit(this);
	}
	
}
