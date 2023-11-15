package io.simplifier.msofficeadapter

object Exceptions {
  case class MsOfficeAdapterException(message: String, cause: Throwable) extends Exception(message, cause)
}
