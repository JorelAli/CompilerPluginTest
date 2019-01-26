package io.github.jorelali.javacompilerplugins.treescanners;

import javax.tools.Diagnostic;

import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.ImportTree;
import com.sun.source.util.JavacTask;
import com.sun.source.util.TreeScanner;
import com.sun.source.util.Trees;
import com.sun.tools.javac.api.BasicJavacTask;
import com.sun.tools.javac.tree.JCTree.JCImport;
import com.sun.tools.javac.util.Context;

public class ReflectionImportTreeScanner extends TreeScanner<Void, Void> {

	private final Context context;
	private final Trees trees;
	private CompilationUnitTree currentCompilationUnitTree;
	
	public ReflectionImportTreeScanner(JavacTask task) {
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
			JCImport t = (JCImport) importTree;
			t.pos += "import java.lang.reflect.".length();
			
			trees.printMessage(Diagnostic.Kind.MANDATORY_WARNING, "Reflection detected, consider not using reflection", t, currentCompilationUnitTree);
		}
		return super.visitImport(importTree, parameter);
	}
	
}
