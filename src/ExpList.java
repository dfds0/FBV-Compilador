package AST;

import java.util.Vector;

public class ExpList extends ASTNode {
	
   private Vector<Exp> vector;

   public ExpList(int ln) {
      super(ln);
      vector = new Vector<Exp>();
   }

   public void addElement(Exp node) {
      this.vector.addElement(node);
   }

   public Exp elementAt(int index)  { 
      return this.vector.elementAt(index); 
   }

   public int size() { 
      return this.vector.size(); 
   }
}
