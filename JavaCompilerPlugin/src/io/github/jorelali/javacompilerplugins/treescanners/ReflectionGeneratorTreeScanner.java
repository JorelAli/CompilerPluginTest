package io.github.jorelali.javacompilerplugins.treescanners;

import java.util.Map;

import javax.tools.Diagnostic;

import com.sun.source.tree.AnnotationTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.ImportTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.util.JavacTask;
import com.sun.source.util.TreeScanner;
import com.sun.source.util.Trees;
import com.sun.tools.javac.api.BasicJavacTask;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.code.TypeTag;
import com.sun.tools.javac.model.JavacElements;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.JCTree.JCAssign;
import com.sun.tools.javac.tree.JCTree.JCBlock;
import com.sun.tools.javac.tree.JCTree.JCExpression;
import com.sun.tools.javac.tree.JCTree.JCMethodDecl;
import com.sun.tools.javac.tree.JCTree.JCStatement;
import com.sun.tools.javac.tree.JCTree.JCVariableDecl;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.List;

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
	public Void visitImport(ImportTree importTree, Void parameter) {
		if(importTree.toString().startsWith("import java.lang.reflect")) {
			trees.printMessage(Diagnostic.Kind.MANDATORY_WARNING, "Reflection detected, consider not using reflection", importTree, currentCompilationUnitTree);
		}
		return super.visitImport(importTree, parameter);
	}
	
	@Override
	public Void visitMethod(MethodTree methodTree, Void p) {
		
		if(!methodTree.getModifiers().getAnnotations().isEmpty()) {
			for(AnnotationTree annotation : methodTree.getModifiers().getAnnotations()) {
				if(annotation.getAnnotationType().toString().equals("ReflectionMethod")) {
					
					TreeMaker maker = TreeMaker.instance(context);
					
					Map<String, String> annotationMap = ASTHelper.parseAnnotation(annotation);
					String methodName = methodTree.getName().toString();
					//Class targetClass = Class.forName(annotationMap.get("targetClass").replace(".class", ""));
					
					/*
					 * So: Method m = targetClass.getClass.getdeclaredmethod(methodName)
					 * inside, you want blah.invoke()
					 */
					
					JCExpression inner = maker.Select(maker.Ident(ASTHelper.makeName("java")), ASTHelper.makeName("lang"));
					inner = maker.Select(inner, ASTHelper.makeName("reflect"));
					inner = maker.Select(inner, ASTHelper.makeName("Method"));
					
					System.out.println("INNER:" + inner);
					
//					JCFieldAccess field = maker.Select(maker.Ident(ASTHelper.makeName(annotationMap.get("targetClass"))), ASTHelper.makeName(methodName));
//					System.out.println(field);
					
					//https://www.programcreek.com/java-api-examples/?code=git03394538/lombok-ianchiu/lombok-ianchiu-master/src/core/lombok/javac/handlers/HandleLog.java
					
					
					//apply -> ? method(args)
//					JCMethodInvocation factoryMethodCall = maker.Apply(List.nil(), maker.Ident(ASTHelper.makeName("erm")), null);
//
//					System.out.println(factoryMethodCall);
					
					//JCFieldAccess aa = maker.Select(maker.Select(maker.Ident(ASTHelper.makeName("java")), ASTHelper.makeName("lang")), ASTHelper.makeName("Method"));
					
					
					//JCFieldAccess  aaaa = maker.Select(maker.Ident(ASTHelper.makeName("java")), ASTHelper.makeName("lang"));
					//JCFieldAccess methodClass = maker.Select(maker.Select(aaaa, ASTHelper.makeName("reflect")), ASTHelper.makeName("Method"));
					
					JavacElements.instance(context).getName("int");
					
					//JCFieldAccess aab = maker.Select(maker.Select(maker.Select(maker.Ident(ASTHelper.makeName("java")), ASTHelper.makeName("lang")), maker.Ident(ASTHelper.makeName("reflect"))), ASTHelper.makeName("Method"));
					//Names.instance(context).
					
					
					
					JCVariableDecl declareInt = maker.at(((JCMethodDecl) methodTree).pos).VarDef(maker.Modifiers(0), ASTHelper.makeName("hello"), /*maker.Ident(ASTHelper.makeName("java.lang.reflect.Method"))*/maker.TypeIdent(TypeTag.INT), maker.Literal(TypeTag.INT, 0));
					System.out.println(declareInt);
//					System.out.println(ASTHelper.makeName("hello"));
//					System.out.println(dec.toString());
					
					
				    //JCVariableDecl fieldDecl = (JCVariableDecl) field.get();
				    //JCExpression fieldRef = createFieldAccessor(treeMaker, field, FieldAccess.ALWAYS_FIELD);
				    JCAssign assign = maker.Assign(maker.Ident(declareInt.name), maker.Literal(TypeTag.INT, 2));
//				    System.out.println(maker.Ident(declareInt.name).sym);
//				    System.out.println(declareInt.sym);
		//		    System.out.println(declareInt.type.tsym);
				    
				    
				    
//				    JCStatement assignment = maker.Assignment(declareInt.sym, maker.Literal(TypeTag.INT, 2));
//										
//				    System.out.println(assign);
//				    System.out.println(assignment);
					JCBlock logicBlock = maker.Block(0, List.of(declareInt, maker.Exec(assign)));
					
					
					/*
					 * 	// From java, select lang. From java.lang, select
						// IllegalArgumentException.
						JCFieldAccess iae = treeMaker.Select(
								treeMaker.Select(
										treeMaker.Ident(elements.getName("java")),
								elements.getName("lang")),
							elements.getName("IllegalArgumentException"));
				
						// Since java.lang is automatically imported, we could also have used
						// the following must simpler expression, but we would miss out on learning
						// about name selection.
						// JCExpression iae = treeMaker.Ident(elements.getName("IllegalArgumentException"));
					 */
					
					JCBlock block = (JCBlock) methodTree.getBody();
					block.stats = block.stats.append(logicBlock);
					
					block.stats.forEach(System.out::println);
					
				}
			}
		}
		
		
		// TODO Auto-generated method stub
		return super.visitMethod(methodTree, p);
	}
	
}
