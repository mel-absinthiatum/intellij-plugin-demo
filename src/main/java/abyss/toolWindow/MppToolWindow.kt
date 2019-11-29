package abyss.toolWindow

import abyss.psi.SharedItemsProvider
import abyss.view.AbyssTreeProvider
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.treeStructure.Tree
import toolWindow.tree.TreeNodeContent
import javax.swing.*
import javax.swing.tree.DefaultMutableTreeNode
import javax.swing.tree.DefaultTreeCellRenderer
import javax.swing.tree.TreeCellRenderer
import javax.swing.tree.TreeSelectionModel

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
        val treeModel = SharedItemsProvider().retrieveSharedItems(project)

        val toolWindowTree = AbyssTreeProvider().tree(treeModel)

        toolWindowTree.run {
            isRootVisible = false
            selectionModel.selectionMode = TreeSelectionModel.SINGLE_TREE_SELECTION
            cellRenderer = makeTreeCellRenderer()

            addTreeSelectionListener { event ->
                val source = event.source as JTree
                val node = source.lastSelectedPathComponent as DefaultMutableTreeNode?
                val obj = node?.userObject as TreeNodeContent?
                obj?.action?.invoke()
            }
        }
        return toolWindowTree
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
