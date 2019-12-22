package abyss.extensionPoints

import abyss.model.tree.nodes.MppAuthorityZoneNode
import abyss.psi.SharedTreeProvider
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.StartupActivity
import com.intellij.util.messages.Topic
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import javax.naming.Context

class CustomStartupActivity : StartupActivity {
    override fun runActivity(project: Project) {
        println("Project ${project.name} started")
        launchSharedElementsUpdating(project)
    }

    private fun launchSharedElementsUpdating(project: Project) {
        GlobalScope.launch {
            val nodes = SharedTreeProvider().sharedElements(project)

            val myBus = project.messageBus
            val publisher: SharedElementsTopicsNotifier =
                myBus.syncPublisher(SharedElementsTopics.SHARED_ELEMENTS_TREE_TOPIC)
            publisher.sharedElementsUpdated(nodes)
        }
    }

}

class ExperimentalEventsProducer {
    private fun runTestWithCoroutine(project: Project) {
        GlobalScope.launch {
            val s = SharedTreeProvider().suspendedStringExperiment(project)
            println("string generated")
            val myBus = project.messageBus
            val publisher: TopicsNotifier = myBus.syncPublisher(SharedElementsTopics.EXPERIMENTAL_TOPIC)
            publisher.stringUpdated(s)
            println("string published")
        }
    }

    private fun runTest(project: Project) {
        SharedTreeProvider().experimentString(project) { str ->
            println("string generated")
            val myBus = project.messageBus
            val publisher: TopicsNotifier = myBus.syncPublisher(SharedElementsTopics.EXPERIMENTAL_TOPIC)
            publisher.stringUpdated(str)
            println("string published")
        }
    }

    public fun doChange(project: Project, context: Context) {
        val myBus = project.messageBus
        val publisher: TopicsNotifier = myBus.syncPublisher(SharedElementsTopics.EXPERIMENTAL_TOPIC)
        publisher.beforeAction(context)
        try {

        } finally {
            publisher.afterAction(context)
        }
    }
}

class SharedElementsTopics {
    companion object {
        var SHARED_ELEMENTS_TREE_TOPIC: Topic<SharedElementsTopicsNotifier> =
            Topic.create("custom name", SharedElementsTopicsNotifier::class.java)
        var EXPERIMENTAL_TOPIC: Topic<TopicsNotifier> =
            Topic.create("custom name", TopicsNotifier::class.java)

    }
}

interface SharedElementsTopicsNotifier {
    fun sharedElementsUpdated(nodes: List<MppAuthorityZoneNode>)
}

interface TopicsNotifier {
    fun stringUpdated(string: String)

    fun beforeAction(context: Context)

    fun afterAction(context: Context)
}