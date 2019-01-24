package io.github.jorelali.javacompilerplugins.treescanners;

import javax.tools.Diagnostic;

import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.ImportTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.util.JavacTask;
import com.sun.source.util.TreeScanner;
import com.sun.source.util.Trees;
import com.sun.tools.javac.api.BasicJavacTask;
import com.sun.tools.javac.util.Context;

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
		// TODO Auto-generated method stub
		return super.visitMethod(methodTree, p);
	}
	
}
