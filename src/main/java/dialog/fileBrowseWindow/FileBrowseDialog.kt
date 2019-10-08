package dialog.fileBrowseWindow

import com.intellij.CommonBundle
import com.intellij.notification.Notification
import com.intellij.notification.NotificationType
import com.intellij.notification.Notifications
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.wm.ex.WindowManagerEx
import java.awt.Window

import javax.swing.Action
import javax.swing.JComponent


class FileBrowseDialog : DialogWrapper {

    private var panel: FileBrowsePanel? = null
    private lateinit var project: Project

    private constructor(project: Project) : super(WindowManagerEx.getInstanceEx().findVisibleFrame(), true) {
        initialize(project)
    }

    private constructor(project: Project, w: Window) : super(w, true) {
        initialize(project)
    }

    private fun initialize(project: Project) {
        this.project = project
        isModal = true
        title = "Select the file"
        setCancelButtonText(CommonBundle.getCloseButtonText())
        panel = FileBrowsePanel(project)
        horizontalStretch = 3f
        verticalStretch = 1f
        init()

    }

    override fun createActions(): Array<Action> {
        return arrayOf(okAction, cancelAction)
    }

    override fun createCenterPanel(): JComponent? {
        return panel
    }


    companion object {

        private var instance: FileBrowseDialog? = null

        // TODO Unwrap optional instance
        fun showStartupDialog(project: Project) {
            val window = WindowManagerEx.getInstanceEx().suggestParentWindow(project)
            if (instance != null && instance!!.isVisible) {
                instance!!.dispose()
            }
            instance = if (window == null) FileBrowseDialog(project) else FileBrowseDialog(project, window)
            if (instance!!.showAndGet()) {
                val notification = Notification(
                    "File selection notification",
                    "Selected",
                    "path: <i>${instance!!.panel!!.searchFieldText}</i>",
                    NotificationType.INFORMATION
                )
                Notifications.Bus.notify(notification, project)
            }
        }
    }

}



