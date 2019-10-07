package actions

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import notifications.SimpleNotificationProvider


class GrouppedDemoAction : AnAction() {
    override fun actionPerformed(anActionEvent: AnActionEvent) {
        SimpleNotificationProvider.notify("Simple action is completed")
    }

    override fun update(anActionEvent: AnActionEvent) {
        // Set the availability based on whether a project is open
        val project = anActionEvent.project
        anActionEvent.presentation.isEnabledAndVisible = project != null
    }
}