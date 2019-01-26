package io.github.jorelali.javacompilerplugins.treescanners;

import static io.github.jorelali.javacompilerplugins.ASTHelper.createTreeMaker;

import java.util.Map;

import com.sun.source.tree.AnnotationTree;
import com.sun.source.tree.ClassTree;
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
import com.sun.tools.javac.tree.JCTree.JCStatement;
import com.sun.tools.javac.tree.JCTree.JCTypeCast;
import com.sun.tools.javac.tree.JCTree.JCVariableDecl;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.Names;

import io.github.jorelali.javacompilerplugins.ASTHelper;

public class ReflectionGeneratorTreeScanner extends TreeScanner<Void, Void> {

	private final Context context;
	private final Trees trees;
	
	private MethodTree currentMethodTree; 
	int variablePos = 0;
	private static final boolean VERBOSE = false;
	
	public ReflectionGeneratorTreeScanner(JavacTask task) {
		this.context = ((BasicJavacTask) task).getContext();
		this.trees = Trees.instance(task);
	}
	
	@Override
	public Void visitClass(ClassTree classTree, Void p) {
		if(VERBOSE) System.out.println("CLASS: " + classTree.getSimpleName());
		return super.visitClass(classTree, p);
	}
	
	@Override
	public Void visitVariable(VariableTree variableTree, Void p) {
		if(VERBOSE) System.out.println("VARIABLE: " + variableTree.getName());
		
		if(!variableTree.getModifiers().getAnnotations().isEmpty()) {
			for(AnnotationTree annotation : variableTree.getModifiers().getAnnotations()) {
				if(annotation.getAnnotationType().toString().equals("ReflectionField")) {
					
					JCVariableDecl dec = (JCVariableDecl) variableTree;
					//System.out.println("Instrumenting@" + variablePositions.get(dec));
					//TreeMaker maker = TreeMaker.instance(context).at(variablePositions.get(dec));
					
					TreeMaker maker = createTreeMaker(context, currentMethodTree);
					Names names = Names.instance(context);
					
					JCExpression fieldClassExpr = ASTHelper.resolveName(maker, names, "java.lang.reflect.Field");
					
					JCAssign targetClassTree = (JCAssign) annotation.getArguments().stream().filter(o -> ((JCAssign) o).lhs.toString().equals("targetClass")).findFirst().get();					
					JCExpression targetClass = (JCExpression) targetClassTree.rhs;
					
					JCMethodInvocation getDeclaredField = maker.Apply(List.nil(), ASTHelper.resolveName(maker, names, targetClass + ".getDeclaredField"), List.of(maker.Literal(variableTree.getName().toString())));
					JCVariableDecl field = maker.VarDef(maker.Modifiers(0), names.fromString("field"), fieldClassExpr, getDeclaredField);
					
					JCMethodInvocation setAccessible = maker.Apply(List.nil(), ASTHelper.resolveName(maker, names, "field.setAccessible"), List.of(maker.Literal(true)));
					JCExpressionStatement compiledSetAccessible = maker.Exec(setAccessible);
					
					JCMethodInvocation invoke = maker.Apply(List.nil(), ASTHelper.resolveName(maker, names, "field.get"), List.of(maker.Literal(TypeTag.BOT, null)));
					JCExpressionStatement compiledInvoke = maker.Exec(invoke);
					
					JCTypeCast z = maker.TypeCast(dec.getType(), compiledInvoke.getExpression());
					System.out.println("==> " + z);
					System.out.println("\n\n");
					
					//				    JCAssign assign = maker.Assign(maker.Ident(declareInt.name), maker.Literal(TypeTag.INT, 2));
					JCAssign assignVal = maker.Assign(maker.Ident(dec.name), z);
					
					ASTHelper.addExceptionToMethodDeclaredThrows(maker, names, currentMethodTree, Exception.class);
					List<JCStatement> resultantList = List.of(field, compiledSetAccessible, compiledInvoke, maker.Exec(assignVal));
					
					JCBlock logicBlock = maker.Block(0, resultantList);
					if(VERBOSE) System.out.println("=== Preparing to instrument variable" + variableTree.getName() + " ===");
					if(VERBOSE) System.out.println(logicBlock);
					JCBlock block = (JCBlock) currentMethodTree.getBody();
					//block.stats = block.stats.append(logicBlock);
					
					block.stats = ASTHelper.listInsert(variablePos, logicBlock, block.stats);
					
					System.out.println("INSTRUMENTING:");
					block.stats.forEach(System.out::println);
					//block.stats = ASTHelper.listRemove(variablePos - 1, block.stats);
					//block.stats.addAll(variablePos, List.of(logicBlock));
					
				}
			}
		}
		
		variablePos++;
		return super.visitVariable(variableTree, p);
	}
	
	@Override
	public Void visitMethod(MethodTree methodTree, Void p) {
		this.currentMethodTree = methodTree;
		variablePos = 0;
		if(VERBOSE) System.out.println("METHOD: " + methodTree.getName());
		
		//Handle @ReflectionMethod
		if(!methodTree.getModifiers().getAnnotations().isEmpty()) {
			for(AnnotationTree annotation : methodTree.getModifiers().getAnnotations()) {
				if(annotation.getAnnotationType().toString().equals("ReflectionMethod")) {
					
					TreeMaker maker = createTreeMaker(context, methodTree);
					Names names = Names.instance(context);

					JCExpression methodClassExpr = ASTHelper.resolveName(maker, names, "java.lang.reflect.Method");

					JCAssign targetClassTree = (JCAssign) annotation.getArguments().stream().filter(o -> ((JCAssign) o).lhs.toString().equals("targetClass")).findFirst().get();					
					JCExpression targetClass = (JCExpression) targetClassTree.rhs;
					
					JCMethodInvocation getDeclaredMethod = maker.Apply(List.nil(), ASTHelper.resolveName(maker, names, targetClass + ".getDeclaredMethod"), List.of(maker.Literal(methodTree.getName().toString())));
					
					JCVariableDecl method = maker.VarDef(maker.Modifiers(0), names.fromString("method"), methodClassExpr, getDeclaredMethod);
					
					JCMethodInvocation setAccessible = maker.Apply(List.nil(), ASTHelper.resolveName(maker, names, "method.setAccessible"), List.of(maker.Literal(true)));
					JCExpressionStatement compiledSetAccessible = maker.Exec(setAccessible);
					
					JCMethodInvocation invoke = maker.Apply(List.nil(), ASTHelper.resolveName(maker, names, "method.invoke"), List.of(maker.Literal(TypeTag.BOT, null)));
					JCExpressionStatement compiledInvoke = maker.Exec(invoke);
					
					ASTHelper.addExceptionToMethodDeclaredThrows(maker, names, methodTree, Exception.class);
					
					//INSTRUMENTATION START

					JCAssign isPrivateAssign = (JCAssign) annotation.getArguments().stream().filter(o -> ((JCAssign) o).lhs.toString().equals("isPrivate")).findFirst().get();
					boolean isPrivate = Boolean.valueOf(String.valueOf(isPrivateAssign.rhs));
					
					List<JCStatement> resultantList;
					
					if(isPrivate) {
						resultantList = List.of(method, compiledSetAccessible, compiledInvoke);
					} else {
						resultantList = List.of(method, compiledInvoke);
					}
					
					JCBlock logicBlock = maker.Block(0, resultantList);
					if(VERBOSE) System.out.println("=== Preparing to instrument method " + methodTree.getName() + " ===");
					if(VERBOSE) System.out.println(logicBlock);
					JCBlock block = (JCBlock) methodTree.getBody();
					block.stats = block.stats.append(logicBlock);
				}
			}	
		}
		
		return super.visitMethod(methodTree, p);
	}
	
}
