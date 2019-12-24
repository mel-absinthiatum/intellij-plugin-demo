package abyss.model.tree.diff

import abyss.model.tree.nodes.CustomNodeInterface
import abyss.model.tree.nodes.NodeInterface
import javax.swing.tree.DefaultMutableTreeNode
import javax.swing.tree.TreeNode

class TreesDiffManager {
    fun diffs(oldTree: NodeInterface, newTree: NodeInterface) {
        newTree.children().toList()
    }

    private fun compareNodes(oldNode: NodeInterface, newNode: NodeInterface): List<TreeMutation> {
        val oldChildren = oldNode.children().toList()
        val newChildren = newNode.children().toList()
        val addedChildren = newChildren.subtract(oldChildren)
        val removedChildren = oldChildren.subtract(newChildren)
        val insertMutations = addedChildren.map { Insert(it, oldNode) }
        val removeMutations = removedChildren.map { Remove(it, oldNode) }

//        val otherMutations = compareNodes()
        return insertMutations + removeMutations
    }


    private fun v(oldNode: NodeInterface, newNode: NodeInterface): TreeNode? {
        // check mutations by subtract
        // make a collection of mutations

        // find intersect children array
        // run on each child, return node with mutations, ancestor of mutated node or null
        // we got a list of children or null


        // if we got null of children and null of mutations return null

        // return a node with a pointer to oldNode model and collection of mutations
        return null

    }

    private fun f(oldNode: CustomNodeInterface, newNode: CustomNodeInterface): DefaultMutableTreeNode? {
        val oldChildren = oldNode.childNodes().toList()
        val newChildren = newNode.childNodes().toList()
        val parent = oldNode.nodeParent()
        val model = oldNode.nodeModel()

        if (parent == null) {
            return null
        }
        if (model == null) {
            return null
        }

        val result = sift(oldChildren, newChildren, parent)
        val mutations = result.mutations
        val unchanged = result.unchanged

        val nodes = unchanged.mapNotNull { f(it.oldNode, it.newNode) }

        if (mutations.isEmpty() && nodes.isEmpty()) {
            return null
        }
        val diffNodeModel = DiffNodeModel(model, mutations)
        return DefaultMutableTreeNode(diffNodeModel)
    }

    private fun sift(
        oldChildren: List<CustomNodeInterface>,
        newChildren: List<CustomNodeInterface>,
        parent: CustomNodeInterface
    ): SiftResult {
        val mutableNewChildren = newChildren.toMutableList()
        val mutableOldChildren = oldChildren.toMutableList()
        val unchangedNodesTuples = mutableListOf<NodesTuple>()
        for (oldChild in mutableOldChildren) {
            for (newChild in mutableNewChildren) {
                if (oldChild.nodeModel() == newChild.nodeModel()) {
                    unchangedNodesTuples.add(NodesTuple(oldChild, newChild))
                    mutableOldChildren.remove(oldChild)
                    mutableNewChildren.remove(newChild)
                    continue
                }
            }
        }
        val mutations = mutableOldChildren.map { Remove(it, parent) } + mutableNewChildren.map { Insert(it, parent) }
        return SiftResult(mutations, unchangedNodesTuples)
    }

    class SiftResult(val mutations: List<TreeMutation>, val unchanged: List<NodesTuple>)

    class NodesTuple(val oldNode: CustomNodeInterface, val newNode: CustomNodeInterface)


    class DiffNodeModel<M : Any>(model: M, mutations: List<TreeMutation>)

//    fun perform(type: TreeMutation) {
//        when (type) {
//            TreeMutation.Insert -> {}
//            EditOperationType.MOVE -> {
//                srcNode.parent?.removeChild(srcNode)
//                dstNode!!.addChild(srcNode, placementIndex)
//                dstNode.refactorText()
//            }
//            EditOperationType.INSERT -> {
//                dstNode!!.addChild(srcNode, placementIndex)
//                dstNode.refactorText()
//            }
//            EditOperationType.DELETE -> {
//                val parent = srcNode.parent
//                parent?.removeChild(srcNode)
//                parent?.refactorText()
//            }
//        }
//    }
}

sealed class TreeMutation

data class Insert(val node: TreeNode, val parent: TreeNode) : TreeMutation()
data class Remove(val node: TreeNode, val parent: TreeNode) : TreeMutation()
data class Move(val node: TreeNode, val oldParent: TreeNode, val newParent: TreeNode) : TreeMutation()