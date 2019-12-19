package abyss.toolWindow

import abyss.extensionPoints.SharedElementsTopics
import abyss.extensionPoints.SharedElementsTopicsNotifier
import abyss.psi.SharedItemsStubsProvider
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.ui.components.Label
import javax.naming.Context
import javax.swing.BoxLayout
import javax.swing.ImageIcon
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.tree.DefaultTreeCellRenderer
import javax.swing.tree.TreeCellRenderer

class MppToolWindow (private val project: Project, private val toolWindow: ToolWindow) {
    val content: JPanel

    init {
        val panel = JPanel()
        panel.layout = BoxLayout(panel, BoxLayout.LINE_AXIS)
//        createToolWindowTree()
//        val customTree = createToolWindowTree()
//
//        val scrollPane = JBScrollPane(customTree)
//
//        scrollPane.border = BorderFactory.createEmptyBorder()
//        panel.add(scrollPane)

        content = panel
        content.add(Label("ajdfgnai"))
        showTestLabel()

    }

    private fun showTestLabel() {
        //        ComponentManagerImpl(null).messageBus
        val bus = project.messageBus
        bus.connect().subscribe(SharedElementsTopics.CHANGE_ACTION_TOPIC, object : SharedElementsTopicsNotifier {
            override fun beforeAction(context: Context) {
                // Process 'before action' event.
            }

            override fun afterAction(context: Context) {
                // Process 'after action' event.
            }

            override fun stringUpdated(string: String) {
                val label = JLabel()
                label.text = string
                content.add(label)
                println("string displayed")
            }
        })
    }

    private fun createToolWindowTree() {
        SharedItemsStubsProvider().getSharedItems(project){

        }
//        SharedItemsProvider().getSharedItems(project) { treeModel ->
//            val toolWindowTree = AbyssTreeProvider().tree(treeModel)
//
//            toolWindowTree.run {
//                isRootVisible = false
//                selectionModel.selectionMode = TreeSelectionModel.SINGLE_TREE_SELECTION
//                cellRenderer = makeTreeCellRenderer()
//
//                addTreeSelectionListener { event ->
//                    val source = event.source as JTree
//                    val node = source.lastSelectedPathComponent as DefaultMutableTreeNode?
//                    val obj = node?.userObject as TreeNodeContent?
//                    obj?.action?.invoke()
//                }
//            }
//
//            val scrollPane = JBScrollPane(toolWindowTree)
//
//            scrollPane.border = BorderFactory.createEmptyBorder()
//            content.add(scrollPane)
//        }
        // TODO: use coroutines and for UI use an activity indicator.
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
