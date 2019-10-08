package messageView

import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import notifications.SimpleNotificationProvider
import javax.swing.ImageIcon


class DummyMessageViewProvider(private val project: Project) {

    // Parameter `title` is the button title on the message view.
    private class MessageOption(val title: String, val action: (Boolean)->Unit)

    private val notificationProvider = SimpleNotificationProvider(project)
    fun showDummyMessage() {
        val option1 = MessageOption("Opt_1") { isChecked ->
            notificationProvider.notify("Selected Opt_1. checkbox is checked: $isChecked")
        }
        val option2 = MessageOption("Opt_2") { isChecked ->
            notificationProvider.notify("Selected Opt_2. checkbox is checked: $isChecked")
        }
        val option3 = MessageOption("Opt_3") { isChecked ->
            notificationProvider.notify("Selected Opt_3. checkbox is checked: $isChecked")
        }

        val messageOptions = arrayOf(option1, option2, option3)
        val optionTitles = messageOptions.map { it.title }



        var isChecked = false
        val exitCode = Messages.showCheckboxMessageDialog(
            "message", "title", optionTitles.toTypedArray(), "checkbox_text", false, 0, 0,
            ImageIcon(javaClass.getResource("/toolWindowResources/Time-icon.png"))
        ) { exitCode, cb ->
            isChecked = cb.isSelected
            exitCode
        }

        if (exitCode == -1) {
            notificationProvider.notify("The message was closed")
        } else {
            messageOptions[exitCode].action(isChecked)
        }
    }
}

