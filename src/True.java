package AST;
import AST.Visitor.Visitor;

public class True extends Exp {

	public True(int lineNumber) {
		super(lineNumber);
	}

	@Override
	public void accept(Visitor visitor) {
		visitor.visit(this);
	}
	
}
