package actions

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import notifications.SimpleNotificationProvider


class SimpleDemoAction : AnAction() {
    override fun actionPerformed(anActionEvent: AnActionEvent) {
        SimpleNotificationProvider.notify("Action is completed")
    }
}