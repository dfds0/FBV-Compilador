package AST;

import AST.Visitor.Visitor;

public class VarDecl extends ASTNode {

	public Type type;
	public Identifier identifier;
	public Exp exp;

	public VarDecl(Type type, Identifier identifier, int lineNumber, Exp exp) {
		super(lineNumber);
		this.type = type;
		this.identifier = identifier;
		this.exp = exp;
	}

	public void accept(Visitor visitor) {
		visitor.visit(this);
	}

}
