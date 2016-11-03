package AST;
import AST.Visitor.Visitor;

public class IdentifierExp extends Exp {

	public String name;

	public IdentifierExp(String name, int lineNumber) { 
		super(lineNumber);
		this.name = name;
	}

	@Override
	public void accept(Visitor visitor) {
		visitor.visit(this);
	}
}
