package AST;

import AST.Visitor.Visitor;

public class And extends Exp {
	
  public Exp exp1;
  public Exp exp2;
  
  public And(Exp exp1, Exp exp2, int ln) {
    super(ln);
    this.exp1=exp1;
    this.exp2=exp2;
  }

  @Override
  public void accept(Visitor visotor) {
	  visotor.visit(this);
  }

}
