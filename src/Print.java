package AST;
import AST.Visitor.Visitor;

public class Print extends Statement {
	public Exp exp;

	public Print(Exp exp, int lineNumber) {
		super(lineNumber);
		this.exp = exp;
	}

	@Override
	public void accept(Visitor visitor) {
		visitor.visit(this);
	}

}
