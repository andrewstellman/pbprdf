package com.stellmangreene.pbprdf.model;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface OntologyPrefix {
	public String prefix();
}
