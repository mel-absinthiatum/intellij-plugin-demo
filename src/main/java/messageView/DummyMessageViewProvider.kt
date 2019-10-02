package messageView

import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import javax.swing.ImageIcon


class DummyMessageViewProvider {
    fun showDummyMessage(project: Project) {
        val option1 = MessageOption("Opt_1_title") { isChecked ->
            println("Opt_1 checkbox is checked: $isChecked")
        }
        val option2 = MessageOption("Opt_2_title") { isChecked ->
            println("Opt_2 checkbox is checked: $isChecked")
        }
        val option3 = MessageOption("Opt_3_title") { isChecked ->
            println("Opt_3 checkbox is checked: $isChecked")
        }

        val messageOptions = arrayOf(option1, option2, option3)
        val optionTitles = messageOptions.map { it.title }

        // TODO: showCheckboxOkCancelDialog

//        val exitCode = Messages.showCheckboxOkCancelDialog("message", "title", "checkbox text", false, 0, 0, null)

        var isChecked = false
        val exitCode = Messages.showCheckboxMessageDialog(
            "message", "title", optionTitles.toTypedArray(), "checkbox_text", false, 0, 0,
            ImageIcon(javaClass.getResource("/toolWindowResources/Time-icon.png"))
        ) { exitCode, cb ->
            isChecked = cb.isSelected
            exitCode
        }

        if (exitCode == -1) {
            println("Close the message by `X` button")
        } else {
            messageOptions[exitCode].action(isChecked)
            println("$isChecked ___ $exitCode")
        }
        Messages.showMessageDialog(project, "HelloWorld", "Greeting", Messages.getInformationIcon())

        println("EXIT $exitCode")
    }
}


// Parameter `title` is the button title on the message view.
class MessageOption(val title: String, val action: (Boolean)->Unit)