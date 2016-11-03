package AST;
import AST.Visitor.Visitor;

public class Call extends Exp {

	public Exp exp;
	public Identifier identifier;
	public ExpList expList;

	public Call(Exp exp, Identifier identifier, ExpList expList, int lineNumber) {
		super(lineNumber);
		this.exp = exp;
		this.identifier = identifier;
		this.expList = expList;
	}

	@Override
	public void accept(Visitor visitor) {
		visitor.visit(this);
	}

}
