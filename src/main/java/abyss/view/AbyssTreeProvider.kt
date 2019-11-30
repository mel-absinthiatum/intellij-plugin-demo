package abyss.view

import abyss.model.SharedItemModel
import com.intellij.ui.treeStructure.Tree
import toolWindow.tree.TreeNodeContentImpl
import javax.swing.tree.DefaultMutableTreeNode
import javax.swing.tree.DefaultTreeModel
import javax.swing.tree.MutableTreeNode
import javax.swing.tree.TreeModel

class AbyssTreeProvider {

    fun tree(model: Collection<SharedItemModel>): Tree {
        val model = treeModel(model)

        return Tree(model)
    }

    private fun treeModel(items: Collection<SharedItemModel>): TreeModel {
        var root = DefaultMutableTreeNode("root")

        for (item in items) {
            val node = makeSharedNode(item)
            root.add(node)
        }

        return DefaultTreeModel(root)
    }


    private fun makeSharedNode(sharedItemModel: SharedItemModel): MutableTreeNode {
        val rootNode = DefaultMutableTreeNode(TreeNodeContentImpl(sharedItemModel.metaInfo.name)).including(
            DefaultMutableTreeNode(TreeNodeContentImpl("expected")),
            DefaultMutableTreeNode(TreeNodeContentImpl("actual"))
        )

        return rootNode
    }

    private fun MutableTreeNode.including(nodes: Collection<MutableTreeNode>): MutableTreeNode {
        nodes.reversed().forEach {
            this.insert(it, 0)
        }
        return this
    }

    private fun MutableTreeNode.including(vararg nodes: MutableTreeNode): MutableTreeNode {
        nodes.reversed().forEach {
            this.insert(it, 0)
        }
        return this
    }
}