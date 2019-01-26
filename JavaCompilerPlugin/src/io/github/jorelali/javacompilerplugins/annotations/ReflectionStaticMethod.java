package io.github.jorelali.javacompilerplugins.annotations;

import static java.lang.annotation.ElementType.METHOD;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.SOURCE)
@Target({ METHOD })
public @interface ReflectionStaticMethod {

	Class<?> targetClass();
	
}
