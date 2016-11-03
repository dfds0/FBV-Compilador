package AST;
import AST.Visitor.Visitor;

public class Block extends Statement {

	public StatementList statementList;

	public Block(StatementList statementList, int lineNumber) {
		super(lineNumber);
		this.statementList = statementList;
	}

	@Override
	public void accept(Visitor visitor) {
		visitor.visit(this);
	}

}

