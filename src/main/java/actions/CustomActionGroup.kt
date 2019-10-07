package actions

import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.actionSystem.DefaultActionGroup


class CustomActionGroup : DefaultActionGroup() {
    override fun update(event: AnActionEvent) {
        // Enable/disable depending on whether user is editing
        val editor = event.getData(CommonDataKeys.EDITOR)
        event.presentation.isEnabled = editor != null
        // Always make visible.
        event.presentation.isVisible = true
        // Take this opportunity to set an icon for the menu entry.
        event.presentation.icon = AllIcons.General.Error
    }
}