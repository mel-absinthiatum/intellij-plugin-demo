package abyss.model.tree.nodes

import com.intellij.openapi.vfs.VirtualFile
import java.util.*
import javax.swing.tree.MutableTreeNode
import javax.swing.tree.TreeNode

// TODO: unify initialization
interface PackageContainable


interface PackageModelInterface: PackageContainable {
    val title: String
    val virtualFile: VirtualFile
}


data class PackageModel(
    override val title: String,
    override val virtualFile: VirtualFile
): PackageModelInterface


class PackageNode (
    val model: PackageModel,
    val children: MutableList<SharedItemNode>,
    override var nodeParent: NodeInterface?
): NodeInterface {
    constructor(model: PackageModel, parent: NodeInterface? = null) : this(model, mutableListOf<SharedItemNode>(), parent)

    init {
        // TODO: Sort
//        children.sortWith(compareBy { it.name.toLowerCase() })
    }

    fun addChildren(nodes: List<SharedItemNode>) {
        nodes.forEach {
            it.nodeParent = this
            children.add(it)
        }
    }

    override fun children(): Enumeration<SharedItemNode> = children.toEnumeration()

    override fun isLeaf(): Boolean = childCount == 0

    override fun getChildCount(): Int = children.size

    override fun getParent(): TreeNode? = nodeParent

    override fun getChildAt(childIndex: Int): TreeNode? = children[childIndex]

    override fun getIndex(node: TreeNode?): Int = children.indexOfFirst { it == node }

    override fun getAllowsChildren(): Boolean = true
}


class ModuleNode(
    val model: String,
    val children: MutableList<PackageNode>,
    override var nodeParent: NodeInterface?
): NodeInterface {
    override fun children(): Enumeration<out TreeNode> = children.toEnumeration()

    override fun isLeaf(): Boolean = childCount == 0

    override fun getChildCount(): Int = children.size

    override fun getParent(): TreeNode? = nodeParent

    override fun getChildAt(childIndex: Int): TreeNode = children[childIndex]

    override fun getIndex(node: TreeNode?): Int = children.indexOfFirst { it == node }

    override fun getAllowsChildren(): Boolean = true
}

class MPackageNode(
    val model: PackageContainable,
    val sharedChildren: MutableList<PackageNode>,
    val nodeParent: TreeNode?
): MutableTreeNode {
    override fun children(): Enumeration<out TreeNode> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun insert(child: MutableTreeNode?, index: Int) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun setParent(newParent: MutableTreeNode?) {
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

    override fun setUserObject(`object`: Any?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun remove(index: Int) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun remove(node: MutableTreeNode?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun isLeaf(): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getChildCount(): Int {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun removeFromParent() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

}
