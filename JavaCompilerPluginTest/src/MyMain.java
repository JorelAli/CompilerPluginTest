import io.github.jorelali.javacompilerplugins.annotations.ReflectionMethod;

public class MyMain {

	public static void main(String[] args) throws Exception {
		MyMain main = new MyMain();
		main.sayHi();
	}
	
	@ReflectionMethod(targetClass = ExampleClass.class)
	public void sayHi() {}
}
