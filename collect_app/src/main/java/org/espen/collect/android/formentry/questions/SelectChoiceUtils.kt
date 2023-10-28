package org.espen.collect.android.formentry.questions

import org.javarosa.core.model.SelectChoice
import org.javarosa.form.api.FormEntryPrompt
import org.javarosa.xpath.parser.XPathSyntaxException
import org.espen.collect.android.exception.ExternalDataException
import org.espen.collect.android.externaldata.ExternalDataUtil
import org.espen.collect.android.fastexternalitemset.ItemsetDao
import org.espen.collect.android.fastexternalitemset.ItemsetDbAdapter
import org.espen.collect.android.fastexternalitemset.XPathParseTool
import org.espen.collect.android.javarosawrapper.FormController
import org.odk.collect.lookup.LookUpRepository
import java.io.FileNotFoundException

object SelectChoiceUtils {

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
        return org.espen.collect.android.externaldata.ExternalDataUtil.populateLookupChoices(prompt,formController, lookupRepository, )
    }

    private fun isLookUpUsed(prompt: FormEntryPrompt): Boolean {
        return prompt.bindAttributes.filter { e-> e.name.equals("db_get") }.isNotEmpty()
    }


    private fun isFastExternalItemsetUsed(prompt: FormEntryPrompt): Boolean {
        val questionDef = prompt.question
        return questionDef?.getAdditionalAttribute(null, "query") != null
    }

    private fun isSearchPulldataItemsetUsed(prompt: FormEntryPrompt): Boolean {
        return org.espen.collect.android.externaldata.ExternalDataUtil.getSearchXPathExpression(prompt.appearanceHint) != null
    }

    @Throws(FileNotFoundException::class, XPathSyntaxException::class)
    private fun readFastExternalItems(prompt: FormEntryPrompt, formController: FormController): List<SelectChoice> {
        return org.espen.collect.android.fastexternalitemset.ItemsetDao(ItemsetDbAdapter()).getItems(prompt, org.espen.collect.android.fastexternalitemset.XPathParseTool(), formController)
    }

    @Throws(FileNotFoundException::class, org.espen.collect.android.exception.ExternalDataException::class)
    private fun readSearchPulldataItems(prompt: FormEntryPrompt, formController: FormController): List<SelectChoice> {
        // SurveyCTO-added support for dynamic select content (from .csv files)
        val xpathFuncExpr =
            org.espen.collect.android.externaldata.ExternalDataUtil.getSearchXPathExpression(prompt.appearanceHint)
        return org.espen.collect.android.externaldata.ExternalDataUtil.populateExternalChoices(prompt, xpathFuncExpr, formController)
    }
}
