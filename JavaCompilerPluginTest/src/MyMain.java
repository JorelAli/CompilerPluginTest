import io.github.jorelali.javacompilerplugins.annotations.ReflectionStaticField;
import io.github.jorelali.javacompilerplugins.annotations.ReflectionStaticMethod;

public class MyMain {

	public static void main(String[] args) throws Exception {
		
		@ReflectionStaticField(targetClass = ExampleClass.class, isPrivate = true) String myString = null;
		
		System.out.println(myString);
		
		sayHi();
	}
	
	@ReflectionStaticMethod(targetClass = ExampleClass.class, isPrivate = true)
	public static void sayHi() {}
}
