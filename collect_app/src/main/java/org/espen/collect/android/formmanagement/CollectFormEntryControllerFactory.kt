package org.espen.collect.android.formmanagement

import org.javarosa.core.model.FormDef
import org.javarosa.entities.EntityFormFinalizationProcessor
import org.javarosa.form.api.FormEntryController
import org.javarosa.form.api.FormEntryModel
import org.espen.collect.android.tasks.FormLoaderTask.FormEntryControllerFactory
import org.odk.collect.settings.keys.ProjectKeys
import org.odk.collect.shared.settings.Settings

class CollectFormEntryControllerFactory constructor(private val settings: Settings) :
    FormEntryControllerFactory {
    override fun create(formDef: FormDef): FormEntryController {
        return FormEntryController(FormEntryModel(formDef)).also {
            it.addPostProcessor(EntityFormFinalizationProcessor())

            if (!settings.getBoolean(ProjectKeys.KEY_PREDICATE_CACHING)) {
                it.disablePredicateCaching()
            }
        }
    }
}
