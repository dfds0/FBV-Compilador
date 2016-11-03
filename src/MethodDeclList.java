package AST;

import java.util.Vector;

public class MethodDeclList extends ASTNode {

	private Vector<MethodDecl> vector;

	public MethodDeclList(int lineNumber) {
		super(lineNumber);
		vector = new Vector<MethodDecl>();
	}

	public void addElement(MethodDecl node) {
		this.vector.addElement(node);
	}

	public MethodDecl elementAt(int index)  { 
		return this.vector.elementAt(index); 
	}

	public int size() { 
		return this.vector.size(); 
	}
	
}
