package toolWindow

import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.ui.JBSplitter
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.treeStructure.Tree
import fileSystemTree.FileSystemTreeProvider
import toolWindow.tree.DemoTreeModelProvider
import toolWindow.tree.TreeNodeContent
import javax.swing.BorderFactory.createEmptyBorder
import javax.swing.BoxLayout
import javax.swing.ImageIcon
import javax.swing.JPanel
import javax.swing.JTree
import javax.swing.tree.DefaultMutableTreeNode
import javax.swing.tree.DefaultTreeCellRenderer
import javax.swing.tree.TreeCellRenderer
import javax.swing.tree.TreeSelectionModel.SINGLE_TREE_SELECTION


class DemoToolWindow (private val project: Project, private val toolWindow: ToolWindow) {
    val content: JPanel

    init {
        val splitter = JBSplitter(true)
        splitter.firstComponent = makeNorthPanel()
        splitter.secondComponent = makeSouthPanel()

        val panel = JPanel()
        panel.layout = BoxLayout(panel, BoxLayout.LINE_AXIS)
        panel.add(splitter)

        content = panel
    }

    private fun makeNorthPanel(): JPanel {
        val panel = JPanel()
        panel.layout = BoxLayout(panel, BoxLayout.LINE_AXIS)
        val customTree = createToolWindowTree()

        val scrollPane = JBScrollPane(customTree)

        scrollPane.border = createEmptyBorder()
        panel.add(scrollPane)

        return panel
    }

    private fun makeSouthPanel(): JPanel {
        val panel = JPanel()
        panel.layout = BoxLayout(panel, BoxLayout.LINE_AXIS)
        val customTree = FileSystemTreeProvider.createFileSystemTree(project)

        val scrollPane = JBScrollPane(customTree)

        scrollPane.border = createEmptyBorder()
        panel.add(scrollPane)

        return panel
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
                val node = source.lastSelectedPathComponent as DefaultMutableTreeNode?
                val obj = node?.userObject as TreeNodeContent?
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
