package abyss.model.tree.nodes

import java.util.*
import javax.swing.tree.TreeNode

interface ElementNode : NodeInterface
data class SharedItemNode(
    var model: SharedElementModelInterface,
    override var nodeParent: NodeInterface? = null,
    var children: MutableList<ElementNode> = mutableListOf()
) : ElementNode {

//    init {
//        // TODO: Sort
////        children.sortWith(compareBy { it.name.toLowerCase() })
//    }


    fun addChild(node: ElementNode) {
        node.nodeParent = this
        children.add(node)
    }

    fun addChildren(nodes: List<ElementNode>) {
        nodes.forEach {
            it.nodeParent = this
            children.add(it)
        }
    }

    fun addChild(model: SharedElementModelInterface) {
        val node = SharedItemNode(model, this)
        children.add(node)
    }

    override fun children(): Enumeration<NodeInterface> {
        return children.toEnumeration()
    }

    override fun isLeaf(): Boolean {
        return childCount == 0
    }

    override fun getChildCount(): Int {
        return children.size
    }

    override fun getParent(): TreeNode? {
        return this.nodeParent
    }

    override fun getChildAt(childIndex: Int): TreeNode? {
        return children[childIndex]
    }

    override fun getIndex(node: TreeNode?): Int {
        return children.indexOfFirst { it == node }
    }

    override fun getAllowsChildren(): Boolean {
        return true
    }
}

data class ExpectOrActuaItemlNode(val model: ExpectOrActualModel, override var nodeParent: NodeInterface? = null) :
    ElementNode {
    override fun children(): Enumeration<NodeInterface> {
        return emptyEnumeration()
    }

    override fun isLeaf(): Boolean {
        return true
    }

    override fun getChildCount(): Int {
        return 0
    }

    override fun getParent(): TreeNode? {
        return this.nodeParent
    }

    override fun getChildAt(childIndex: Int): TreeNode? {
        assert(true)
        return null
    }

    override fun getIndex(node: TreeNode?): Int {
        assert(true)
        return 0
    }

    override fun getAllowsChildren(): Boolean {
        return false
    }
}
