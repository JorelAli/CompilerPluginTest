package io.github.jorelali.javacompilerplugins.treescanners;

import static io.github.jorelali.javacompilerplugins.ASTHelper.createAssignment;
import static io.github.jorelali.javacompilerplugins.ASTHelper.createLocalPrimitiveVariable;
import static io.github.jorelali.javacompilerplugins.ASTHelper.createTreeMaker;
import static io.github.jorelali.javacompilerplugins.ASTHelper.parseAnnotation;

import java.util.Map;

import com.sun.source.tree.AnnotationTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.util.JavacTask;
import com.sun.source.util.TreeScanner;
import com.sun.source.util.Trees;
import com.sun.tools.javac.api.BasicJavacTask;
import com.sun.tools.javac.code.TypeTag;
import com.sun.tools.javac.tree.JCTree.JCBlock;
import com.sun.tools.javac.tree.JCTree.JCExpression;
import com.sun.tools.javac.tree.JCTree.JCExpressionStatement;
import com.sun.tools.javac.tree.JCTree.JCIdent;
import com.sun.tools.javac.tree.JCTree.JCNewClass;
import com.sun.tools.javac.tree.JCTree.JCVariableDecl;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.Names;

import io.github.jorelali.javacompilerplugins.ASTHelper;

public class ReflectionGeneratorTreeScanner extends TreeScanner<Void, Void> {

	private final Context context;
	private final Trees trees;
	private CompilationUnitTree currentCompilationUnitTree;
	
	public ReflectionGeneratorTreeScanner(JavacTask task) {
		this.context = ((BasicJavacTask) task).getContext();
		this.trees = Trees.instance(task);
	}
	
	@Override
	public Void visitCompilationUnit(CompilationUnitTree compilationUnitTree, Void p) {
		currentCompilationUnitTree = compilationUnitTree;
		return super.visitCompilationUnit(compilationUnitTree, p);
	}
	
	@Override
	public Void visitMethod(MethodTree methodTree, Void p) {
		
		if(!methodTree.getModifiers().getAnnotations().isEmpty()) {
			for(AnnotationTree annotation : methodTree.getModifiers().getAnnotations()) {
				if(annotation.getAnnotationType().toString().equals("ReflectionMethod")) {
					
					TreeMaker maker = createTreeMaker(context, methodTree);
					Names names = Names.instance(context);

					Map<String, String> annotationMap = parseAnnotation(annotation);
					String methodName = methodTree.getName().toString();
					//Class targetClass = Class.forName(annotationMap.get("targetClass").replace(".class", ""));
					
					/*
					 * So: Method m = targetClass.getClass.getdeclaredmethod(methodName)
					 * inside, you want blah.invoke()
					 */
					
					
					JCExpression inner = maker.Select(maker.Ident(names.fromString("java")), names.fromString("lang"));
					inner = maker.Select(inner, names.fromString("reflect"));
					inner = maker.Select(inner, names.fromString("Modifier"));
					
					
					inner = ASTHelper.resolveName(maker, names, "java.lang.reflect.Modifier");
					//System.out.println("inner: " + inner);
					System.out.println(ASTHelper.resolveName(maker, names, "java.lang.reflect.Modifier"));
					
					JCVariableDecl declareInt = createLocalPrimitiveVariable(maker, "hello", TypeTag.INT, maker.Literal(TypeTag.INT, 0));
					JCExpressionStatement assignment = createAssignment(maker, declareInt, maker.Literal(TypeTag.INT, 2));

					class Example { }					
					System.out.println(maker.Literal(TypeTag.CLASS, Example.class));
					
					//System.out.println(Names.instance(context).fromString("java").equals(makeName("java")));
					
					//new java.lang.reflect.Modifier();
					//JCIdent iae = maker.Ident(inner.);//maker.Ident(Names.instance(context).fromString("java.lang.reflect.Modifier"));
					//JCIdent iae = maker.Ident(Names.instance(context).fromString("IllegalArgumentException"));
					JCNewClass a = maker.NewClass(null, List.nil(), inner, List.nil(), null);
					
					//TODO:
					//method invocation
					//field accessing
					//Class instantiation
					//non-primitive variable declaration
					System.out.println(a);
					
					
					
										
					JCBlock logicBlock = maker.Block(0, List.of(declareInt, assignment, maker.Exec(a)));
					System.out.println(logicBlock);
					
					
					JCBlock block = (JCBlock) methodTree.getBody();
					block.stats = block.stats.append(logicBlock);
										
				}
			}
		}
		
		
		// TODO Auto-generated method stub
		return super.visitMethod(methodTree, p);
	}
	
}
