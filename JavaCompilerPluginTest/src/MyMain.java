import io.github.jorelali.javacompilerplugins.annotations.ReflectionField;
import io.github.jorelali.javacompilerplugins.annotations.ReflectionMethod;

public class MyMain {

	public static void main(String[] args) throws Exception {
		
		@ReflectionField(targetClass = ExampleClass.class) String myString = null;
		System.out.println(myString);
		
		sayHi();
	}
	
	@ReflectionMethod(targetClass = ExampleClass.class, isPrivate = true)
	public static void sayHi() {}
}
