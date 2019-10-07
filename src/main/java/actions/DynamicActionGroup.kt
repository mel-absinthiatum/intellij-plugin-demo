package actions

import com.intellij.openapi.actionSystem.ActionGroup
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent


class DynamicActionGroup : ActionGroup() {
    override fun getChildren(anActionEvent: AnActionEvent?): Array<AnAction> {
        return arrayOf(
            SimpleDemoAction("Action Added at Runtime", "Dynamic Action Demo", null)
            )

    }
}