package io.github.jorelali.javacompilerplugins;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.StatementTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.JavacTask;
import com.sun.source.util.Plugin;
import com.sun.source.util.TaskEvent;
import com.sun.source.util.TaskListener;
import com.sun.source.util.TreeScanner;
import com.sun.tools.javac.api.BasicJavacTask;
import com.sun.tools.javac.code.TypeTag;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.JCTree.JCBinary;
import com.sun.tools.javac.tree.JCTree.JCBlock;
import com.sun.tools.javac.tree.JCTree.JCExpression;
import com.sun.tools.javac.tree.JCTree.JCIf;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.Name;
import com.sun.tools.javac.util.Names;

public class JorelsPlugin implements Plugin {

	@Override
	public String getName() {
		return "JorelsPlugin";
	}

//	private static Set<String> TARGET_TYPES = new HashSet<>(Arrays.asList(
//			// Use only primitive types for simplicity
//			byte.class.getName(), short.class.getName(), char.class.getName(), int.class.getName(),
//			long.class.getName(), float.class.getName(), double.class.getName()));
//
//	@Override
//	public void init(JavacTask task, String... args) {
//		Context context = ((BasicJavacTask) task).getContext();
//		task.addTaskListener(new TaskListener() {
//			@Override
//			public void started(TaskEvent e) {
//			}
//
//			@Override
//			public void finished(TaskEvent e) {
//				if (e.getKind() != TaskEvent.Kind.PARSE) {
//					return;
//				}
//				
//				
//					e.getCompilationUnit().accept(new TreeScanner<Void, Void>() {
//						@Override
//						public Void visitMethod(MethodTree method, Void v) {
//							System.out.println("M!" + e.getKind());
//							List<VariableTree> parametersToInstrument = method.getParameters().stream()
//									.filter(JorelsPlugin.this::shouldInstrument).collect(Collectors.toList());
//							if (!parametersToInstrument.isEmpty()) {
//								// There is a possible case that more than one
//								// argument is marked by @Positive,
//								// as the checks are added to the method's body
//								// beginning, we process parameters RTL
//								// to ensure correct order.
//								Collections.reverse(parametersToInstrument);
//								parametersToInstrument.forEach(p -> addCheck(method, p, context));
//							}
//							// There is a possible case that there is a nested class
//							// declared in a method's body,
//							// hence, we want to proceed with method body AST as
//							// well.
//							return super.visitMethod(method, v);
//						}
//					}, null);
//				
//				
//				
//				
//			}
//		});
//	}
//
//	private boolean shouldInstrument(VariableTree parameter) {
//		return TARGET_TYPES.contains(parameter.getType().toString()) && parameter.getModifiers().getAnnotations()
//				.stream().anyMatch(a -> Positive.class.getSimpleName().equals(a.getAnnotationType().toString()));
//	}
//
//	private void addCheck(MethodTree method, VariableTree parameter, Context context) {
//		
//		TreeMaker maker = TreeMaker.instance(context);
//		Names symbolsTable = Names.instance(context);
//		Name parameterId = symbolsTable.fromString(parameter.getName().toString());
//		
//		JCBinary condition =  maker.Binary(JCTree.Tag.LE, maker.Ident(parameterId), maker.Literal(TypeTag.INT, 0));
//		
//		String parameterName = parameter.getName().toString();
//
//		String errorMessagePrefix = String.format("Argument '%s' of type %s is marked by @%s but got '", parameterName,
//				parameter.getType(), Positive.class.getSimpleName());
//		String errorMessageSuffix = "' for it";
//
//		JCBlock block = maker.Block(0, com.sun.tools.javac.util.List.of(maker.Throw(maker.NewClass(null, nil(),
//				maker.Ident(symbolsTable.fromString(IllegalArgumentException.class.getSimpleName())),
//				com.sun.tools.javac.util.List.of(maker.Binary(JCTree.Tag.PLUS,
//						maker.Binary(JCTree.Tag.PLUS, maker.Literal(TypeTag.CLASS, errorMessagePrefix),
//								maker.Ident(parameterId)),
//						maker.Literal(TypeTag.CLASS, errorMessageSuffix))),
//				null))));
//		
//		JCIf check = maker.at(((JCTree) parameter).pos).If(maker.Parens(condition), block, null);
//		
//		
//		
//		JCTree.JCBlock body = (JCTree.JCBlock) method.getBody();
//		body.stats = body.stats.prepend(check);
//		System.out.println("a");
//		for(JCStatement o : body.stats) {
//			System.out.println(o);
//		}
//	}

	@Override
	//call()
	public void init(JavacTask task, String... args) {
		//task.addTaskListener(new MyTaskListener(task));

		Context context = ((BasicJavacTask) task).getContext();
		
		task.addTaskListener(new TaskListener() {

//			PositiveIntModifier modifier = new PositiveIntModifier(((BasicJavacTask)
//					task).getContext());

			@Override
			public void finished(TaskEvent taskEvent) {
				if (taskEvent.getKind() == TaskEvent.Kind.PARSE) {
					//modifier.scan(taskEvent.getCompilationUnit(), null);
					taskEvent.getCompilationUnit().accept(new TreeScanner<Void, Void>() {

						///
						
						@Override
						public Void visitMethod(MethodTree methodTree, Void p) {
							
							//TreeTranslator.result;
							
							//methodTree.accept(this, null);
							
							
							TreeMaker maker = TreeMaker.instance(context);
							Name parameterId = Names.instance(context).fromString("i"); //parameter.getName().toString(), where parameter instanceof VariableTree
							
							//LE = Less than or Equal to
							JCBinary CONDITION = maker.Binary(JCTree.Tag.LE, maker.Ident(parameterId), maker.Literal(TypeTag.INT, 0));
							
							
							
							JCExpression AIE = maker.Ident(Names.instance(context).fromString(IllegalArgumentException.class.getSimpleName()));
							
							
							JCBlock BLOCK = maker.Block(0, com.sun.tools.javac.util.List.of(
									maker.Throw(
											maker.NewClass(null, com.sun.tools.javac.util.List.nil(), AIE,
													com.sun.tools.javac.util.List.of(
															//(yo + IAE) + nope
															maker.Binary(JCTree.Tag.PLUS, 
																	//yo + IAE
																	maker.Binary(JCTree.Tag.PLUS, 
																			maker.Literal(TypeTag.CLASS, "yo"), 
																			maker.Ident(parameterId)), 
																	maker.Literal(TypeTag.CLASS, "nope")
																)), null))));
							
							for(StatementTree statement : methodTree.getBody().getStatements()) {
								if(statement.getKind().equals(Tree.Kind.VARIABLE)) {
									VariableTree varTree = (VariableTree) statement;
									if(varTree.getName().toString().equals("i")) {
										System.out.println("Preparing to instrument i!");
										
										JCIf ifStatement = maker.at(((JCTree) varTree).pos).If(maker.Parens(CONDITION), BLOCK, null);
										
										//System.out.println(ifStatement);
										
										
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
						
						///
						
					}, null);
					
				}
			}

			@Override
			public void started(TaskEvent var1) {}
		});
	}

	static class MyTaskListener implements TaskListener {

		private final MyTreeVisitor visitor;
		private final Context context;

		MyTaskListener(JavacTask task) {
			context = ((BasicJavacTask) task).getContext();
			visitor = new MyTreeVisitor(task, context);
		}

		@Override
		public void finished(TaskEvent taskEvent) {
			/*
			 * PARSE – builds an Abstract Syntax Tree (AST)
	 ENTER – source code imports are resolved
	 ANALYZE – parser output (an AST) is analyzed for errors
	 GENERATE – generating binaries for the target source file
			 */
			if (taskEvent.getKind() == TaskEvent.Kind.ANALYZE) {

			}

			switch(taskEvent.getKind()) {
				case ANALYZE:
					System.out.println("== ANALYSE ==");
					CompilationUnitTree compilationUnit = taskEvent.getCompilationUnit();
					visitor.scan(compilationUnit, null);
					break;
				case ANNOTATION_PROCESSING:
				case ANNOTATION_PROCESSING_ROUND:
				case ENTER:
				case GENERATE:
				case PARSE:
					System.out.println("== " + taskEvent.getKind().name() + " ==");
					break;
				default:
					break;

			}



			/*
			 *
	 TreeMaker factory = TreeMaker.instance(context);
	 Names symbolsTable = Names.instance(context);
			 */

		}

		@Override
		public void started(TaskEvent taskEvent) {

		}
	}

}
