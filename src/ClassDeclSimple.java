package AST;
import AST.Visitor.Visitor;

public class ClassDeclSimple extends ClassDecl {

	public Identifier identifier;
	public VarDeclList varDeclList;  
	public MethodDeclList methodDeclList;

	public ClassDeclSimple(Identifier identifier, VarDeclList varDeclList, 
			MethodDeclList methodDeclList, int lineNumber) {
		super(lineNumber);
		this.identifier = identifier;
		this.varDeclList = varDeclList;
		this.methodDeclList = methodDeclList;
	}

	@Override
	public void accept(Visitor visitor) {
		visitor.visit(this);
	}

}
