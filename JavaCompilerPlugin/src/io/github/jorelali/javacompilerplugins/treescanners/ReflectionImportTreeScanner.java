package io.github.jorelali.javacompilerplugins.treescanners;

import com.sun.source.tree.ImportTree;
import com.sun.source.util.JavacTask;
import com.sun.source.util.TreeScanner;
import com.sun.tools.javac.api.BasicJavacTask;
import com.sun.tools.javac.util.Context;

public class ReflectionImportTreeScanner extends TreeScanner<Void, Void> {

	private final Context context;
	
	public ReflectionImportTreeScanner(JavacTask task) {
		this.context = ((BasicJavacTask) task).getContext();
	}
	
	@Override
	public Void visitImport(ImportTree importTree, Void p) {
		System.out.println(importTree.toString());
		return super.visitImport(importTree, p);
	}
	
}
