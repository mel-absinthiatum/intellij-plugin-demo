package actions

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.ui.Messages
import notifications.SimpleNotificationProvider
import javax.swing.Icon


class SimpleDemoAction(text: String = "", description: String= "", icon: Icon? = null) : AnAction(text, description, icon) {
    override fun actionPerformed(anActionEvent: AnActionEvent) {
        SimpleNotificationProvider.notify("Simple action is completed")

        // Using the event, create and show a dialog
        val currentProject = anActionEvent.project
        val dlgMsg = StringBuffer(anActionEvent.presentation.text + " Selected!")
        val dlgTitle = anActionEvent.presentation.description
        // If an element is selected in the editor, add info about it.
        val nav = anActionEvent.getData(CommonDataKeys.NAVIGATABLE)
        if (nav != null) {
            dlgMsg.append(String.format("\nSelected Element: %s", nav.toString()))
        }
        Messages.showMessageDialog(currentProject, dlgMsg.toString(), dlgTitle, Messages.getInformationIcon())
    }

    override fun update(anActionEvent: AnActionEvent) {
        // Set the availability based on whether a project is open
        val project = anActionEvent.project
        anActionEvent.presentation.isEnabledAndVisible = project != null
    }
}