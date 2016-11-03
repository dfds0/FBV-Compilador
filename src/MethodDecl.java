package AST;

import AST.Visitor.Visitor;

public class MethodDecl extends ASTNode {

	public Type type;
	public Identifier identifier;
	public FormalList formalList;
	public VarDeclList varDeclList;
	public StatementList statementList;
	public Exp exp;

	public MethodDecl(Type type, Identifier identifier, FormalList formalList, VarDeclList varDeclList, 
			StatementList statementList, Exp exp, int lineNumber) {
		super(lineNumber);
		this.type = type;
		this.identifier = identifier;
		this.formalList = formalList;
		this.varDeclList = varDeclList;
		this.statementList = statementList;
		this.exp = exp;
	}

	public void accept(Visitor visitor) {
		visitor.visit(this);
	}

}
