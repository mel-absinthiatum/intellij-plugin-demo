package abyss.model.tree.nodes

import abyss.model.SharedType
import com.intellij.psi.PsiElement
import com.intellij.psi.stubs.Stub
import java.net.URL
import java.util.*
import javax.swing.tree.TreeNode



interface SharedStubModelInterface {
    val stub: Stub
    val sharedChildren: Collection<SharedStubModelInterface>
    val url: URL
}
interface OldNodeInterface: TreeNode {
    var nodeParent: OldNodeInterface?

}
interface NodeInterface: TreeNode {
    var nodeParent: NodeInterface?
}


class Node(
    val title: String,
    var sharedChildren: Array<String>,
    override var nodeParent: OldNodeInterface?
): OldNodeInterface  {

    constructor(title: String, parent: OldNodeInterface?) : this(title, arrayOf(), parent)
    constructor(sharedChildren: Array<String>) : this("", sharedChildren, null)

    override fun children(): Enumeration<OldNodeInterface> { return sharedChildren.map { Node(it, this) }.toEnumeration()}

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
    val psi: PsiElement
    val type: SharedType
    val stub: Stub?
}

data class ExpectOrActualModel(
    override val psi: PsiElement,
    override val type: SharedType,
    override val stub: Stub?
) : ExpectOrActualModelInterface, NodeModel

class ExpectOrActualNode(
    val model: ExpectOrActualModelInterface,
    override var nodeParent: NodeInterface?
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
    val name: String?
    val type: SharedType
    val stub: Stub?
}

data class SharedElementModel(
    override val name: String?,
    override val type: SharedType,
    override val stub: Stub?
): SharedElementModelInterface, NodeModel

// TODO: notnull parent and model
class SharedElementNode (
    val model: SharedElementModelInterface,
    var sharedChildren: Array<ElementContainable>,
    override var nodeParent: NodeInterface?
): NodeInterface {

    // TODO: Kotlin readonly public properties
    val nestedElementsNodes = mutableListOf<SharedElementNode>()
    private val expectOrActualNodes = mutableListOf<ExpectOrActualNode>()

    constructor(model: SharedElementModelInterface, parent: NodeInterface?) : this(model, arrayOf<ElementContainable>(), parent)
//    constructor(sharedChildren: Array<ExpectOrActualModel>) : this(null, sharedChildren, null)

    init {
        // TODO: Sort
//        children.sortWith(compareBy { it.name.toLowerCase() })
    }

    fun addChild(model: SharedElementModelInterface) {
        val node = SharedElementNode(model, this)
        nestedElementsNodes.add(node)
    }

    fun addChild(model: ExpectOrActualModelInterface) {
        val node = ExpectOrActualNode(model, this)
        expectOrActualNodes.add(node)
    }

    fun addChildNode(node: SharedElementNode) {
        node.nodeParent = this
        nestedElementsNodes.add(node)
    }

    fun addChildNode(node: ExpectOrActualNode) {
        node.nodeParent = this
        expectOrActualNodes.add(node)
    }

    fun addChildren(nodes: Collection<SharedElementNode>) {
        nodes.forEach {
            addChildNode(it)
        }
    }


//    }
//    override fun children(): Enumeration<NodeInterface> {
////    { return sharedChildren.mapNotNull { model ->
////        // TODO
////        when (model) {
////            is SharedElementModelInterface -> { SharedElementNode(model, this) }
////            is ExpectOrActualModelInterface -> { ExpectOrActualNode(model, this) }
////        }
////        null
//////        ExpectOrActualNode(it, this)
//    }

    override fun children(): Enumeration<NodeInterface> {
        val list: MutableList<NodeInterface> = nestedElementsNodes.toMutableList()
        list.addAll(expectOrActualNodes)
        return list.toEnumeration()
    }



    override fun isLeaf(): Boolean {
        return childCount == 0
    }

    override fun getChildCount(): Int {
        return nestedElementsNodes.size + expectOrActualNodes.size
    }

    override fun getParent(): TreeNode? {
        return this.nodeParent
    }

    override fun getChildAt(childIndex: Int): TreeNode? {
        val list: MutableList<NodeInterface> = nestedElementsNodes.toMutableList()
        list.addAll(expectOrActualNodes)

        return list[childIndex]
    }

    override fun getIndex(node: TreeNode?): Int {
        val list: MutableList<NodeInterface> = nestedElementsNodes.toMutableList()
        list.addAll(expectOrActualNodes)

        return list.indexOfFirst { it == node }
    }

    override fun getAllowsChildren(): Boolean {
        return true
    }
}


// TODO: Use light psi
/*
*
            val lightElement: PsiElement? = when (declaration) {
                is KtClassOrObject -> declaration.toLightClass()
                is KtNamedFunction, is KtSecondaryConstructor -> LightClassUtil.getLightClassMethod(declaration as KtFunction)
                is KtProperty, is KtParameter -> {
                    if (declaration is KtParameter && !declaration.hasValOrVar()) return false
                    // can't rely on light element, check annotation ourselves
                    val entryPointsManager = EntryPointsManager.getInstance(declaration.project) as EntryPointsManagerBase
                    return checkAnnotatedUsingPatterns(
                        declaration,
                        entryPointsManager.additionalAnnotations + entryPointsManager.ADDITIONAL_ANNOTATIONS
                    )
                }
                else -> return false
            }*/


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
