package io.simplifier.msofficeadapter

import io.simplifier.msofficeadapter.word.{CustomAdapter, DefaultAdapter}

/**
  * Façade for custom and default adapter
  */
object MsOfficeAdapter {
  val customAdapter: CustomAdapter = new CustomAdapter()
  val defaultAdapter: DefaultAdapter = new DefaultAdapter(customAdapter)
  case class Replacements(replacements: Map[String, String], arrayReplacements: Seq[Seq[Map[String, String]]])
}