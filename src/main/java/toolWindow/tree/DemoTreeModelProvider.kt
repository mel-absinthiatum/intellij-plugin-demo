package toolWindow.tree

import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.popup.JBPopupFactory
import com.intellij.openapi.wm.ToolWindow
import dialog.DemoDialog
import dialog.DemoDialogWrapper
import messageView.DummyMessageViewProvider
import javax.swing.tree.*


class DemoTreeModelProvider(val toolWindow: ToolWindow, val project: Project) {

    fun createToolWindowTreeModel(): TreeModel {

        val rootNode = DefaultMutableTreeNode(TreeNodeContentImpl("UI Demonstrations"))

        val dialogCatalog = DefaultMutableTreeNode(TreeNodeContentImpl("Dialogs"))
        dialogCatalog.add(makeDialogWithWrapperNode())
        dialogCatalog.add(makeMessagesDialodNode())
        dialogCatalog.add(makeRawDialogNode())

        val popupCatalog = DefaultMutableTreeNode(TreeNodeContentImpl("Popups"))
        popupCatalog.add(makePopupNode())

        val notificationsCatalog = DefaultMutableTreeNode(TreeNodeContentImpl("Notifications"))


        rootNode.add(dialogCatalog)
        rootNode.add(popupCatalog)
        // TODO: Implement notifications displaying
//        rootNode.add(notificationsCatalog)


        return DefaultTreeModel(rootNode)
    }

    private fun makeDialogWithWrapperNode(): MutableTreeNode {
        return DefaultMutableTreeNode(TreeNodeContentImpl("Dialog with wrapper") {
            if (DemoDialogWrapper().showAndGet()) {
                toolWindow.hide(null)
            }
        })
    }

    private fun makeRawDialogNode(): MutableTreeNode {
        return DefaultMutableTreeNode(TreeNodeContentImpl("Raw dialog (JDialog)") {
            val dialog = DemoDialog()
            dialog.pack()
            dialog.isVisible = true
        })
    }

    private fun makeMessagesDialodNode(): MutableTreeNode {
        return DefaultMutableTreeNode(TreeNodeContentImpl("Messages Dialog") {
            DummyMessageViewProvider().showDummyMessage(project)
        })
    }

    private fun makePopupNode(): MutableTreeNode {
        return DefaultMutableTreeNode(TreeNodeContentImpl("Popup") {
            val popup =
                JBPopupFactory.getInstance().createConfirmation("Close the Demo Tools?", {
                    toolWindow.hide(null)
                }, 0)
            popup.showInCenterOf(toolWindow.component)
        })
    }
}