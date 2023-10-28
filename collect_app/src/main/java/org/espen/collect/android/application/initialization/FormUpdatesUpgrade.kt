package org.espen.collect.android.application.initialization

import org.espen.collect.android.backgroundwork.FormUpdateScheduler
import org.odk.collect.async.Scheduler
import org.odk.collect.projects.ProjectsRepository
import org.odk.collect.upgrade.Upgrade

class FormUpdatesUpgrade(
    private val scheduler: Scheduler,
    private val projectsRepository: ProjectsRepository,
    private val formUpdateScheduler: org.espen.collect.android.backgroundwork.FormUpdateScheduler
) : Upgrade {

    override fun key(): String? {
        return null
    }

    override fun run() {
        scheduler.cancelAllDeferred()

        projectsRepository.getAll().forEach {
            formUpdateScheduler.scheduleUpdates(it.uuid)
        }
    }
}
