package abyss.model.tree.nodes

import abyss.model.SharedType
import com.intellij.psi.stubs.Stub
import java.net.URL
import java.util.*
import javax.swing.tree.TreeNode



interface SharedStubModelInterface {
    val stub: Stub
    val sharedChildren: Collection<SharedStubModelInterface>
    val url: URL
}

interface NodeInterface: TreeNode

class Node(
    val title: String,
    val sharedChildren: Array<String>,
    val nodeParent: TreeNode?
): NodeInterface  {

    constructor(title: String, parent: TreeNode) : this(title, arrayOf(), parent)
    constructor(sharedChildren: Array<String>) : this("", sharedChildren, null)

    override fun children(): Enumeration<NodeInterface> { return sharedChildren.map { Node(it, this) }.toEnumeration()}

    override fun isLeaf(): Boolean {
        return childCount == 0
    }

    override fun getChildCount(): Int {
        return sharedChildren.size
    }

    override fun getParent(): TreeNode? {
        return this.nodeParent
    }

    override fun getChildAt(childIndex: Int): TreeNode {
        return Node(sharedChildren[childIndex], this)
    }

    override fun getIndex(node: TreeNode?): Int {
        val n = node as Node
        return sharedChildren.indexOfFirst { it == n.title }
    }

    override fun getAllowsChildren(): Boolean {
        return true
    }

}


interface ExpectOrActualModelInterface: ElementContainable {
    val type: SharedType
    val stub: Stub?
}

data class ExpectOrActualModel(
    override val type: SharedType,
    override val stub: Stub?
) : ExpectOrActualModelInterface

class ExpectOrActualNode(
    val model: ExpectOrActualModelInterface,
    val nodeParent: TreeNode?
): NodeInterface {

    override fun children(): Enumeration<NodeInterface> { return emptyEnumeration() }

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

interface ElementContainable

interface SharedElementModelInterface: ElementContainable, PackageContainable {
    val type: SharedType
    val stub: Stub?
}

data class SharedElementModel(
    override val type: SharedType,
    override val stub: Stub?
): SharedElementModelInterface

// TODO: notnull parent and model
class SharedElementNode (
    val model: ElementContainable,
    val sharedChildren: Array<ElementContainable>,
    val nodeParent: TreeNode?
): NodeInterface {

    constructor(model: ElementContainable, parent: TreeNode) : this(model, arrayOf<ElementContainable>(), parent)
//    constructor(sharedChildren: Array<ExpectOrActualModel>) : this(null, sharedChildren, null)

    init {
        // TODO: Sort
//        children.sortWith(compareBy { it.name.toLowerCase() })
    }

    override fun children(): Enumeration<NodeInterface> { return sharedChildren.mapNotNull { model ->
        // TODO
        when (model) {
            is SharedElementModelInterface -> { SharedElementNode(model, this) }
            is ExpectOrActualModelInterface -> { ExpectOrActualNode(model, this) }
        }
        null
//        ExpectOrActualNode(it, this)
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
            is ExpectOrActualModelInterface -> { ExpectOrActualNode(model, this) }
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


//data class FileTreeNode(val file: File?, val children: Array<File>, val nodeParent: TreeNode?) : TreeNode {
//    constructor(file: File, parent: TreeNode) : this(file, file.listFiles() ?: arrayOf(), parent)
//    constructor(children: Array<File>) : this(null, children, null)
//
//    init {
//        children.sortWith(compareBy { it.name.toLowerCase() })
//    }
//
//    override fun children(): Enumeration<FileTreeNode> {
//        return children.map { it -> FileTreeNode(it, this) }.toEnumeration()
//    }
//
//    override fun getAllowsChildren(): Boolean {
//        return true
//    }
//
//    override fun getChildAt(childIndex: Int): TreeNode {
//        return FileTreeNode(children[childIndex], this)
//    }
//
//    override fun getChildCount(): Int {
//        return children.size
//    }
//
//    override fun getIndex(node: TreeNode): Int {
//        val ftn = node as FileTreeNode
//        return children.indexOfFirst { it -> (it == ftn.file) }
//    }
//
//    override fun getParent(): TreeNode? {
//        return this.nodeParent
//    }
//
//    override fun isLeaf(): Boolean {
//        val isNotFolder = (this.file != null) && (this.file.isFile)
//        return this.childCount == 0 && isNotFolder
//    }
//}

/**
 * Extension function for converting a {@link List} to an {@link Enumeration}
 */
fun <T> List<T>.toEnumeration(): Enumeration<T> {
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

fun <T> emptyEnumeration(): Enumeration<T> {
    return object : Enumeration<T> {

        override fun hasMoreElements(): Boolean {
            return false
        }

        override fun nextElement(): T {
            throw NoSuchElementException("Empty enumeration asked for an element")
        }
    }
}