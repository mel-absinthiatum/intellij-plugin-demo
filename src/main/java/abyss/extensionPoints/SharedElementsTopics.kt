package abyss.extensionPoints

import abyss.model.tree.nodes.RootNode
import com.intellij.util.messages.Topic
import javax.naming.Context

class SharedElementsTopics {
    companion object {
        var SHARED_ELEMENTS_TREE_TOPIC: Topic<SharedElementsTopicsNotifier> =
            Topic.create("custom name", SharedElementsTopicsNotifier::class.java)
        var EXPERIMENTAL_TOPIC: Topic<ExperimentalTopicsNotifier> =
            Topic.create("custom name", ExperimentalTopicsNotifier::class.java)

    }
}

interface SharedElementsTopicsNotifier {
    fun sharedElementsUpdated(root: RootNode)
}

interface ExperimentalTopicsNotifier {
    fun stringUpdated(string: String)

    fun beforeAction(context: Context)

    fun afterAction(context: Context)
}