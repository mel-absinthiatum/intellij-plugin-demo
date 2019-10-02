package toolWindow.tree

import com.intellij.icons.AllIcons
import com.intellij.notification.Notification
import com.intellij.notification.NotificationType
import com.intellij.notification.Notifications
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.popup.JBPopupFactory
import com.intellij.openapi.wm.ToolWindow
import dialog.DemoDialog
import dialog.DemoDialogWrapper
import messageView.DummyMessageViewProvider
import javax.swing.tree.DefaultMutableTreeNode
import javax.swing.tree.DefaultTreeModel
import javax.swing.tree.MutableTreeNode
import javax.swing.tree.TreeModel


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
        notificationsCatalog.add(makePlainNotification())
        notificationsCatalog.add(makeLinkNotification())


        rootNode.add(dialogCatalog)
        rootNode.add(popupCatalog)
        rootNode.add(notificationsCatalog)


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

    private fun makePlainNotification(): MutableTreeNode {
        return DefaultMutableTreeNode(TreeNodeContentImpl("Plain notification") {
            val notification = Notification("Demo plain notification", "Plain notification",
                "Here we are!<br><br><i>Unbelievable</i>", NotificationType.WARNING)
            Notifications.Bus.notify(notification, project)
        })
    }

    private fun makeLinkNotification(): MutableTreeNode {
        return DefaultMutableTreeNode(TreeNodeContentImpl("Link notification") {
            val notification = Notification(
                "Demo plain notification",
                AllIcons.Actions.Commit,
                "Link notification",
                "and its subtitle",
                "<a href=\"https://www.youtube.com/user/gordonramsay?hl=ru\">Here we are!</a>",
                NotificationType.INFORMATION) { n, event ->
                val url = event.url
                val eventType = event.eventType

                com.intellij.ide.BrowserUtil.browse(url)

                val notification = Notification("Demo plain notification", "Plain notification",
                    "$url <i>$eventType</i>", NotificationType.INFORMATION)
                Notifications.Bus.notify(notification, project)
            }
            Notifications.Bus.notify(notification, project)
        })
    }
}