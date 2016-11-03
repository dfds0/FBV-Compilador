package AST;
import AST.Visitor.Visitor;

public class IntegerLiteral extends Exp {

	public int value;

	public IntegerLiteral(int value, int lineNumber) {
		super(lineNumber);
		this.value = value;
	}

	@Override
	public void accept(Visitor visitor) {
		visitor.visit(this);
	}

}
