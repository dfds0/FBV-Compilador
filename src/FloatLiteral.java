package AST;
import AST.Visitor.Visitor;

public class FloatLiteral extends Exp {

	public int value;

	public FloatLiteral(int value, int lineNumber) {
		super(lineNumber);
		this.value = value;
	}

	@Override
	public void accept(Visitor visitor) {
		visitor.visit(this);
	}

}
