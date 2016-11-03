package AST;

import AST.Visitor.Visitor;

public class MainClass extends ASTNode {

	public Identifier identifier1;
	public Identifier identifier2;
	public Statement statement;

	public MainClass(Identifier identifier1, Identifier identifier2, 
			Statement statement, int lineNumber) {
		super(lineNumber);
		this.identifier1 = identifier1;
		this.identifier2 = identifier2;
		this.statement = statement;
	}

	public void accept(Visitor visitor) {
		visitor.visit(this);
	}

}

