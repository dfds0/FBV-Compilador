package AST;

import java.util.Vector;

public class StatementList extends ASTNode {

	private Vector<Statement> vector;

	public StatementList(int lineNumber) {
		super(lineNumber);
		this.vector = new Vector<Statement>();
	}

	public void addElement(Statement node) {
		// Pilha
		vector.add(0, node);
	}

	public Statement elementAt(int index)  { 
		return this.vector.elementAt(index); 
	}

	public int size() { 
		return this.vector.size(); 
	}

}
