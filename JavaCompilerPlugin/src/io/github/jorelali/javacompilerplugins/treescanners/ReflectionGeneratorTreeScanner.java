package io.github.jorelali.javacompilerplugins.treescanners;

import static io.github.jorelali.javacompilerplugins.ASTHelper.*;

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
					
					Map<String, String> annotationMap = parseAnnotation(annotation);
					String methodName = methodTree.getName().toString();
					//Class targetClass = Class.forName(annotationMap.get("targetClass").replace(".class", ""));
					
					/*
					 * So: Method m = targetClass.getClass.getdeclaredmethod(methodName)
					 * inside, you want blah.invoke()
					 */
					
					JCExpression inner = maker.Select(maker.Ident(makeName("java")), makeName("lang"));
					inner = maker.Select(inner, makeName("reflect"));
					inner = maker.Select(inner, makeName("Method"));
					
					JCVariableDecl declareInt = createLocalPrimitiveVariable(maker, "hello", TypeTag.INT, maker.Literal(TypeTag.INT, 0));
					JCExpressionStatement assignment = createAssignment(maker, declareInt, maker.Literal(TypeTag.INT, 2));
						
					
										
					JCBlock logicBlock = maker.Block(0, List.of(declareInt, assignment));
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
