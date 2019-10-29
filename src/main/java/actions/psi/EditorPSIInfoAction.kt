package actions.psi

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.actionSystem.LangDataKeys
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import notifications.SimpleNotificationProvider

class EditorPSIInfoAction: AnAction() {
    override fun actionPerformed(e: AnActionEvent) {
        val editor = e.getRequiredData<Editor>(CommonDataKeys.EDITOR)
        val project = e.getRequiredData<Project>(CommonDataKeys.PROJECT)
        val document = editor.document

        val psiFile = e.getData(LangDataKeys.PSI_FILE)
        if (psiFile != null) {
            SimpleNotificationProvider.notify(psiFile.name, psiFile.fileType.description, project)
        }
    }
}