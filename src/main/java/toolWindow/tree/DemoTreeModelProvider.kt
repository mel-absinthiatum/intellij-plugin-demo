package toolWindow.tree

import com.intellij.icons.AllIcons
import com.intellij.notification.Notification
import com.intellij.notification.NotificationType
import com.intellij.notification.Notifications
import com.intellij.openapi.fileChooser.FileChooser
import com.intellij.openapi.fileChooser.FileChooserDescriptor
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.popup.JBPopupFactory
import com.intellij.openapi.wm.ToolWindow
import dialog.DemoDialog
import dialog.DemoDialogWrapper
import dialog.fileBrowseWindow.FileBrowseDialog
import messageView.DummyMessageViewProvider
import javax.swing.tree.DefaultMutableTreeNode
import javax.swing.tree.DefaultTreeModel
import javax.swing.tree.MutableTreeNode
import javax.swing.tree.TreeModel


class DemoTreeModelProvider(private val toolWindow: ToolWindow, private val project: Project) {

    fun createToolWindowTreeModel(): TreeModel {

        val rootNode = makeCatalogNode("UI Demonstrations").including(
            makeCatalogNode("Popups").including(
                makePopupNode()
            ),
            makeCatalogNode("Dialogs").including(
                makeRawDialogNode(),
                makeDialogWithWrapperNode(),
                makeMessagesDialogNode()
            ),
            makeCatalogNode("Notifications").including(
                makePlainNotification(),
                makeLinkNotification()
            ),
            makeCatalogNode("Files/Classes chooser").including(
                makeFileChooserNode(),
                makeFileChooserByTitleNode()
            )
        )

        return DefaultTreeModel(rootNode)
    }


    private fun MutableTreeNode.including(vararg nodes: MutableTreeNode): MutableTreeNode {
        nodes.reversed().forEach {
            this.insert(it, 0)
        }
        return this
    }


    private fun makeCatalogNode(title: String): MutableTreeNode {
        return DefaultMutableTreeNode(TreeNodeContentImpl(title))
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

    private fun makeMessagesDialogNode(): MutableTreeNode {
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
            val notification = Notification(
                "Demo plain notification", "Plain notification",
                "Here we are!<br><br><i>Unbelievable</i>", NotificationType.WARNING
            )
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
                NotificationType.INFORMATION
            ) { _, event ->
                val url = event.url
                val eventType = event.eventType

                com.intellij.ide.BrowserUtil.browse(url)

                val notification = Notification(
                    "Demo plain notification", "Plain notification",
                    "$url <i>$eventType</i>", NotificationType.INFORMATION
                )
                Notifications.Bus.notify(notification, project)
            }
            Notifications.Bus.notify(notification, project)
        })
    }

    private fun makeFileChooserNode(): MutableTreeNode {
        return DefaultMutableTreeNode(TreeNodeContentImpl("File chooser") {
            val descriptor = FileChooserDescriptor(true, false, false, false, false, false)
            FileChooser.chooseFile(descriptor, project, null) { vf ->
                val notification = Notification(
                    "File selection notification", "Selected",
                    "<i>${vf.name}</i><br>with path: <i>${vf.path}</i>", NotificationType.INFORMATION
                )
                Notifications.Bus.notify(notification, project)
            }
        })
    }

    private fun makeFileChooserByTitleNode(): MutableTreeNode {
        return DefaultMutableTreeNode(TreeNodeContentImpl("File chooser with text field") {
            FileBrowseDialog.showStartupDialog(project)
        })
    }
}