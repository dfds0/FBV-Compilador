package AST;
import AST.Visitor.Visitor;

public class NewArray extends Exp {

	public Exp exp;

	public NewArray(Exp exp, int lineNumber) {
		super(lineNumber);
		this.exp = exp;
	}

	@Override
	public void accept(Visitor visitor) {
		visitor.visit(this);
	}

}
