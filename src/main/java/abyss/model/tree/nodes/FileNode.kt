package abyss.model.tree.nodes

import com.intellij.openapi.vfs.VirtualFile
import java.util.*
import javax.swing.tree.TreeNode


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
