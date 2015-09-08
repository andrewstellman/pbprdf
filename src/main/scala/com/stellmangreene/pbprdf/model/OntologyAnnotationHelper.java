package com.stellmangreene.pbprdf.model;

import java.lang.reflect.Field;

/**
 * Utility class to help with ontology annotations using Java reflection
 * (because Scala reflection is just a little too clumsy)
 * 
 * @author andrewstellman
 */
public class OntologyAnnotationHelper {

	public static OntologyClass getOntologyClassAnnotation(Field field) {
		return field.getAnnotation(OntologyClass.class);
	}

	public static OntologySubClassOf getOntologySubClassOfAnnotation(Field field) {
		return field.getAnnotation(OntologySubClassOf.class);
	}

	public static OntologyProperty getOntologyPropertyAnnotation(Field field) {
		return field.getAnnotation(OntologyProperty.class);
	}
	
	public static Boolean isObjectProperty(Field field) {
		return field.getAnnotation(OntologyObjectProperty.class) != null;
	}
	
	public static String getComment(Field field) {
		OntologyComment ontologyComment = field.getAnnotation(OntologyComment.class);
		if (ontologyComment == null)
			return null;
		else
			return ontologyComment.comment();
	}

}
