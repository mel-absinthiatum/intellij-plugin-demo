package abyss.model.tree.nodes

import java.util.*
import javax.swing.Icon
import javax.swing.tree.MutableTreeNode
import javax.swing.tree.TreeNode


interface NodeModel {
    fun getLabelText(): String
    fun getIcon(): Icon?
}

interface CustomNodeInterface : MutableTreeNode {
    fun removeNodeParent()
    fun nodeModel(): Any?
    fun childNodes(): List<CustomNodeInterface>
    fun nodeParent(): CustomNodeInterface?
}

interface TemplateNodeInterface<M : NodeModel, P : CustomNodeInterface, C : CustomNodeInterface> : CustomNodeInterface {
    var model: M

    var nodeParent: P?

    fun add(node: C)

    fun add(nodes: List<C>)

    fun remove(node: C)

    fun remove(nodes: List<C>)
}

abstract class TemplateNode<M : NodeModel, P : CustomNodeInterface, C : CustomNodeInterface>(
    override var model: M,
    override var nodeParent: P? = null,
    val children: MutableList<C> = mutableListOf()
) : TemplateNodeInterface<M, P, C> {

    override fun childNodes(): List<C> = children

    override fun nodeModel(): Any? = model

    override fun nodeParent() = nodeParent

    override fun add(node: C) {
        children.add(node)
    }

    override fun add(nodes: List<C>) {
        children.addAll(nodes)
    }

    override fun remove(node: C) {
        node.removeNodeParent()
        children.removeIf { it == node }
    }

    override fun remove(nodes: List<C>) {
        nodes.forEach { it.removeNodeParent() }
        children.removeAll(nodes)
    }

    override fun remove(index: Int) {
        //TODO remove node parent if necessary
        children.removeAt(index)
    }

    override fun remove(node: MutableTreeNode?) {
        //TODO remove node parent if necessary
        children.removeIf { it == node }
    }

    override fun insert(child: MutableTreeNode, index: Int) {
        val newNode = child as? C ?: throw Exception("wrong type")
        children.add(index, newNode)
    }

    override fun setParent(newParent: MutableTreeNode) {
        val newParentNode = newParent as? P ?: throw Exception("wrong type")
        nodeParent = newParentNode
    }

    override fun setUserObject(`object`: Any) {
        val newModel = `object` as? M ?: throw Exception("wrong type")
        model = newModel
    }

    override fun removeFromParent() {
        nodeParent?.remove(this)
        nodeParent = null
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

    fun including(vararg nodes: C): TemplateNode<M, P, C> {
        this.add(nodes.asList())
        return this
    }
}

abstract class TemplateLeaf<M : NodeModel, P : CustomNodeInterface>(model: M) : TemplateNode<M, P, Nothing>(model) {
    override fun getAllowsChildren(): Boolean = false

    override fun add(node: Nothing) {
        assert(false) { "Not allowed." }
    }

    override fun add(nodes: List<Nothing>) {
        assert(false) { "Not allowed." }
    }

    override fun remove(node: Nothing) {
        assert(false) { "Not allowed." }
    }

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

abstract class TemplateRootNode<M : NodeModel, C : CustomNodeInterface>(
    model: M
) : TemplateNode<M, Nothing, C>(model) {
    override var nodeParent: Nothing? = null

    override fun getParent(): TreeNode? = null

    override fun removeNodeParent() {}
}


interface SharedElementContent : CustomNodeInterface


class ExpectOrActualNode(model: ExpectOrActualModel) : TemplateLeaf<ExpectOrActualModel, SharedElementNode>(model),
    SharedElementContent

class SharedElementNode(model: SharedElementModel) :
    TemplateNode<SharedElementModel, CustomNodeInterface, SharedElementContent>(model),
    SharedElementContent

class PackageNode(model: PackageModel) : TemplateNode<PackageModel, MppAuthorityZoneNode, SharedElementNode>(model)

class MppAuthorityZoneNode(model: MppAuthorityZoneModel) :
    TemplateNode<MppAuthorityZoneModel, RootNode, PackageNode>(model)

class RootNode : TemplateRootNode<NodeModel, MppAuthorityZoneNode>(rootNodeModel)


object rootNodeModel : NodeModel {
    override fun getLabelText(): String {
        return ""
    }

    override fun getIcon(): Icon? {
        return null
    }
}

