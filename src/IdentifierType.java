package AST;
import AST.Visitor.Visitor;

public class IdentifierType extends Type {

	public String name;

	public IdentifierType(String name, int lineNumber) {
		super(lineNumber);
		this.name = name;
	}

	@Override
	public void accept(Visitor visitor) {
		visitor.visit(this);
	}

}
