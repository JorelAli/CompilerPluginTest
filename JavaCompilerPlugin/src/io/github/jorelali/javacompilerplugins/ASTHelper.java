package io.github.jorelali.javacompilerplugins;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;
import java.util.stream.Collectors;

import com.sun.source.tree.AnnotationTree;
import com.sun.source.tree.MethodTree;
import com.sun.tools.javac.code.TypeTag;
import com.sun.tools.javac.tree.JCTree.JCAssign;
import com.sun.tools.javac.tree.JCTree.JCExpression;
import com.sun.tools.javac.tree.JCTree.JCExpressionStatement;
import com.sun.tools.javac.tree.JCTree.JCMethodDecl;
import com.sun.tools.javac.tree.JCTree.JCVariableDecl;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.Name;
import com.sun.tools.javac.util.Names;

public class ASTHelper {

	/**
	 * Parses Annotations into a Map<String, String>
	 * @param annotation The annotation tree to parse
	 * @return a mapping of the "variable names" to the "variable values" (as Strings)
	 */
	public static Map<String, String> parseAnnotation(AnnotationTree annotation) {
		Map<String, String> map = new HashMap<>();
		annotation.getArguments().forEach(expression -> {
			JCAssign assignment = (JCAssign) expression;
			map.put(assignment.lhs.toString(), assignment.rhs.toString());
		});
		return map;
	}
	
	public static Name makeNameDirty(String name) {
		return new Name(null) {

			@Override
			public char charAt(int index) {
				return name.charAt(index);
			}

			@Override
			public int length() {
				return name.length();
			}

			@Override
			public CharSequence subSequence(int start, int end) {
				return name.substring(start, end);
			}

			@Override
			public boolean contentEquals(CharSequence cs) {
				return name.contentEquals(cs);
			}

			@Override
			public int getIndex() {
				return 0;
			}

			@Override
			public int getByteLength() {
				return length();
			}

			@Override
			public byte getByteAt(int var1) {
				return getByteArray()[var1];
			}

			@Override
			public byte[] getByteArray() {
				return name.getBytes();
			}

			@Override
			public int getByteOffset() {
				return 0;
			}

		};
	}
	
	public static int methodPosition(MethodTree methodTree) {
		return ((JCMethodDecl) methodTree).pos;
	}
	
	public static TreeMaker createTreeMaker(Context context, MethodTree methodTree) {
		return TreeMaker.instance(context).at(methodPosition(methodTree));
	}
	
	public static JCVariableDecl createLocalPrimitiveVariable(TreeMaker maker, String name, TypeTag type, JCExpression initValue) {
		return maker.VarDef(maker.Modifiers(0), makeNameDirty(name), maker.TypeIdent(type), initValue);
	}

	public static void addExceptionToMethodDeclaredThrows(TreeMaker maker, Names names, MethodTree methodTree, Class<? extends Exception> exception) {
		JCMethodDecl thisMethod = (JCMethodDecl) methodTree;
		thisMethod.thrown = thisMethod.thrown.append(ASTHelper.resolveName(maker, names, exception.getName()));
	}
	
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
	
	// Arg 1 is the enclosing class, but we won't instantiate an inner class.
	// Arg 2 is a list of type parameters (of the enclosing class).
	// Arg 3 is the actual class expression.
	// Arg 4 is a list of arguments to pass to the constructor.
	// Arg 5 is a class body, for creating an anonymous class.
	public static JCExpressionStatement newClassInstanceWithNoParamsOrGenerics(TreeMaker maker, Names names, String fullClassNameWithPath) {
		return maker.Exec(maker.NewClass(null, List.nil(), resolveName(maker, names, fullClassNameWithPath), List.nil(), null));
	}
	
}
