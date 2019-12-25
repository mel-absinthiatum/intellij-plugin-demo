package abyss.actions

import abyss.treeUpdateManager.SharedElementsUpdatesManager
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import notifications.SimpleNotificationProvider


class RefreshTreeAction : AnAction() {
    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project
        SimpleNotificationProvider.notify("Refresh", "refresh the tree now!", e.project)
        if (project == null) {
            return
        }
        SharedElementsUpdatesManager().updateSharedTreeRoot(project)
    }
}
