package AST;

import AST.Visitor.Visitor;

public class Formal extends ASTNode {

	public Type type;
	public Identifier identifier;

	public Formal(Type type, Identifier identifier, int lineNumber) {
		super(lineNumber);
		this.type = type;
		this.identifier = identifier;
	}

	public void accept(Visitor visitor) {
		visitor.visit(this);
	}
}
