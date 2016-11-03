package AST;
import AST.Visitor.Visitor;

public class BooleanType extends Type {

	public BooleanType(int lineNumber) {
		super(lineNumber);
	}

	@Override
	public void accept(Visitor visitor) {
		visitor.visit(this);
	}

}
