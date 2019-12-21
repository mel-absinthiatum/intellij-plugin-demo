package abyss.actions

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import notifications.SimpleNotificationProvider


class RefreshTreeAction : AnAction() {
    override fun actionPerformed(e: AnActionEvent) {
        SimpleNotificationProvider.notify("Refresh", "refresh the tree now!", e.project)
    }
}
