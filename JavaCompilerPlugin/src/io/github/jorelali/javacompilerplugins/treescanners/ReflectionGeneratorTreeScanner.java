package io.github.jorelali.javacompilerplugins.treescanners;

import static io.github.jorelali.javacompilerplugins.ASTHelper.createTreeMaker;

import com.sun.source.tree.AnnotationTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.JavacTask;
import com.sun.source.util.TreeScanner;
import com.sun.source.util.Trees;
import com.sun.tools.javac.api.BasicJavacTask;
import com.sun.tools.javac.code.TypeTag;
import com.sun.tools.javac.tree.JCTree.JCAssign;
import com.sun.tools.javac.tree.JCTree.JCBlock;
import com.sun.tools.javac.tree.JCTree.JCExpression;
import com.sun.tools.javac.tree.JCTree.JCExpressionStatement;
import com.sun.tools.javac.tree.JCTree.JCMethodInvocation;
import com.sun.tools.javac.tree.JCTree.JCVariableDecl;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.Names;

import io.github.jorelali.javacompilerplugins.ASTHelper;

public class ReflectionGeneratorTreeScanner extends TreeScanner<Void, Void> {

	private final Context context;
	private final Trees trees;
	private static final boolean VERBOSE = false;
	
	public ReflectionGeneratorTreeScanner(JavacTask task) {
		this.context = ((BasicJavacTask) task).getContext();
		this.trees = Trees.instance(task);
	}
	
	@Override
	public Void visitCompilationUnit(CompilationUnitTree compilationUnitTree, Void p) {
		if(VERBOSE) System.out.println("COMPUNIT: " + compilationUnitTree);
		return super.visitCompilationUnit(compilationUnitTree, p);
	}
	
	@Override
	public Void visitClass(ClassTree classTree, Void p) {
		if(VERBOSE) System.out.println("CLASS: " + classTree.getSimpleName());
		return super.visitClass(classTree, p);
	}
	
	@Override
	public Void visitVariable(VariableTree variableTree, Void p) {
		if(VERBOSE) System.out.println("VARIABLE: " + variableTree.getName());
		return super.visitVariable(variableTree, p);
	}
	
	@Override
	public Void visitMethod(MethodTree methodTree, Void p) {
		if(VERBOSE) System.out.println("METHOD: " + methodTree.getName());
		
		//Handle @ReflectionMethod
		if(!methodTree.getModifiers().getAnnotations().isEmpty()) {
			for(AnnotationTree annotation : methodTree.getModifiers().getAnnotations()) {
				if(annotation.getAnnotationType().toString().equals("ReflectionMethod")) {
					
					TreeMaker maker = createTreeMaker(context, methodTree);
					Names names = Names.instance(context);

					JCExpression methodClassExpr = ASTHelper.resolveName(maker, names, "java.lang.reflect.Method");

					JCAssign targetClassTree = (JCAssign) annotation.getArguments().stream().peek(o -> System.out.println(o)).filter(o -> ((JCAssign) o).lhs.toString().equals("targetClass")).findFirst().get();					
					JCExpression targetClass = (JCExpression) targetClassTree.rhs;
					
					JCMethodInvocation getDeclaredMethod = maker.Apply(List.nil(), ASTHelper.resolveName(maker, names, targetClass + ".getDeclaredMethod"), List.of(maker.Literal(methodTree.getName().toString())));
					
					JCVariableDecl method = maker.VarDef(maker.Modifiers(0), names.fromString("method"), methodClassExpr, getDeclaredMethod);
					
					JCMethodInvocation setAccessible = maker.Apply(List.nil(), ASTHelper.resolveName(maker, names, "method.setAccessible"), List.of(maker.Literal(true)));
					JCExpressionStatement compiledSetAccessible = maker.Exec(setAccessible);
					
					JCMethodInvocation invoke = maker.Apply(List.nil(), ASTHelper.resolveName(maker, names, "method.invoke"), List.of(maker.Literal(TypeTag.BOT, null)));
					JCExpressionStatement compiledInvoke = maker.Exec(invoke);
					
					ASTHelper.addExceptionToMethodDeclaredThrows(maker, names, methodTree, Exception.class);
					
					//INSTRUMENTATION START

					JCBlock logicBlock = maker.Block(0, List.of(method, compiledSetAccessible, compiledInvoke));
					System.out.println("=== Preparing to instrument " + methodTree.getName() + " ===");
					System.out.println(logicBlock);
					JCBlock block = (JCBlock) methodTree.getBody();
					block.stats = block.stats.append(logicBlock);
				}
			}	
		}
		
		return super.visitMethod(methodTree, p);
	}
	
}
