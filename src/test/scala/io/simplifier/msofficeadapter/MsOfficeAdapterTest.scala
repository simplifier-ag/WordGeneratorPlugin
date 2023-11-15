package io.simplifier.msofficeadapter

import org.scalatest.{Matchers, WordSpec}

class MsOfficeAdapterTest extends WordSpec with Matchers{

  "MsOfficeAdapterTest" should {

    "have a customAdapter that is not null" in {
      MsOfficeAdapter.customAdapter should not be null
    }

    "have a defaultAdapter that is not null" in {
      MsOfficeAdapter.defaultAdapter should not be null
    }

  }
}
