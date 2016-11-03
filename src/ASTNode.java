package AST;

import AST.Visitor.Visitor;

abstract public class ASTNode {

	// Linha aonde o node foi encontrado
	protected int lineNumber;

	public ASTNode(int lineNumber) {
		this.lineNumber = lineNumber;
	}

	public int getLine() {
		return lineNumber;
	} 
	
}
