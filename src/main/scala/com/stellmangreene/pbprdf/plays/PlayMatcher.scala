package com.stellmangreene.pbprdf.plays

import scala.util.matching.Regex

/**
 * Trait for all play companion objects
 *
 * @author andrewstellman
 */
trait PlayMatcher {
  val playByPlayRegex: Regex

  def matches(play: String) = {
    playByPlayRegex.pattern.matcher(play).matches
  }
}