package actions

import abyss.coroutines.CoroutinePlayground
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent

class CoroutinesPlayAction : AnAction() {

    override fun actionPerformed(e: AnActionEvent) {
        CoroutinePlayground().runTest()
    }
}