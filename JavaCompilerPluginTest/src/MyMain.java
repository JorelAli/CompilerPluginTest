import io.github.jorelali.javacompilerplugins.annotations.ReflectionField;
import io.github.jorelali.javacompilerplugins.annotations.ReflectionStaticField;
import io.github.jorelali.javacompilerplugins.annotations.ReflectionStaticMethod;

public class MyMain {

	public static void main(String[] args) throws Exception {
		
		@ReflectionStaticField(targetClass = ExampleClass.class) String myString = null;
		System.out.println(myString);
		
		ExampleClass instance = new ExampleClass();
		@ReflectionField(targetClass = ExampleClass.class, withInstance = "instance") String nonStaticString = null;
		System.out.println(nonStaticString);
		
		sayHi();
	}
	
	@ReflectionStaticMethod(targetClass = ExampleClass.class)
	public static void sayHi() {}
}
