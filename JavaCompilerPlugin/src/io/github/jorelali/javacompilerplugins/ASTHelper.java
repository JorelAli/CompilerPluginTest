package io.github.jorelali.javacompilerplugins;

import java.util.HashMap;
import java.util.Map;

import com.sun.source.tree.AnnotationTree;
import com.sun.source.tree.MethodTree;
import com.sun.tools.javac.code.TypeTag;
import com.sun.tools.javac.tree.JCTree.JCAssign;
import com.sun.tools.javac.tree.JCTree.JCExpression;
import com.sun.tools.javac.tree.JCTree.JCExpressionStatement;
import com.sun.tools.javac.tree.JCTree.JCFieldAccess;
import com.sun.tools.javac.tree.JCTree.JCMethodDecl;
import com.sun.tools.javac.tree.JCTree.JCVariableDecl;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.Name;

public class ASTHelper {

	public static Map<String, String> parseAnnotation(AnnotationTree annotation) {
		Map<String, String> map = new HashMap<>();
		annotation.getArguments().forEach(expression -> {
			JCAssign assignment = (JCAssign) expression;
			map.put(assignment.lhs.toString(), assignment.rhs.toString());
		});
		return map;
	}
	
	private static Name makeNameDirty(String name) {
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

	public static JCExpressionStatement createAssignment(TreeMaker maker, JCVariableDecl variable, JCExpression newValue) {
		return maker.Exec(maker.Assign(maker.Ident(variable.name), newValue));
	}
	
	//maker.Select(maker.Select(maker.Ident(NAME[java]), NAME[lang]), NAME[String]).
	public static JCExpression a(TreeMaker maker, String path) {
		String[] names = path.split("\\.");
		
		for(int i = 0; i < names.length - 1; i++) {
			
		}
		
		//https://www.programcreek.com/java-api-examples/?code=git03394538/lombok-ianchiu/lombok-ianchiu-master/src/core/lombok/javac/handlers/HandleLog.java#
		
//		maker.Select(names[0], names[1]);, 2, 3, 4...
//		
//		
//		A = aa(maker, name1, name2)
//		
//		aa(maker, A, name3)
		
		return null;
	}
	
	public static JCFieldAccess aa(TreeMaker maker, Name name1, Name name2) {
		return maker.Select(maker.Ident(name1), name2);
		
	}
	
}
