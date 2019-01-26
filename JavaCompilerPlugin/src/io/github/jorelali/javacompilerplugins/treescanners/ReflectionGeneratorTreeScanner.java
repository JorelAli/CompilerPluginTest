package io.github.jorelali.javacompilerplugins.treescanners;

import static io.github.jorelali.javacompilerplugins.ASTHelper.addExceptionToMethodDeclaredThrows;
import static io.github.jorelali.javacompilerplugins.ASTHelper.createTreeMaker;
import static io.github.jorelali.javacompilerplugins.ASTHelper.getAnnotationValue;
import static io.github.jorelali.javacompilerplugins.ASTHelper.listInsert;
import static io.github.jorelali.javacompilerplugins.ASTHelper.resolveName;

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

import io.github.jorelali.javacompilerplugins.annotations.ReflectionField;
import io.github.jorelali.javacompilerplugins.annotations.ReflectionStaticField;
import io.github.jorelali.javacompilerplugins.annotations.ReflectionStaticMethod;

public class ReflectionGeneratorTreeScanner extends TreeScanner<Void, Void> {

	private final Context context;
	private final Trees trees;
	private final Names names;
	
	private MethodTree currentMethodTree; 
	private int variablePos = 0;
	private static final boolean VERBOSE = true;
	
	public ReflectionGeneratorTreeScanner(JavacTask task) {
		this.context = ((BasicJavacTask) task).getContext();
		this.trees = Trees.instance(task);
		this.names = Names.instance(context);
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
				if(annotation.getAnnotationType().toString().equals(ReflectionStaticField.class.getSimpleName())) {
					
					TreeMaker maker = createTreeMaker(context, currentMethodTree);
										
					JCExpression targetClass = getAnnotationValue(annotation, "targetClass");
					
					JCMethodInvocation getDeclaredField = maker.Apply(List.nil(), resolveName(maker, names, targetClass + ".getDeclaredField"), List.of(maker.Literal(variableTree.getName().toString())));
					JCVariableDecl field = maker.VarDef(maker.Modifiers(0), names.fromString("field"), resolveName(maker, names, "java.lang.reflect.Field"), getDeclaredField);
					
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
					if(VERBOSE) System.out.println("=== Preparing to instrument variable " + variableTree.getName() + " ===");
					if(VERBOSE) System.out.println(logicBlock);
					
					JCBlock block = (JCBlock) currentMethodTree.getBody();
					block.stats = listInsert(variablePos, logicBlock, block.stats);
					variablePos+=2;
				}
				if(annotation.getAnnotationType().toString().equals(ReflectionField.class.getSimpleName())) {
					
					TreeMaker maker = createTreeMaker(context, currentMethodTree);
										
					JCExpression targetClass = getAnnotationValue(annotation, "targetClass");
					
					JCMethodInvocation getDeclaredField = maker.Apply(List.nil(), resolveName(maker, names, targetClass + ".getDeclaredField"), List.of(maker.Literal(variableTree.getName().toString())));
					JCVariableDecl field = maker.VarDef(maker.Modifiers(0), names.fromString("field"), resolveName(maker, names, "java.lang.reflect.Field"), getDeclaredField);
					
					JCMethodInvocation setAccessible = maker.Apply(List.nil(), resolveName(maker, names, "field.setAccessible"), List.of(maker.Literal(true)));
					JCExpressionStatement compiledSetAccessible = maker.Exec(setAccessible);
					
					String instanceStr = String.valueOf(getAnnotationValue(annotation, "withInstance"));
					instanceStr = instanceStr.substring(1, instanceStr.length() - 1);
					
					//
					JCMethodInvocation invoke = maker.Apply(List.nil(), resolveName(maker, names, "field.get"), List.of(maker.Ident(names.fromString(instanceStr))));
					JCExpressionStatement compiledInvoke = maker.Exec(invoke);
					System.out.println("->" + compiledInvoke);
					
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
					if(VERBOSE) System.out.println("=== Preparing to instrument variable " + variableTree.getName() + " ===");
					if(VERBOSE) System.out.println(logicBlock);
					
					JCBlock block = (JCBlock) currentMethodTree.getBody();
					block.stats = listInsert(variablePos, logicBlock, block.stats);
					block.stats.forEach(System.out::println);
					variablePos+=2;
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
				if(annotation.getAnnotationType().toString().equals(ReflectionStaticMethod.class.getSimpleName())) {
					
					TreeMaker maker = createTreeMaker(context, methodTree);

					JCExpression targetClass = getAnnotationValue(annotation, "targetClass");
					
					JCMethodInvocation getDeclaredMethod = maker.Apply(List.nil(), resolveName(maker, names, targetClass + ".getDeclaredMethod"), List.of(maker.Literal(methodTree.getName().toString())));
					
					JCVariableDecl method = maker.VarDef(maker.Modifiers(0), names.fromString("method"), resolveName(maker, names, "java.lang.reflect.Method"), getDeclaredMethod);
					
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
