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


// TODO: notnull parent and model
class PackageNode (
    val model: PackageContainable,
    val sharedChildren: Array<PackageContainable>,
    val nodeParent: TreeNode?
): NodeInterface {

    fun insertChild(child: PackageContainable) {
        // TODO: Implement as well as other mutating methods
    }

    constructor(model: PackageContainable, parent: TreeNode) : this(model, arrayOf<PackageContainable>(), parent)
    init {
        // TODO: Sort
//        children.sortWith(compareBy { it.name.toLowerCase() })
    }

    override fun children(): Enumeration<NodeInterface> { return sharedChildren.mapNotNull { model ->
        when (model) {
            is SharedElementModelInterface -> { SharedElementNode(model, this) }
            is PackageModelInterface -> { PackageNode(model, this) }
        }
        null
    }.toEnumeration()}

    override fun isLeaf(): Boolean {
        return childCount == 0
    }

    override fun getChildCount(): Int {
        return sharedChildren.size
    }

    override fun getParent(): TreeNode? {
        return this.nodeParent
    }

    override fun getChildAt(childIndex: Int): TreeNode? {
        val model = sharedChildren[childIndex]
        when (model) {
            is SharedElementModelInterface -> { SharedElementNode(model, this) }
            is PackageModelInterface -> { PackageNode(model, this) }
        }
        return null
    }

    override fun getIndex(node: TreeNode?): Int {
        val n = node as ExpectOrActualNode
        return sharedChildren.indexOfFirst { it == n.model }
    }

    override fun getAllowsChildren(): Boolean {
        return true
    }
}


class MPackageNode(
    val model: PackageContainable,
    val sharedChildren: Array<PackageContainable>,
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

/**
 * Extension function for converting a {@link List} to an {@link Enumeration}
 */
private fun <T> List<T>.toEnumeration(): Enumeration<T> {
    return object : Enumeration<T> {
        var count = 0

        override fun hasMoreElements(): Boolean {
            return this.count < size
        }

        override fun nextElement(): T {
            if (this.count < size) {
                return get(this.count++)
            }
            throw NoSuchElementException("List enumeration asked for more elements than present")
        }
    }
}

private fun <T> emptyEnumeration(): Enumeration<T> {
    return object : Enumeration<T> {

        override fun hasMoreElements(): Boolean {
            return false
        }

        override fun nextElement(): T {
            throw NoSuchElementException("Empty enumeration asked for an element")
        }
    }
}