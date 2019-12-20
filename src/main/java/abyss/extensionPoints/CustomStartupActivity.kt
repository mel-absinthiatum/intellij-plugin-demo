package abyss.extensionPoints

import abyss.psi.SharedTreeProvider
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.StartupActivity
import com.intellij.ui.treeStructure.Tree
import com.intellij.util.messages.Topic
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import javax.naming.Context

class CustomStartupActivity : StartupActivity {
    override fun runActivity(project: Project) {
        println("My project started!!! WOW! ${project.name}")
        launchTreeUpdating(project)
    }

    private fun launchTreeUpdating(project: Project) {
        val tree = SharedTreeProvider().tree(project)
        println("tree generated")
        val myBus = project.messageBus
        val publisher: SharedElementsTopicsNotifier = myBus.syncPublisher(SharedElementsTopics.CHANGE_ACTION_TOPIC)
        publisher.sharedElementsTreeUpdated(tree)
        println("tree published")
    }

    private fun runTestWithCoroutine(project: Project) {
        GlobalScope.launch {
            val s = SharedTreeProvider().suspendedStringExperiment(project)
            println("string generated")
            val myBus = project.messageBus
            val publisher: SharedElementsTopicsNotifier = myBus.syncPublisher(SharedElementsTopics.CHANGE_ACTION_TOPIC)
            publisher.stringUpdated(s)
            println("string published")
        }
    }

    private fun runTest(project: Project) {
        SharedTreeProvider().experimentString(project) { str ->
            println("string generated")
            val myBus = project.messageBus
            val publisher: SharedElementsTopicsNotifier = myBus.syncPublisher(SharedElementsTopics.CHANGE_ACTION_TOPIC)
            publisher.stringUpdated(str)
            println("string published")
        }
    }


    public fun doChange(project: Project, context: Context) {
        val myBus = project.messageBus
        val publisher: SharedElementsTopicsNotifier = myBus.syncPublisher(SharedElementsTopics.CHANGE_ACTION_TOPIC)
        publisher.beforeAction(context)
        try {

        } finally {
            publisher.afterAction(context)
        }
    }
}

class SharedElementsTopics {
    companion object {
        var CHANGE_ACTION_TOPIC: Topic<SharedElementsTopicsNotifier> =
            Topic.create("custom name", SharedElementsTopicsNotifier::class.java)
    }
}

interface SharedElementsTopicsNotifier {
    fun stringUpdated(string: String)

    fun sharedElementsTreeUpdated(tree: Tree)

    fun beforeAction(context: Context)

    fun afterAction(context: Context)
}