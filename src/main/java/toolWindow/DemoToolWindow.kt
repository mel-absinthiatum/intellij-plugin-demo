package toolWindow

import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.treeStructure.Tree
import messageView.DummyMessageViewProvider
import javax.swing.BorderFactory.createEmptyBorder
import javax.swing.BoxLayout
import javax.swing.ImageIcon
import javax.swing.JPanel
import javax.swing.JTree
import javax.swing.tree.DefaultMutableTreeNode
import javax.swing.tree.DefaultTreeCellRenderer
import javax.swing.tree.DefaultTreeModel
import javax.swing.tree.TreeCellRenderer
import javax.swing.tree.TreeSelectionModel.SINGLE_TREE_SELECTION
import toolWindow.tree.TreeNodeContent
import toolWindow.tree.DemoTreeModelProvider


class DemoToolWindow (val project: Project, val toolWindow: ToolWindow) {
    val content: JPanel

    init {
        val panel = JPanel()
        panel.layout = BoxLayout(panel, BoxLayout.LINE_AXIS)
        val customTree = createToolWindowTree()

        val scrollPane = JBScrollPane(customTree)

        scrollPane.border = createEmptyBorder()
        panel.add(scrollPane)

        content = panel
    }

    private fun createToolWindowTree(): Tree {
        val treeModelProvider = DemoTreeModelProvider(toolWindow, project)
        val treeModel = treeModelProvider.createToolWindowTreeModel()

        val toolWindowTree = Tree(treeModel)

        toolWindowTree.run {
            showsRootHandles = true
            selectionModel.selectionMode = SINGLE_TREE_SELECTION
            cellRenderer = makeTreeCellRenderer()

            addTreeSelectionListener { event ->
                val source = event.source as JTree
                val node = source.lastSelectedPathComponent as DefaultMutableTreeNode
                val obj = node.userObject as TreeNodeContent?
                obj?.action?.invoke()
            }
        }
        return toolWindowTree
    }

    private fun makeTreeCellRenderer(): TreeCellRenderer {
        val imageIcon = ImageIcon(javaClass.getResource("/toolWindowResources/star.png"))
        val renderer = DefaultTreeCellRenderer()
        renderer.leafIcon = imageIcon
        renderer.borderSelectionColor = null
        renderer.backgroundSelectionColor = null
        return renderer
    }
}
