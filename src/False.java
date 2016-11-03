package AST;
import AST.Visitor.Visitor;

public class False extends Exp {

	public False(int lineNumber) {
		super(lineNumber);
	}

	@Override
	public void accept(Visitor visitor) {
		visitor.visit(this);
	}
}
