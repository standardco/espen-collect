package org.espen.collect.android.formentry.questions

import org.javarosa.core.model.SelectChoice
import org.javarosa.form.api.FormEntryPrompt
import org.javarosa.measure.Measure
import org.javarosa.xpath.parser.XPathSyntaxException
import org.espen.collect.android.dynamicpreload.ExternalDataUtil
import org.espen.collect.android.exception.ExternalDataException
import org.espen.collect.android.fastexternalitemset.ItemsetDao
import org.espen.collect.android.fastexternalitemset.ItemsetDbAdapter
import org.espen.collect.android.fastexternalitemset.XPathParseTool
import org.espen.collect.android.javarosawrapper.FormController
import java.io.FileNotFoundException
import org.odk.collect.lookup.LookUpRepository

object SelectChoiceUtils {

    @JvmStatic
    @Throws(FileNotFoundException::class, XPathSyntaxException::class, ExternalDataException::class)
    fun loadSelectChoices(prompt: FormEntryPrompt, formController: FormController): List<SelectChoice> {
        Measure.log("LoadSelectChoices")

        return when {
            isFastExternalItemsetUsed(prompt) -> readFastExternalItems(prompt, formController)
            isSearchPulldataItemsetUsed(prompt) -> readSearchPulldataItems(prompt, formController)
            else -> prompt.selectChoices ?: emptyList()
        }
    }

    @JvmStatic
    @Throws(FileNotFoundException::class, XPathSyntaxException::class, org.espen.collect.android.exception.ExternalDataException::class)
    fun loadSelectChoices(prompt: FormEntryPrompt, formController: FormController, lookupRepository: LookUpRepository): List<SelectChoice> {
        return when {
            isLookUpUsed(prompt) -> readLookupRepository(prompt, formController, lookupRepository)
            isFastExternalItemsetUsed(prompt) -> readFastExternalItems(prompt, formController)
            isSearchPulldataItemsetUsed(prompt) -> readSearchPulldataItems(prompt, formController)
            else -> prompt.selectChoices
        }
    }

    private fun readLookupRepository(prompt: FormEntryPrompt, formController: FormController, lookupRepository: LookUpRepository): List<SelectChoice> {
        return ExternalDataUtil.populateLookupChoices(prompt,formController, lookupRepository, )
    }

    private fun isLookUpUsed(prompt: FormEntryPrompt): Boolean {
        return prompt.bindAttributes.filter { e-> e.name.equals("db_get") }.isNotEmpty()
    }

    private fun isFastExternalItemsetUsed(prompt: FormEntryPrompt): Boolean {
        val questionDef = prompt.question
        return questionDef?.getAdditionalAttribute(null, "query") != null
    }

    private fun isSearchPulldataItemsetUsed(prompt: FormEntryPrompt): Boolean {
        return ExternalDataUtil.getSearchXPathExpression(prompt.appearanceHint) != null
    }

    @Throws(FileNotFoundException::class, XPathSyntaxException::class)
    private fun readFastExternalItems(prompt: FormEntryPrompt, formController: FormController): List<SelectChoice> {
        return ItemsetDao(ItemsetDbAdapter()).getItems(prompt, XPathParseTool(), formController)
    }

    @Throws(FileNotFoundException::class, ExternalDataException::class)
    private fun readSearchPulldataItems(prompt: FormEntryPrompt, formController: FormController): List<SelectChoice> {
        // SurveyCTO-added support for dynamic select content (from .csv files)
        val xpathFuncExpr =
            ExternalDataUtil.getSearchXPathExpression(prompt.appearanceHint)
        return ExternalDataUtil.populateExternalChoices(prompt, xpathFuncExpr, formController)
    }
}
