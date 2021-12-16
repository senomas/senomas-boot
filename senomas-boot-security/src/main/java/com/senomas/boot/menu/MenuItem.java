package com.senomas.boot.menu;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD) 
public @interface MenuItem {
	
	int order() default 100;
	
	String id();
	
	String path();
	
	String authorize() default "";
}
