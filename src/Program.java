package AST;

public class Program extends ASTNode {

	public MainClass mainClass;
	public ClassDeclList classDeclList;

	//TODO: Accept?
	public Program(MainClass mainClass, ClassDeclList classDeclList, int lineNumber) {
		super(lineNumber);
		this.mainClass = mainClass;
		this.classDeclList = classDeclList;
	}

}
