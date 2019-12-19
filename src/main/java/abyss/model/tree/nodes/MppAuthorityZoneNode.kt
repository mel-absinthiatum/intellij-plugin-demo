package abyss.model.tree.nodes

import java.util.*
import javax.swing.tree.TreeNode

interface MppAuthorityZoneModelInterface {
    val title: String
}

class MppAuthorityZoneModel(override val title: String): MppAuthorityZoneModelInterface, NodeModel

class MppAuthorityZoneNode (
    var model: MppAuthorityZoneModelInterface,
    override var nodeParent: NodeInterface? = null
): NodeInterface {
    val children = mutableListOf<PackageNode>()

    fun addChildren(nodes: List<PackageNode>) {
        nodes.forEach {
            it.nodeParent = this
            children.add(it)
        }
    }

    override fun children(): Enumeration<PackageNode> = children.toEnumeration()

    override fun isLeaf(): Boolean = childCount == 0

    override fun getChildCount(): Int = children.size

    override fun getParent(): TreeNode? = nodeParent

    override fun getChildAt(childIndex: Int): TreeNode = children[childIndex]

    override fun getIndex(node: TreeNode?): Int = children.indexOfFirst { it == node }

    override fun getAllowsChildren(): Boolean = true
}