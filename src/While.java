package AST;
import AST.Visitor.Visitor;

public class While extends Statement {

	public Exp exp;
	public Statement statement;

	public While(Exp exp, Statement statement, int lineNumber) {
		super(lineNumber);
		this.exp = exp;
		this.statement = statement;
	}

	@Override
	public void accept(Visitor visitor) {
		visitor.visit(this);
	}

}

