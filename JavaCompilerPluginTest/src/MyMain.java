import java.lang.reflect.Field;

import io.github.jorelali.javacompilerplugins.annotations.ReflectionField;
import io.github.jorelali.javacompilerplugins.annotations.ReflectionMethod;

public class MyMain {

	@SuppressWarnings("unused")
	public static void main(String[] args) throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
		
		ExampleClass myExampleClass = new ExampleClass();

		//This isn't allowed, because myString is private
		//String str = myExampleClass.myString;
		
		//So you'd use reflection:
		
		//Old method to get a private field from 
		Field field = myExampleClass.getClass().getDeclaredField("myString");
		field.setAccessible(true);
		String stringValue = (String) field.get(myExampleClass);
		
		//My method concept, using the compiler
		@ReflectionField(fieldName = "myString", targetClass = ExampleClass.class) 
		String val = (String) (Object) myExampleClass;
		
	}
	
	@ReflectionMethod(targetClass = ExampleClass.class)
	public void sayHi() {

		
		
		
	}
	
	
	
	

}
