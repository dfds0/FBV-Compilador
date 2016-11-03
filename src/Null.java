package AST;
import AST.Visitor.Visitor;

public class Null extends Exp {

	public Null(int lineNumber) {
		super(lineNumber);
	}

	@Override
	public void accept(Visitor visitor) {
		visitor.visit(this);
	}

}
