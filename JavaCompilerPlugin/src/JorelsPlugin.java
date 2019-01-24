import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.util.JavacTask;
import com.sun.source.util.Plugin;
import com.sun.source.util.TaskEvent;
import com.sun.source.util.TaskListener;
import com.sun.tools.javac.api.BasicJavacTask;
import com.sun.tools.javac.processing.JavacProcessingEnvironment;
import com.sun.tools.javac.util.Context;

public class JorelsPlugin implements Plugin {

	@Override
	public String getName() {
		return "JorelsPlugin";
	}

	
	
	@Override
	//call()
	public void init(JavacTask task, String... args) {
		//task.addTaskListener(new MyTaskListener(task));
		 
		task.addTaskListener(new TaskListener() {

			PositiveIntModifier modifier = new PositiveIntModifier(((BasicJavacTask) task).getContext());
			
			@Override
			public void finished(TaskEvent taskEvent) {
				if (taskEvent.getKind() == TaskEvent.Kind.ANALYZE) {
					modifier.scan(taskEvent.getCompilationUnit(), null);
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
			 * 	PARSE – builds an Abstract Syntax Tree (AST)
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
