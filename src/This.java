package AST;
import AST.Visitor.Visitor;

public class This extends Exp {

	public This(int lineNumber) {
		super(lineNumber);
	}

	@Override
	public void accept(Visitor visitor) {
		visitor.visit(this);
	}

}
