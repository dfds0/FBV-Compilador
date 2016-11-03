package AST;
import AST.Visitor.Visitor;

public class If extends Statement {

	public Exp exp;
	public Statement statement1;
	public Statement statement2;

	public If(Exp exp, Statement statement1, Statement statement2, int lineNumber) {
		super(lineNumber);
		this.exp = exp;
		this.statement1 = statement1;
		this.statement2 = statement2;
	}

	@Override
	public void accept(Visitor visitor) {
		visitor.visit(this);
	}

}

