package com.stellmangreene.pbprdf.test

import org.openrdf.model.impl.ValueFactoryImpl
import org.openrdf.model.URI

object TestUri {
  def create(s: String): URI = ValueFactoryImpl.getInstance().createURI(s"http://stellman-greene.com/pbprdf/${s}")
}