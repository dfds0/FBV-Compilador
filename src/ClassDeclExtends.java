package AST;
import AST.Visitor.Visitor;

public class ClassDeclExtends extends ClassDecl {

	public Identifier identifier1;
	public Identifier identifier2;
	public VarDeclList varDeclList;  
	public MethodDeclList methodDeclList;

	public ClassDeclExtends(Identifier identifier1, Identifier identifier2, VarDeclList varDeclList, 
			MethodDeclList methodDeclList, int lineNumber) {
		super(lineNumber);
		this.identifier1 = identifier1;
		this.identifier2 = identifier2;
		this.varDeclList = varDeclList;
		this.methodDeclList = methodDeclList;
	}

	@Override
	public void accept(Visitor visitor) {
		visitor.visit(this);
	}

}
