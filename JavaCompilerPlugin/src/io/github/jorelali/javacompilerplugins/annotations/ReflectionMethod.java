package io.github.jorelali.javacompilerplugins.annotations;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.CLASS;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Retention(CLASS)
@Target({ METHOD })
public @interface ReflectionMethod {

	Class<?> targetClass();
	boolean isPrivate() default false;
	
}
