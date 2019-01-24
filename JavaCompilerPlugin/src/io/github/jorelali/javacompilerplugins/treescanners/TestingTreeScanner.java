package io.github.jorelali.javacompilerplugins.treescanners;

import com.sun.source.tree.MethodTree;
import com.sun.source.tree.StatementTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.JavacTask;
import com.sun.source.util.TreeScanner;
import com.sun.tools.javac.api.BasicJavacTask;
import com.sun.tools.javac.code.TypeTag;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.tree.JCTree.JCBinary;
import com.sun.tools.javac.tree.JCTree.JCBlock;
import com.sun.tools.javac.tree.JCTree.JCExpression;
import com.sun.tools.javac.tree.JCTree.JCIf;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.Name;
import com.sun.tools.javac.util.Names;

public class TestingTreeScanner extends TreeScanner<Void, Void> {

	private final Context context;

	public TestingTreeScanner(JavacTask task) {
		this.context = ((BasicJavacTask) task).getContext();
	}

	@Override
	public Void visitMethod(MethodTree methodTree, Void p) {
		TreeMaker maker = TreeMaker.instance(context);
		// parameter.getName().toString(),
		// where
		// parameter
		// instanceof
		// VariableTree
		Name parameterId = Names.instance(context).fromString("i"); 

		// LE = Less than or Equal to
		JCBinary CONDITION = maker.Binary(JCTree.Tag.LE, maker.Ident(parameterId), maker.Literal(TypeTag.INT, 0));

		JCExpression AIE = maker.Ident(Names.instance(context).fromString(IllegalArgumentException.class.getSimpleName()));

		JCBlock BLOCK = maker.Block(0,
				com.sun.tools.javac.util.List.of(maker.Throw(maker.NewClass(null, com.sun.tools.javac.util.List.nil(),
						AIE,
						com.sun.tools.javac.util.List.of(
								// (yo + IAE) + nope
								maker.Binary(JCTree.Tag.PLUS,
										// yo + IAE
										maker.Binary(JCTree.Tag.PLUS, maker.Literal(TypeTag.CLASS, "yo"),
												maker.Ident(parameterId)),
										maker.Literal(TypeTag.CLASS, "nope"))),
						null))));

		for (StatementTree statement : methodTree.getBody().getStatements()) {
			if (statement.getKind().equals(Tree.Kind.VARIABLE)) {
				VariableTree varTree = (VariableTree) statement;
				if (varTree.getName().toString().equals("i")) {
					System.out.println("Preparing to instrument i!");

					JCIf ifStatement = maker.at(((JCTree) varTree).pos).If(maker.Parens(CONDITION), BLOCK, null);

					// System.out.println(ifStatement);

					JCBlock block = (JCBlock) methodTree.getBody();
					block.stats = block.stats.append(ifStatement);

					block.stats.forEach(System.out::println);
					block.stats.forEach(o -> System.out.println(o.getKind()));

					System.out.println("Instrumentation complete!");
					break;

				}
			}
		}
		return super.visitMethod(methodTree, p);
	}

}
