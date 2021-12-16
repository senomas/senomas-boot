package com.senomas.boot;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Audited {
	
	String value() default "";

	public class Key {}

	public class Brief extends Key {}

	public class Detail extends Brief {}
}
