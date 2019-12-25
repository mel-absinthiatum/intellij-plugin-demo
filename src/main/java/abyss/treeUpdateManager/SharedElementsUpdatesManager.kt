package abyss.treeUpdateManager

import abyss.extensionPoints.SharedElementsTopics
import abyss.extensionPoints.SharedElementsTopicsNotifier
import com.intellij.openapi.project.Project
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

interface SharedElementsUpdatesManagerInterface {
    // TODO: Use interface
}


class SharedElementsUpdatesManager: SharedElementsUpdatesManagerInterface {

    private val timers = HashMap<Project, Job>()

    fun launchUpdatesTimer(project: Project, interval: Long) {
        val timer: Job = startCoroutineTimer(delayMillis = 0, repeatMillis = interval) {
            updateSharedTreeRoot(project)
            println("update all by timer")
        }
        timers[project] = timer
    }

    fun updateSharedTreeRoot(project: Project) {
        GlobalScope.launch {
            val root = SharedTreeProvider().sharedTreeRoot(project)

            val myBus = project.messageBus
            val publisher: SharedElementsTopicsNotifier =
                myBus.syncPublisher(SharedElementsTopics.SHARED_ELEMENTS_TREE_TOPIC)
            publisher.sharedElementsUpdated(root)
        }
    }

    private fun startCoroutineTimer(delayMillis: Long = 0, repeatMillis: Long = 0, action: () -> Unit) = GlobalScope.launch {
        delay(delayMillis)
        if (repeatMillis > 0) {
            while (true) {
                action()
                delay(repeatMillis)
            }
        } else {
            action()
        }
    }
}




/*    fun subscribeForModulesUpdates(project: Project) {
        project.getMessageBus().connect().subscribe(ProjectTopics.MODULES, object : ModuleListener {
            fun moduleAdded(project: Project, module: Module) {
                // TODO: Handle module routines.
            }
        })

    }*/