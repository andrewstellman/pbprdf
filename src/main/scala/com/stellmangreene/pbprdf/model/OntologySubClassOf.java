package com.stellmangreene.pbprdf.model;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface OntologySubClassOf {
	public String[] subClassOf();
}
