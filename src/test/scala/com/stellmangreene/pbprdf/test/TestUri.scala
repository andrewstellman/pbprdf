package com.stellmangreene.pbprdf.test

import org.eclipse.rdf4j.model.impl.SimpleValueFactory
import org.eclipse.rdf4j.model.URI

object TestUri {
  def create(s: String): URI = SimpleValueFactory.getInstance().createURI(s"http://stellman-greene.com/pbprdf/${s}")
}