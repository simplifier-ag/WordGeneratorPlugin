package io.simplifier.msofficeadapter.word

import io.simplifier.msofficeadapter.Exceptions.MsOfficeAdapterException
import io.simplifier.msofficeadapter.MsOfficeAdapter.Replacements
import org.mockito.Mockito._
import org.scalatest.mockito.MockitoSugar
import org.scalatest.{Matchers, WordSpec}

import scala.util.Try

class DefaultAdapterTest extends WordSpec with Matchers {
  "A DefaultAdapter" when {
    "filling templates" should {
      "call correctTemplate from the custom adapter exactly once" in new FillTemplatesFixture {
        intercept[MsOfficeAdapterException] {
          defaultAdapter.fillTemplates(data, replacementList)
        }
        verify(customAdapter,times(2)).correctTemplate(data, Some(replacementList.head))
      }
    }

    "call fillTemplates from the custom adapter exactly once" in new FillTemplatesFixture {
      customAdapter.fillTemplates(data, replacementList)
      verify(customAdapter).fillTemplates(data, replacementList)
    }
  }

  "filling a single template" should {
    "call correctTemplate from the custom adapter exactly once" in new FillTemplateFixture {
      intercept[MsOfficeAdapterException] {
        defaultAdapter.fillTemplate(data, replacements1)
      }
      verify(customAdapter,times(2)).correctTemplate(data, Some(replacements1))
    }

    "call fillTemplates from the custom adapter exactly once" in new FillTemplateFixture {
      intercept[MsOfficeAdapterException] {
        defaultAdapter.fillTemplate(data, replacements1)
      }
      verify(customAdapter,times(2)).correctTemplate(data, Some(replacements1))
    }

  }

  trait BaseFixture extends MockitoSugar {
    val customAdapter: CustomAdapter = mock[CustomAdapter]
    val defaultAdapter: DefaultAdapter = new DefaultAdapter(customAdapter)


    val data: Array[Byte] = Array()
    val replacements1: Replacements = Replacements(Map("key1" -> "value1"), Seq())
    val replacements2: Replacements = Replacements(Map("key1" -> "value1"), Seq())
    val replacements3: Replacements = Replacements(Map("key1" -> "value1"), Seq())
    val replacementList: List[Replacements] = List(replacements1, replacements2, replacements3)
  }

  trait FillTemplatesFixture extends MockitoSugar with BaseFixture {
    Try(when(defaultAdapter.fillTemplates(data, replacementList)).thenReturn(Seq(Array(1.toByte)))).toOption.getOrElse(Seq(Array(1.toByte)))
  }

  trait FillTemplateFixture extends BaseFixture {
    Try(when(defaultAdapter.fillTemplate(data, replacements1)).thenReturn(Array(1.toByte))).toOption.getOrElse(Array(1.toByte))
  }

}