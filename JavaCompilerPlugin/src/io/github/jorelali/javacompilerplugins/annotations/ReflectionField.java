package io.github.jorelali.javacompilerplugins.annotations;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.CLASS;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Retention(CLASS)
@Target({ FIELD, LOCAL_VARIABLE, PARAMETER, TYPE })
public @interface ReflectionField {

	Class<?> targetClass();
	String fieldName();
	boolean isPrivate() default false;
	
}
