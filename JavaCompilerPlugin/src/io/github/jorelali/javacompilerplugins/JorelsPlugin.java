package io.github.jorelali.javacompilerplugins;
import com.sun.source.util.JavacTask;
import com.sun.source.util.Plugin;
import com.sun.source.util.TaskEvent;
import com.sun.source.util.TaskListener;

import io.github.jorelali.javacompilerplugins.treescanners.ReflectionGeneratorTreeScanner;
import io.github.jorelali.javacompilerplugins.treescanners.ReflectionImportTreeScanner;

public class JorelsPlugin implements Plugin {

	@Override
	public String getName() {
		return "JorelsPlugin";
	}
	
	@Override
	public void init(JavacTask task, String... args) {

		task.addTaskListener(new TaskListener() {

			@Override
			public void finished(TaskEvent taskEvent) {
				if (taskEvent.getKind() == TaskEvent.Kind.PARSE) {
					//taskEvent.getCompilationUnit().accept(new ReflectionImportTreeScanner(task), null);
					taskEvent.getCompilationUnit().accept(new ReflectionGeneratorTreeScanner(task), null);
					//taskEvent.getCompilationUnit().accept(new TestingTreeScanner(task), null);
				}
			}

			@Override
			public void started(TaskEvent taskEvent) {}
		});
	}

}
