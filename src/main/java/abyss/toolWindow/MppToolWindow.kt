package abyss.toolWindow

import abyss.extensionPoints.SharedElementsTopics
import abyss.extensionPoints.SharedElementsTopicsNotifier
import abyss.view.AbyssTreeCellRenderer
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.treeStructure.Tree
import com.intellij.util.messages.MessageBus
import javax.swing.BorderFactory
import javax.swing.BoxLayout
import javax.swing.ImageIcon
import javax.swing.JPanel
import javax.swing.tree.DefaultTreeCellRenderer
import javax.swing.tree.TreeCellRenderer
import javax.swing.tree.TreeSelectionModel

class MppToolWindow (private val project: Project, private val toolWindow: ToolWindow) {
    val content: JPanel

    init {
        val panel = JPanel()
        panel.layout = BoxLayout(panel, BoxLayout.LINE_AXIS)
        subscribe(project.messageBus)

        content = panel
    }

    private fun subscribe(bus: MessageBus) {
        val bus = project.messageBus
        bus.connect().subscribe(SharedElementsTopics.SHARED_ELEMENTS_TREE_TOPIC, object : SharedElementsTopicsNotifier {
            override fun sharedElementsTreeUpdated(tree: Tree) {
                tree.run {
                    isRootVisible = false
                    selectionModel.selectionMode = TreeSelectionModel.SINGLE_TREE_SELECTION
                    cellRenderer = AbyssTreeCellRenderer(this)

                    addTreeSelectionListener { event ->
                        //                    val source = event.source as JTree
//                    val node = source.lastSelectedPathComponent as DefaultMutableTreeNode?
//                    val obj = node?.userObject as TreeNodeContent?
//                    obj?.action?.invoke()
                        println("Cliked")
                    }
                }

                val scrollPane = JBScrollPane(tree)

                scrollPane.border = BorderFactory.createEmptyBorder()
                content.add(scrollPane)
            }
        })
    }


    private fun makeTreeCellRenderer(): TreeCellRenderer {
        val imageIcon = ImageIcon(javaClass.getResource("/abyss/class.png"))
        val renderer = DefaultTreeCellRenderer()
        renderer.leafIcon = imageIcon
        renderer.borderSelectionColor = null
        renderer.backgroundSelectionColor = null
        return renderer
    }
}
