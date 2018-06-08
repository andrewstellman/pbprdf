package com.stellmangreene.pbprdf.test

import org.eclipse.rdf4j.model.impl.SimpleValueFactory
import org.eclipse.rdf4j.model.IRI

object TestIri {
  def create(s: String): IRI = SimpleValueFactory.getInstance().createIRI(s"http://stellman-greene.com/pbprdf/${s}")
}