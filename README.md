# CompilerPluginTest
This project is a proof of concept that it is possible to create a Java Compiler Plugin to generate reflection by using custom annotations.

## Project structure
- [JavaCompilerPlugin](https://github.com/JorelAli/CompilerPluginTest/tree/master/JavaCompilerPlugin) is the main Javac plugin
- [JavaCompilerPluginTest](https://github.com/JorelAli/CompilerPluginTest/tree/master/JavaCompilerPluginTest) is a program which is compiled with the _JavaCompilerPlugin_ plugin

## Building and execution
### Building
- Add Java's `tools.jar` library to your classpath (This can be found in `%JAVA_HOME%/lib/tools.jar` if you have an installed JDK)
- Compile the JavaCompilerPlugin into a .jar file
- Add `META-INF/services/com.sun.source.util.Plugin` to the compiled .jar file

### Executing
- Compile the JavaCompilerPluginTest file with the following command: `javac -processorpath YOUR_JAR.jar -Xplugin:JorelsPlugin -classpath YOUR_JAR.jar MyMain.java ExampleClass.java`

## Concept
The concept is as follows:
Imagine you are a new developer learning Java and have heard of "reflection", but are not comfortable with using it, or do not know the syntax to perform reflection. You are however, comfortable with using annotations for variables and methods and decide to use custom annotations to handle the reflection access.

# Documentation (ish)

## Annotation declaration
This project relies on two major annotations:
### [ReflectionStaticField](https://github.com/JorelAli/CompilerPluginTest/blob/master/JavaCompilerPlugin/src/io/github/jorelali/javacompilerplugins/annotations/ReflectionStaticField.java) 
This annotation is used to declare access to a private static variable within another class.

For example, if you have a class `ExampleClass` with the following private static variable:
```java
public class ExampleClass {
    private static String privateString = "Hello, World!";
}
```
You would be able to access it from another class with the following code:
```java
//The variable name MUST be the same name as the variable in ExampleClass
@ReflectionStaticField(targetClass = ExampleClass.class) String privateString = null;

//Other code, for example printing the String
System.out.println(myString); //Prints "Hello, World!"

```

### [ReflectionStaticMethod](https://github.com/JorelAli/CompilerPluginTest/blob/master/JavaCompilerPlugin/src/io/github/jorelali/javacompilerplugins/annotations/ReflectionStaticMethod.java) 
This annotation is used to declare access to a private static method within another class.

For example, if you have a class `ExampleClass` with the following method declaration:
```java
private static void printHello() {
    System.out.println("Hello!");
}
```
You would be able to declare it within your own class and execute it, as shown below:
```java
//Declaration of the target private method. The method name MUST be the same as the name declared in ExampleClass
@ReflectionStaticMethod(targetClass = ExampleClass.class)
public static void printHello() {}

//Your own code
public static void myMethod() {
    printHello(); //Prints "Hello"
}
```

