package abyss.toolWindow

import abyss.psi.SharedItemsProvider
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.treeStructure.Tree
import javax.swing.BorderFactory
import javax.swing.BoxLayout
import javax.swing.ImageIcon
import javax.swing.JPanel
import javax.swing.tree.DefaultTreeCellRenderer
import javax.swing.tree.TreeCellRenderer

class MppToolWindow (private val project: Project, private val toolWindow: ToolWindow) {
    val content: JPanel

    init {
        val panel = JPanel()
        panel.layout = BoxLayout(panel, BoxLayout.LINE_AXIS)
        val customTree = createToolWindowTree()

        val scrollPane = JBScrollPane(customTree)

        scrollPane.border = BorderFactory.createEmptyBorder()
        panel.add(scrollPane)

        content = panel
    }

    private fun createToolWindowTree(): Tree {
        val treeModel = SharedItemsProvider().getSharedItems(project) { treeModel ->

        }
        // TODO: use coroutines and for UI use activity indicator.

//        val toolWindowTree = AbyssTreeProvider().tree(treeModel)
//
//        toolWindowTree.run {
//            isRootVisible = false
//            selectionModel.selectionMode = TreeSelectionModel.SINGLE_TREE_SELECTION
//            cellRenderer = makeTreeCellRenderer()
//
//            addTreeSelectionListener { event ->
//                val source = event.source as JTree
//                val node = source.lastSelectedPathComponent as DefaultMutableTreeNode?
//                val obj = node?.userObject as TreeNodeContent?
//                obj?.action?.invoke()
//            }
//        }
//        return toolWindowTree
        return Tree()
    }

    private fun makeTreeCellRenderer(): TreeCellRenderer {
        val imageIcon = ImageIcon(javaClass.getResource("/abyss/Obj.png"))
        val renderer = DefaultTreeCellRenderer()
        renderer.leafIcon = imageIcon
        renderer.borderSelectionColor = null
        renderer.backgroundSelectionColor = null
        return renderer
    }

}
