package abyss.model.tree.nodes

import java.util.*
import javax.swing.tree.TreeNode


interface CustomNodeInterface: TreeNode {
    fun removeNodeParent()
}

interface TemplateNodeInterface<M: NodeModel, P: CustomNodeInterface, C: CustomNodeInterface>: CustomNodeInterface {
    var model: M

    var nodeParent: P?

    fun add(node: C)

    fun add(nodes: List<C>)

    fun remove(node: C)
}

interface NodeModel

abstract class TemplateNode<M: NodeModel, P: CustomNodeInterface, C: CustomNodeInterface>(
    override var model: M,
    override var nodeParent: P? = null,
    val children: MutableList<C> = mutableListOf()
): TemplateNodeInterface<M, P, C>{

    override fun add(node: C) { children.add(node) }

    override fun add(nodes: List<C>) { children.addAll(nodes)}

    override fun remove(node: C) {
        node.removeNodeParent()
        children.remove(node)
    }

    override fun children(): Enumeration<out TreeNode> = children.toEnumeration()

    override fun isLeaf(): Boolean = childCount == 0

    override fun getChildCount(): Int = children.size

    override fun getParent(): TreeNode? = nodeParent

    override fun getChildAt(childIndex: Int): TreeNode? = children[childIndex]

    override fun getIndex(node: TreeNode?): Int = children.indexOfFirst { node == it }

    override fun removeNodeParent() {
        nodeParent = null
    }

    override fun getAllowsChildren(): Boolean = true

}


interface SharedElementContent: CustomNodeInterface

// TODO: add abstract classes for root and leaf nodes
class ExpectOrActualNode1(model: ExpectOrActualModel)
    : TemplateNode<ExpectOrActualModel, SharedElementNode1, Nothing>(model),
    SharedElementContent {
    override fun getAllowsChildren(): Boolean = false

    override fun add(node: Nothing) { assert(false) { "Not allowed." } }

    override fun add(nodes: List<Nothing>) { assert(false) { "Not allowed." } }

    override fun remove(node: Nothing) { assert(false) { "Not allowed." } }

    override fun children(): Enumeration<out TreeNode> = emptyEnumeration()

    override fun isLeaf(): Boolean = true

    override fun getChildCount(): Int = 0

    override fun getChildAt(childIndex: Int): TreeNode? {
        assert(false) { "Not allowed." }
        return null
    }

    override fun getIndex(node: TreeNode?): Int {
        assert(false) { "Not allowed." }
        return -1
    }
}

class SharedElementNode1(model: SharedElementModel)
    : TemplateNode<SharedElementModel, CustomNodeInterface, SharedElementContent>(model),
    SharedElementContent

class PackageNode1(model: PackageModel)
    : TemplateNode<PackageModel, MppAuthorityZoneNode1, SharedElementNode1>(model)

class MppAuthorityZoneNode1(model: MppAuthorityZoneModel)
    : TemplateNode<MppAuthorityZoneModel, CustomNodeInterface, PackageNode1>(model)

class RootNode: TemplateNode<NodeModel, Nothing, MppAuthorityZoneNode1>(rootNodeModel) {
    override var nodeParent: Nothing? = null
    override fun getParent(): TreeNode? = null
    override fun removeNodeParent() {}
}

object rootNodeModel: NodeModel

