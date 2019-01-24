import javax.lang.model.element.Name;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;

import com.sun.source.tree.BinaryTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.LineMap;
import com.sun.source.tree.MemberSelectTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.Tree.Kind;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.JavacTask;
import com.sun.source.util.SourcePositions;
import com.sun.source.util.TreePath;
import com.sun.source.util.TreePathScanner;
import com.sun.source.util.Trees;
import com.sun.tools.javac.code.Attribute;
import com.sun.tools.javac.code.Symbol.TypeSymbol;
import com.sun.tools.javac.code.Symbol.VarSymbol;
import com.sun.tools.javac.code.TypeTag;
import com.sun.tools.javac.model.JavacElements;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.JCTree.JCBinary;
import com.sun.tools.javac.tree.JCTree.JCBlock;
import com.sun.tools.javac.tree.JCTree.JCExpression;
import com.sun.tools.javac.tree.JCTree.JCLiteral;
import com.sun.tools.javac.tree.JCTree.JCThrow;
import com.sun.tools.javac.tree.JCTree.JCVariableDecl;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.Names;

import sun.tools.java.Type;

public class MyTreeVisitor extends TreePathScanner<Void, Void> {
	
	// offsets of AST nodes in source file
	private final SourcePositions sourcePositions;
	
	// bridges Compiler api, Annotation Processing API and Tree API
	private final Trees trees;
	
	// utility to operate on types
	private final Types types;
	private final TypeMirror mapType;
	private final Name getName;

	private final Context context;
	private final JavacElements elements;
	
	private CompilationUnitTree currCompUnit;

	MyTreeVisitor(JavacTask task, Context context) {
		this.context = context;
		elements = JavacElements.instance(context);
		
		types = task.getTypes();
		trees = Trees.instance(task);
		sourcePositions = trees.getSourcePositions();

		// utility to operate on program elements
		Elements elements = task.getElements();
		
		// create the type element to match against
		mapType = elements.getTypeElement("java.util.Map").asType();
		
		// create a Name object representing the method name to match against
		getName = elements.getName("get");
	}

	@Override
	public Void visitCompilationUnit(CompilationUnitTree tree, Void p) {
		currCompUnit = tree;
		return super.visitCompilationUnit(tree, p);
	}

//	@Override
//	public Void visitBinary(BinaryTree tree, Void p) {
//		
//		// unpack the kind of expression, left and right hand side
//		ExpressionTree left = tree.getLeftOperand();
//		ExpressionTree right = tree.getRightOperand();
//		Kind kind = tree.getKind();
//		
//
//		// apply our code pattern logic
//		if (isGetCallOnMap(new TreePath(getCurrentPath(), left)) && kind == Kind.EQUAL_TO && isNullLiteral(right)) {
//			System.out.println("Found Match at line: " + getLineNumber(tree) + " in " + currCompUnit.getSourceFile().getName());
//		}
//		return super.visitBinary(tree, p);
//	}
	
	@Override
	public Void visitMethod(MethodTree node, Void p) {
		
		System.out.println("Method visitation!");
		
		JCBlock block = (JCBlock) node.getBody();
		
		TreeMaker maker = TreeMaker.instance(context);
		JCExpression iae = maker.Ident(Names.instance(context).fromString("IllegalArgumentException"));
		//JCExpression iae = maker.Ident(elements.getName("IllegalArgumentException"));
		
		// Arg 1 is the enclosing class, but we won't instantiate an inner class.
		// Arg 2 is a list of type parameters (of the enclosing class).
		// Arg 3 is the actual class expression.
		// Arg 4 is a list of arguments to pass to the constructor.
		// Arg 5 is a class body, for creating an anonymous class.
		JCExpression expr = maker.NewClass(null, List.<JCExpression> nil(), iae, null, null);
		JCThrow throwable = maker.Throw(expr);
		
		//block.stats = block.stats.append(throwable);
		
		//maker.VarDef(maker.Modifiers(0), arg1, , arg3)
		maker.If(maker.Literal(TypeTag.INT, 0), null, null);
		
		//todo how to make a JCExpression
//		maker.Annotation(new Attribute.Constant(
//				new com.sun.tools.javac.code.Type.JCPrimitiveType(TypeTag.INT, TypeSymbol.), 2));
		
//		VarSymbol s;
//		new VarSymbol(0, ASTHelper.makeSunName("myvar"), 
//				new com.sun.tools.javac.code.Type.JCPrimitiveType(null, null), null);
		
//		maker.VarDef(null, null);
//		maker.Literal(TypeTag.CLASS, "hello");
		
		
		block.stats.forEach(o -> System.out.println(o.getKind()));
		System.out.println("b");
//		if(right.getKind().equals(com.sun.source.tree.Tree.Kind.INT_LITERAL)) {
//			JCBinary b = (JCBinary) tree;
//			JCExpression s;
//			
//		}
		
		System.out.println("a");
		return super.visitMethod(node, p);
	}
	
	@Override
	public Void visitVariable(VariableTree node, Void arg1) {
		System.out.println(node.getName());
		System.out.println("\t" + node.getName().length());
		System.out.println("\t" + node.getName().subSequence(0, node.getName().length()));
		
		return super.visitVariable(node, arg1);
	}
	
//	@Override
//	public Void visitVariable(VariableTree node, Void p) {
//		TypeMirror type = trees.getTypeMirror(getCurrentPath());
//		//trees.printMessage(Diagnostic.Kind.NOTE, "Type is " + type, node, currCompUnit);
//		//trees.printMessage(Diagnostic.Kind.OTHER, "generic warning", node, currCompUnit);
//		
//		
//		if(type.getKind() == TypeKind.INT) {
//			System.out.println("Preparing to instrument on int type");
//			
//			System.out.println(node.getName());
//			
//			if(node.getName().toString().equals("i")) {
//				//trees.printMessage(Diagnostic.Kind.ERROR, "You can't name a variable i!", node, currCompUnit);
//			}
//			
//			
//			TreeMaker maker = TreeMaker.instance(context);
//			
////			com.sun.tools.javac.util.Name mName = new com.sun.tools.javac.util.Name(null) {
////
////				private final String zName = "NullPointerException";
////				
////				@Override
////				public int getIndex() {
////					// TODO Auto-generated method stub
////					return 0;
////				}
////
////				@Override
////				public int getByteLength() {
////					return zName.length();
////					// TODO Auto-generated method stub
////					//return 0;
////				}
////
////				@Override
////				public byte getByteAt(int var1) {
////					return zName.getBytes()[var1];
////					// TODO Auto-generated method stub
////					//return 0;
////				}
////
////				@Override
////				public byte[] getByteArray() {
////					return zName.getBytes();
////					// TODO Auto-generated method stub
////					//return null;
////				}
////
////				@Override
////				public int getByteOffset() {
////					// TODO Auto-generated method stub
////					return 0;
////				}
////				
////			};
//			
//			//JCExpression expr = maker.NewClass(null, null, maker.Ident(mName), null, null);
//			
//			
//			/*
//			 * JCFieldAccess iae = treeMaker.Select(
//				treeMaker.Select(
//						treeMaker.Ident(elements.getName("java")),
//				elements.getName("lang")),
//			elements.getName("IllegalArgumentException"));
//			 */
//			
//			JCExpression iae = maker.Ident(elements.getName("IllegalArgumentException"));
//			
//			
//			JCExpression expr = maker.NewClass(null, null, iae, null, null);
//
//			JCThrow throwable = maker
//					.at(((JCTree) node).pos)
//					.Throw(expr);
//					
//			JCVariableDecl variable = (JCVariableDecl) node;
//			
//			
//			
//			
//			//res
//			//.VarDef(new VarSymbol(0L, node.getName(), Type.JCPrimitiveType, (Symbol)null), null);
//			
//			
//			
//		}
//		
//		
//		return super.visitVariable(node, p);
//	}
	
	

	private boolean isNullLiteral(ExpressionTree node) {
		
		// is this expression representing "null"?
		return (node.getKind() == Kind.NULL_LITERAL);
	}

	private boolean isGetCallOnMap(TreePath path) {
		switch (path.getLeaf().getKind()) {
			
			// is it a Method Invocation?
			case METHOD_INVOCATION:
				MethodInvocationTree methodInvocation = (MethodInvocationTree) path.getLeaf();
				
				// extract the identifier and receiver (methodSelectTree)
				ExpressionTree methodSelect = methodInvocation.getMethodSelect();
				switch (methodSelect.getKind()) {
					case MEMBER_SELECT:
						
						// extract receiver
						MemberSelectTree mst = (MemberSelectTree) methodSelect;
						ExpressionTree expr = mst.getExpression();
						
						// get type of extracted receiver
						TypeMirror type = trees.getTypeMirror(new TreePath(path, expr));
						
						// extract method name
						Name name = mst.getIdentifier();
						
						// 1) check if receiver’s type is subtype of java.util.Map
						// 2) check if the extracted method name is exactly "get"
						if (types.isAssignable(types.erasure(type), types.erasure(mapType)) && name == getName) {
							return true;
						}
				}
		}
		return false;
	}

	private long getLineNumber(Tree tree) {
		
		// map offsets to line numbers in source file
		LineMap lineMap = currCompUnit.getLineMap();
		if (lineMap == null)
			return -1;
		
		// find offset of the specified AST node
		long position = sourcePositions.getStartPosition(currCompUnit, tree);
		return lineMap.getLineNumber(position);
	}
}