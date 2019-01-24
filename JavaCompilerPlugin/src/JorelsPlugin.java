import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.util.JavacTask;
import com.sun.source.util.Plugin;
import com.sun.source.util.TaskEvent;
import com.sun.source.util.TaskListener;
import com.sun.tools.javac.api.BasicJavacTask;
import com.sun.tools.javac.util.Context;

public class JorelsPlugin implements Plugin {

	@Override
	public String getName() {
		return "JorelsPlugin";
	}

	@Override
	//call()
	public void init(JavacTask task, String... args) {
		System.out.println("Running!");
		task.addTaskListener(new MyTaskListener(task));
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
				CompilationUnitTree compilationUnit = taskEvent.getCompilationUnit();
				visitor.scan(compilationUnit, null);
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