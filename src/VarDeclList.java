package AST;

import java.util.Vector;

public class VarDeclList extends ASTNode {

	private Vector<VarDecl> vector;

	public VarDeclList(int lineNumber) {
		super(lineNumber);
		this.vector = new Vector<VarDecl>();
	}

	public void addElement(VarDecl node) {
		this.vector.addElement(node);
	}

	public VarDecl elementAt(int index)  { 
		return this.vector.elementAt(index); 
	}

	public int size() { 
		return this.vector.size(); 
	}

}
