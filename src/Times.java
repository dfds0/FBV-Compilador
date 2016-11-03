package AST;
import AST.Visitor.Visitor;

public class Times extends Exp {

	public Exp exp1;
	public Exp exp2;

	public Times(Exp exp1, Exp exp2, int lineNumber) {
		super(lineNumber);
		this.exp1 = exp1;
		this.exp2 = exp2;
	}

	@Override
	public void accept(Visitor visitor) {
		visitor.visit(this);
	}

}
