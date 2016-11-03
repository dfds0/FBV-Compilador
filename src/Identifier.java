package AST;

import AST.Visitor.Visitor;

public class Identifier extends ASTNode {

	public String name;

	public Identifier(String name, int lineNumber) { 
		super(lineNumber);
		this.name = name;
	}

	public String toString(){
		return name;
	}
	
	public void accept(Visitor visitor) {
		visitor.visit(this);
	}

}
