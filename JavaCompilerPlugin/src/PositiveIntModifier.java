import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.sun.source.tree.MethodTree;
import com.sun.source.tree.StatementTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.TreePathScanner;
import com.sun.tools.javac.code.TypeTag;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.JCTree.JCBinary;
import com.sun.tools.javac.tree.JCTree.JCBlock;
import com.sun.tools.javac.tree.JCTree.JCExpression;
import com.sun.tools.javac.tree.JCTree.JCIf;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.Name;
import com.sun.tools.javac.util.Names;

public class PositiveIntModifier extends TreePathScanner<Void, Void> {

	private static Set<String> TARGET_TYPES = Stream
			.of(byte.class, short.class, char.class, int.class, long.class, float.class, double.class)
			.map(Class::getName)
			.collect(Collectors.toSet());

	private final Context context;
	
	public PositiveIntModifier(Context context) {
		this.context = context;
	}

	private boolean shouldInstrument(VariableTree parameter) {
		return 
			TARGET_TYPES.contains(parameter.getType().toString());/*&& 
			parameter.getModifiers().getAnnotations().stream().anyMatch(
				a -> Positive.class.getSimpleName().equals(a.getAnnotationType().toString())
			);*/
	}
	
	@Override
	public Void visitMethod(MethodTree methodTree, Void p) {
		TreeMaker maker = TreeMaker.instance(context);
		Name parameterId = Names.instance(context).fromString("i"); //parameter.getName().toString(), where parameter instanceof VariableTree
		
		//LE = Less than or Equal to
		JCBinary CONDITION = maker.Binary(JCTree.Tag.LE, maker.Ident(parameterId), maker.Literal(TypeTag.INT, 0));
		
		JCExpression AIE = maker.Ident(Names.instance(context).fromString(IllegalArgumentException.class.getSimpleName()));
		
		JCBlock BLOCK = maker.Block(0, List.of(
				maker.Throw(
						maker.NewClass(null, List.nil(), AIE,
								List.of(
										//(yo + IAE) + nope
										maker.Binary(JCTree.Tag.PLUS, 
												//yo + IAE
												maker.Binary(JCTree.Tag.PLUS, 
														maker.Literal(TypeTag.CLASS, "yo"), 
														maker.Ident(parameterId)), 
												maker.Literal(TypeTag.CLASS, "nope")
											)), null))));
				
				
				
		
		//Create if statement
		//maker.If(maker.Parens(CONDITION), BLOCK, null);
		
		for(StatementTree statement : methodTree.getBody().getStatements()) {
			if(statement.getKind().equals(Tree.Kind.VARIABLE)) {
				VariableTree varTree = (VariableTree) statement;
				if(varTree.getName().toString().equals("i")) {
					System.out.println("Preparing to instrument i!");
					
					JCIf ifStatement = maker.at(((JCTree) varTree).pos).If(maker.Parens(CONDITION), BLOCK, null);
					JCBlock block = (JCBlock) methodTree.getBody();
					block.stats = block.stats.prepend(ifStatement);
					System.out.println("Instrumentation complete!");
					break;
					
					
				}
			}
		}
		
//		methodTree.getBody().getStatements().forEach(o -> {
//			System.out.println(o + "\t\t" + o.getKind());
//				
//			}
//		);		
		
		
		return super.visitMethod(methodTree, p);
	}

}
