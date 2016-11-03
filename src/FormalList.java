package AST;

import java.util.Vector;

public class FormalList extends ASTNode {

	private Vector<Formal> vector;

	public FormalList(int lineNumber) {
		super(lineNumber);
		vector = new Vector<Formal>();
	}

	public void addElement(Formal node) {
		vector.add(0, node);
	}

	public Formal elementAt(int index)  { 
		return this.vector.elementAt(index); 
	}

	public int size() { 
		return this.vector.size(); 
	}

}
