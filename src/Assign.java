package AST;
import AST.Visitor.Visitor;

public class Assign extends Statement {

	public Identifier identifier;
	public Exp exp;

	public Assign(Identifier identifier, Exp exp, int lineNumber) {
		super(lineNumber);
		this.identifier = identifier;
		this.exp = exp;
	}

	@Override
	public void accept(Visitor visitor) {
		visitor.visit(this);
	}

}

