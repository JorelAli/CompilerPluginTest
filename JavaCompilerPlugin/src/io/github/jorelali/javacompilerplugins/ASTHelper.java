package io.github.jorelali.javacompilerplugins;

import java.util.Arrays;
import java.util.Stack;
import java.util.stream.Collectors;

import com.sun.source.tree.MethodTree;
import com.sun.tools.javac.code.TypeTag;
import com.sun.tools.javac.tree.JCTree.JCExpression;
import com.sun.tools.javac.tree.JCTree.JCExpressionStatement;
import com.sun.tools.javac.tree.JCTree.JCMethodDecl;
import com.sun.tools.javac.tree.JCTree.JCMethodInvocation;
import com.sun.tools.javac.tree.JCTree.JCVariableDecl;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.Names;

public class ASTHelper {

	/**
	 * Gets a MethodTree's current position
	 * @return The current position of a Method Tree
	 */
	private static int methodPosition(MethodTree methodTree) {
		return ((JCMethodDecl) methodTree).pos;
	}
	
	/**
	 * Creates a new TreeMaker, given a MethodTree. Sorts out the methodPosition.
	 */
	public static TreeMaker createTreeMaker(Context context, MethodTree methodTree) {
		return TreeMaker.instance(context).at(methodPosition(methodTree));
	}
	
	/**
	 * Creates a local variable, that is a primitive type
	 */
	public static JCVariableDecl createLocalPrimitiveVariable(TreeMaker maker, Names names, String name, TypeTag type, JCExpression initValue) {
		return maker.VarDef(maker.Modifiers(0), names.fromString(name), maker.TypeIdent(type), initValue);
	}

	/**
	 * Adds an exception to the list of thrown exceptions.
	 * <br><br>
	 * <code>public void myMethod() {
	 * <br>//code<br>
	 * }</code><br><br>Gets turned into
	 * <br><br>
	 * <code>public void myMethod() throws Exception {
	 * <br>//code<br>
	 * }</code>
	 */
	public static void addExceptionToMethodDeclaredThrows(TreeMaker maker, Names names, MethodTree methodTree, Class<? extends Exception> exception) {
		JCMethodDecl thisMethod = (JCMethodDecl) methodTree;
		thisMethod.thrown = thisMethod.thrown.append(ASTHelper.resolveName(maker, names, exception.getName()));
	}
	
	/**
	 * Assigns a value to a variable
	 */
	public static JCExpressionStatement createAssignment(TreeMaker maker, JCVariableDecl variable, JCExpression newValue) {
		return maker.Exec(maker.Assign(maker.Ident(variable.name), newValue));
	}
	
	//maker.Select(maker.Select(maker.Ident(NAME[java]), NAME[lang]), NAME[String]).
	/**
	 * Resolves the JCExpression from a given name.
	 * @param maker
	 * @param names
	 * @param path A valid path, for example java.lang.reflect.Modifier
	 * @return
	 */
	public static JCExpression resolveName(TreeMaker maker, Names names, String path) {
		String[] identNames = path.split("\\.");
		Stack<String> identNamesStack = Arrays.stream(identNames).collect(Collectors.toCollection(Stack::new));
		
		switch(identNames.length) {
			case 0:
				throw new RuntimeException("Invalid name splitting thingy");
			case 1:
				return maker.Ident(names.fromString(identNames[0]));
			case 2:
				return maker.Select(maker.Ident(names.fromString(identNames[0])), names.fromString(identNames[1]));
			default:
				String topElement = identNamesStack.pop();
				return maker.Select(resolveName(maker, names, String.join(".", identNamesStack)), names.fromString(topElement));
		}
	}
	
	/**
	 * Creates a System.out.println("STRING") statement
	 */
	public static JCExpressionStatement createSysout(TreeMaker maker, Names names, String string) {
		JCMethodInvocation m = maker.Apply(List.nil(), ASTHelper.resolveName(maker, names, "java.lang.System.out.println"), List.of(maker.Literal(string)));
		return maker.Exec(m);
	}
	
	// Arg 1 is the enclosing class, but we won't instantiate an inner class.
	// Arg 2 is a list of type parameters (of the enclosing class).
	// Arg 3 is the actual class expression.
	// Arg 4 is a list of arguments to pass to the constructor.
	// Arg 5 is a class body, for creating an anonymous class.
	public static JCExpressionStatement newClassInstanceWithNoParamsOrGenerics(TreeMaker maker, Names names, String fullClassNameWithPath) {
		return maker.Exec(maker.NewClass(null, List.nil(), resolveName(maker, names, fullClassNameWithPath), List.nil(), null));
	}
	
}
