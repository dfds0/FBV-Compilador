package AST;
import AST.Visitor.Visitor;

public class Not extends Exp {

	public Exp exp;

	public Not(Exp exp, int lineNumber) {
		super(lineNumber);
		this.exp = exp; 
	}

	@Override
	public void accept(Visitor visitor) {
		visitor.visit(this);
	}

}
