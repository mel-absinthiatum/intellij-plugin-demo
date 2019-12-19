package abyss.model.tree.nodes

import java.util.*
import javax.swing.tree.TreeNode

interface CustomNodeInterface: TreeNode {
    fun removeNodeParent()
}

interface TemplateNodeInterface<C: CustomNodeInterface, P: CustomNodeInterface>: CustomNodeInterface {
    var nodeParent: P?

    fun add(node: C)

    fun add(nodes: List<C>)

    fun remove(node: C)
}


abstract class TemplateNode<C: CustomNodeInterface, P: CustomNodeInterface>(
    override var nodeParent: P? = null,
    val children: MutableList<C> = mutableListOf()
): TemplateNodeInterface<C, P>{

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

class ExpectOrActualNode1: TemplateNode<Nothing, SharedElementNode1>(), SharedElementContent {
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

class SharedElementNode1: TemplateNode<SharedElementContent, CustomNodeInterface>(), SharedElementContent {
}

class FileNode1: TemplateNode<SharedElementNode1, CustomNodeInterface>() {
}




interface NewNodeInterface<C: TreeNode, P: TreeNode>: TreeNode {
    var nodeParent: P?

    fun add(child: C)

    fun add(children: List<C>)

    fun remove(child: C)
}


class K: NewNodeInterface<ExpectOrActualNode, SharedItemNode> {
    override var nodeParent: SharedItemNode?
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
        set(value) {}

    override fun add(child: ExpectOrActualNode) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun add(children: List<ExpectOrActualNode>) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun remove(child: ExpectOrActualNode) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun children(): Enumeration<out TreeNode> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun isLeaf(): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getChildCount(): Int {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getParent(): TreeNode {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getChildAt(childIndex: Int): TreeNode {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getIndex(node: TreeNode?): Int {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getAllowsChildren(): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}