package abyss.extensionPoints

import abyss.treeUpdateManager.SharedElementsUpdatesManager
import abyss.treeUpdateManager.SharedTreeProvider
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.StartupActivity
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import javax.naming.Context

class CustomStartupActivity : StartupActivity {
    override fun runActivity(project: Project) {
        println("Project ${project.name} started")
        SharedElementsUpdatesManager().updateSharedTreeRoot(project)
    }
}

class ExperimentalEventsProducer {
    private fun runTestWithCoroutine(project: Project) {
        GlobalScope.launch {
            val s = SharedTreeProvider().suspendedStringExperiment(project)
            println("string generated")
            val myBus = project.messageBus
            val publisher: ExperimentalTopicsNotifier = myBus.syncPublisher(SharedElementsTopics.EXPERIMENTAL_TOPIC)
            publisher.stringUpdated(s)
            println("string published")
        }
    }

    private fun runTest(project: Project) {
        SharedTreeProvider().experimentString(project) { str ->
            println("string generated")
            val myBus = project.messageBus
            val publisher: ExperimentalTopicsNotifier = myBus.syncPublisher(SharedElementsTopics.EXPERIMENTAL_TOPIC)
            publisher.stringUpdated(str)
            println("string published")
        }
    }

    public fun doChange(project: Project, context: Context) {
        val myBus = project.messageBus
        val publisher: ExperimentalTopicsNotifier = myBus.syncPublisher(SharedElementsTopics.EXPERIMENTAL_TOPIC)
        publisher.beforeAction(context)
        try {

        } finally {
            publisher.afterAction(context)
        }
    }
}
