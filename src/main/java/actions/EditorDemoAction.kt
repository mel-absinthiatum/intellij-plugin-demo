package actions

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project


class EditorDemoAction : AnAction() {

    /**
     * Replaces the run of text selected by the primary caret with a fixed string.
     * @param e  Event related to this action
     */
    override fun actionPerformed(e: AnActionEvent) {
        // Get all the required data from data keys
        // Editor and Project were verified in update(), so they are not null.
        val editor = e.getRequiredData<Editor>(CommonDataKeys.EDITOR)
        val project = e.getRequiredData<Project>(CommonDataKeys.PROJECT)
        val document = editor.document
        // Work off of the primary caret to get the selection info
        val primaryCaret = editor.caretModel.primaryCaret
        val start = primaryCaret.selectionStart
        val end = primaryCaret.selectionEnd
        // Replace the selection with a fixed string.
        // Must do this document change in a write action context.
        WriteCommandAction.runWriteCommandAction(
            project
        ) { document.replaceString(start, end, "awesome_string") }
        // De-select the text range that was just replaced
        primaryCaret.removeSelection()
    }

    /**
     * Sets visibility and enables this action menu item if:
     * A project is open,
     * An editor is active,
     * Some characters are selected
     * @param e  Event related to this action
     */
    override fun update(e: AnActionEvent) {
        // Get required data keys
        val project = e.project
        val editor = e.getData<Editor>(CommonDataKeys.EDITOR)
        // Set visibility and enable only in case of existing project and editor and if a selection exists
        e.presentation.isEnabledAndVisible =
            project != null && editor != null && editor.selectionModel.hasSelection()
    }
}