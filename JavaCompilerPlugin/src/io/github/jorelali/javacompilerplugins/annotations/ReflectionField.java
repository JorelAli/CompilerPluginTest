package io.github.jorelali.javacompilerplugins.annotations;

import static java.lang.annotation.ElementType.LOCAL_VARIABLE;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.SOURCE)
@Target({ LOCAL_VARIABLE })
public @interface ReflectionField {

	Class<?> targetClass();
	String withInstance();
	
}
