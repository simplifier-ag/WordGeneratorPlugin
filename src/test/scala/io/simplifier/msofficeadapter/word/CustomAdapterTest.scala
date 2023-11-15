package io.simplifier.msofficeadapter.word

import io.simplifier.msofficeadapter.MsOfficeAdapter.Replacements
import io.simplifier.msofficeadapter.word.templates.correcttemplates.CorrectTemplatesHandler
import io.simplifier.msofficeadapter.word.templates.filltemplates.FillTemplatesHandler
import org.mockito.Mockito._
import org.scalatest.mockito.MockitoSugar
import org.scalatest.{Matchers, WordSpec}

class CustomAdapterTest extends WordSpec with Matchers {

  "CustomAdapterTest" when {
    "being instantiated" should {
      "take the default handlers" in new InstantiationFixture {
        val customAdapter = new CustomAdapter()
        customAdapter should not be null
      }
    }

    "correcting a template" should {
      "call handleTemplate in the correctTemplatesHandler only once" in new CorrectTemplateFixture {
        customAdapter.correctTemplate(data, None)
        verify(correctTemplatesHandler).handleTemplate(_, _, _)
      }
    }
  }

  trait BaseFixture extends MockitoSugar {
    val fillTemplatesHandler: FillTemplatesHandler = mock[FillTemplatesHandler]
    val correctTemplatesHandler: CorrectTemplatesHandler = mock[CorrectTemplatesHandler]
    val customAdapter: CustomAdapter = new CustomAdapter(fillTemplatesHandler, correctTemplatesHandler)
    val data: Array[Byte] = Array()
  }

  trait FillTemplatesFixture extends BaseFixture {
    val replacements1: Replacements = Replacements(Map("key1"->"value1"), Seq())
    val replacements2: Replacements = Replacements(Map("key2"->"value2"), Seq())
    val replacements3: Replacements = Replacements(Map("key3"->"value3"), Seq())
    val replacementList: List[Replacements] = List(replacements1, replacements2, replacements3)

  }

  trait FillTemplateFixture extends BaseFixture {
    val replacements1: Replacements = Replacements(Map("key1"->"value1"), Seq())
  }

  trait CorrectTemplateFixture extends BaseFixture

  trait InstantiationFixture

}
