package AST;

import java.util.Vector;

import AST.Visitor.Visitor;

public class ClassDeclList extends ASTNode {
	
   private Vector<ClassDecl> vector;

   public ClassDeclList(int lineNumber) {
      super(lineNumber);
      this.vector = new Vector<ClassDecl>();
   }

   public void addElement(ClassDecl node) {
      this.vector.addElement(node);
   }

   public ClassDecl elementAt(int index)  { 
      return vector.elementAt(index); 
   }

   public int size() { 
      return vector.size(); 
   }
   
}
