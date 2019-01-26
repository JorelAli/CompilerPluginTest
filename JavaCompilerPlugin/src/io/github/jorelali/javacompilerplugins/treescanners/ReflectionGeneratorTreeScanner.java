package io.github.jorelali.javacompilerplugins.treescanners;

import static io.github.jorelali.javacompilerplugins.ASTHelper.createTreeMaker;

import com.sun.source.tree.AnnotationTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.JavacTask;
import com.sun.source.util.TreeScanner;
import com.sun.source.util.Trees;
import com.sun.tools.javac.api.BasicJavacTask;
import com.sun.tools.javac.code.TypeTag;
import com.sun.tools.javac.tree.JCTree.JCAnnotation;
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

import static io.github.jorelali.javacompilerplugins.ASTHelper.*;

public class ReflectionGeneratorTreeScanner extends TreeScanner<Void, Void> {

	private final Context context;
	private final Trees trees;
	
	private MethodTree currentMethodTree; 
	private int variablePos = 0;
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
					
					TreeMaker maker = createTreeMaker(context, currentMethodTree);
					Names names = Names.instance(context);
					
					JCExpression fieldClassExpr = resolveName(maker, names, "java.lang.reflect.Field");
										
					JCExpression targetClass = getAnnotationValue(annotation, "targetClass");
					
					JCMethodInvocation getDeclaredField = maker.Apply(List.nil(), resolveName(maker, names, targetClass + ".getDeclaredField"), List.of(maker.Literal(variableTree.getName().toString())));
					JCVariableDecl field = maker.VarDef(maker.Modifiers(0), names.fromString("field"), fieldClassExpr, getDeclaredField);
					
					JCMethodInvocation setAccessible = maker.Apply(List.nil(), resolveName(maker, names, "field.setAccessible"), List.of(maker.Literal(true)));
					JCExpressionStatement compiledSetAccessible = maker.Exec(setAccessible);
					
					JCMethodInvocation invoke = maker.Apply(List.nil(), resolveName(maker, names, "field.get"), List.of(maker.Literal(TypeTag.BOT, null)));
					JCExpressionStatement compiledInvoke = maker.Exec(invoke);
					
					JCVariableDecl dec = (JCVariableDecl) variableTree;
					JCTypeCast typeCast = maker.TypeCast(dec.getType(), compiledInvoke.getExpression());
					JCAssign assignVal = maker.Assign(maker.Ident(dec.name), typeCast);
					
					addExceptionToMethodDeclaredThrows(maker, names, currentMethodTree, Exception.class);
					
					boolean isPrivate = Boolean.valueOf(String.valueOf(getAnnotationValue(annotation, "isPrivate")));
					
					List<JCStatement> resultantList;
					
					if(isPrivate) {
						resultantList = List.of(field, compiledSetAccessible, maker.Exec(assignVal));
					} else {
						resultantList = List.of(field, maker.Exec(assignVal));
					}
					
					
					JCBlock logicBlock = maker.Block(0, resultantList);
					if(VERBOSE) System.out.println("=== Preparing to instrument variable" + variableTree.getName() + " ===");
					if(VERBOSE) System.out.println(logicBlock);
					
					JCBlock block = (JCBlock) currentMethodTree.getBody();
					block.stats = listInsert(variablePos, logicBlock, block.stats);
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

					JCExpression methodClassExpr = resolveName(maker, names, "java.lang.reflect.Method");

					JCExpression targetClass = getAnnotationValue(annotation, "targetClass");
					
					JCMethodInvocation getDeclaredMethod = maker.Apply(List.nil(), resolveName(maker, names, targetClass + ".getDeclaredMethod"), List.of(maker.Literal(methodTree.getName().toString())));
					
					JCVariableDecl method = maker.VarDef(maker.Modifiers(0), names.fromString("method"), methodClassExpr, getDeclaredMethod);
					
					JCMethodInvocation setAccessible = maker.Apply(List.nil(), resolveName(maker, names, "method.setAccessible"), List.of(maker.Literal(true)));
					JCExpressionStatement compiledSetAccessible = maker.Exec(setAccessible);
					
					JCMethodInvocation invoke = maker.Apply(List.nil(), resolveName(maker, names, "method.invoke"), List.of(maker.Literal(TypeTag.BOT, null)));
					JCExpressionStatement compiledInvoke = maker.Exec(invoke);
					
					addExceptionToMethodDeclaredThrows(maker, names, methodTree, Exception.class);
					
					//INSTRUMENTATION START

					boolean isPrivate = Boolean.valueOf(String.valueOf(getAnnotationValue(annotation, "isPrivate")));
					
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
