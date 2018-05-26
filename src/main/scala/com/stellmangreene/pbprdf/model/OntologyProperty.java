package com.stellmangreene.pbprdf.model;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface OntologyProperty {
	public String label();

	public String domain() default "";

	public String[] domains() default {};

	public String range();
}
