import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import io.github.jorelali.javacompilerplugins.annotations.ReflectionField;
import io.github.jorelali.javacompilerplugins.annotations.ReflectionMethod;

public class MyMain {
	
	//cache

	@SuppressWarnings("unused")
	public static void main(String[] args) throws Exception {
		
		ExampleClass myExampleClass = new ExampleClass();
		
		

		//This isn't allowed, because myString is private
		//String str = myExampleClass.myString;
		
		//So you'd use reflection:
		
		//Old method to get a private field from 
		Field field = myExampleClass.getClass().getDeclaredField("myString");
		field.setAccessible(true);
		String stringValue = (String) field.get(myExampleClass);
		
		Method method = myExampleClass.getClass().getDeclaredMethod("sayHi");
		method.setAccessible(true);
		method.invoke(myExampleClass);
		
		{
			///
			{
				///
			}
		}
		
		//My method concept, using the compiler
		@ReflectionField(fieldName = "myString", targetClass = ExampleClass.class) 
		String val = (String) (Object) myExampleClass;
		
		MyMain main = new MyMain();
		main.sayHi();
	}
	
	@ReflectionMethod(targetClass = ExampleClass.class)
	public void sayHi() {
		
		
		//reflection to run ExampleClass.sayHi();
	}
	
	
	
	

}
