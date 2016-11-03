package AST;
import AST.Visitor.Visitor;

public class NewObject extends Exp {

	public Identifier identifier;

	public NewObject(Identifier identifier, int lineNumber) {
		super(lineNumber);
		this.identifier = identifier;
	}

	@Override
	public void accept(Visitor visitor) {
		visitor.visit(this);
	}
	
}
