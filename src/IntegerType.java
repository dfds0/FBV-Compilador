package AST;
import AST.Visitor.Visitor;

public class IntegerType extends Type {

	public IntegerType(int lineNumber) {
		super(lineNumber);
	}

	@Override
	public void accept(Visitor visitor) {
		visitor.visit(this);
	}

}
