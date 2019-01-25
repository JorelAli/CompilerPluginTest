package io.github.jorelali.javacompilerplugins.treescanners;

import static io.github.jorelali.javacompilerplugins.ASTHelper.createAssignment;
import static io.github.jorelali.javacompilerplugins.ASTHelper.createLocalPrimitiveVariable;
import static io.github.jorelali.javacompilerplugins.ASTHelper.createTreeMaker;
import static io.github.jorelali.javacompilerplugins.ASTHelper.newClassInstanceWithNoParamsOrGenerics;
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
	
	/*
	 * TODO:
	 * Non-primitive variable assignment
	 * Class instantiation with parameters
	 */
	
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
					
										
					JCVariableDecl declareInt = createLocalPrimitiveVariable(maker, "hello", TypeTag.INT, maker.Literal(TypeTag.INT, 0));
					System.out.println(declareInt);
					JCExpressionStatement assignIntTo2 = createAssignment(maker, declareInt, maker.Literal(TypeTag.INT, 2));
					JCExpressionStatement newModifier = newClassInstanceWithNoParamsOrGenerics(maker, names, "java.lang.reflect.Modifier");
					
					JCExpression modifierExpr = ASTHelper.resolveName(maker, names, "java.lang.reflect.Modifier");
					
					JCVariableDecl modifierDecl = maker.VarDef(maker.Modifiers(0), ASTHelper.makeNameDirty("myModifier"), modifierExpr, maker.NewClass(null, List.nil(), modifierExpr, List.nil(), null));
					System.out.println(modifierDecl);
					
					class Example { }					
					System.out.println(maker.Literal(TypeTag.CLASS, Example.class));
					
					JCBlock logicBlock = maker.Block(0, List.of(declareInt, assignIntTo2, newModifier, modifierDecl));
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
