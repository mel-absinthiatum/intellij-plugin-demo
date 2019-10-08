package notifications

import com.intellij.notification.Notification
import com.intellij.notification.NotificationType
import com.intellij.notification.Notifications
import com.intellij.openapi.project.Project


class SimpleNotificationProvider(private val project: Project?) {
    fun notify(title: String = "Demo notification", text: String = "") {
        notify(title, text, project)
    }

    companion object {
        fun notify(title: String = "Demo notification", text: String = "", project: Project? = null) {
            val notification = Notification(
                "Simple demo notification", title,
                text, NotificationType.INFORMATION
            )
            Notifications.Bus.notify(notification, project)
        }
    }
}