import io.github.jorelali.javacompilerplugins.annotations.ReflectionMethod;

public class MyMain {

	public static void main(String[] args) throws Exception {
		sayHi();
	}
	
	@ReflectionMethod(targetClass = ExampleClass.class)
	public static void sayHi() {}
}
