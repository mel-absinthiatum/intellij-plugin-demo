package dialog.fileBrowseWindow

import com.intellij.openapi.fileChooser.FileChooserDescriptor
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.TextFieldWithBrowseButton
import com.intellij.util.ui.JBUI
import java.awt.BorderLayout
import javax.swing.JPanel
import javax.swing.JTextField


class FileBrowsePanel(private val project: Project) : JPanel() {
    val searchFieldText get() = searchField.text

    private val searchField: TextFieldWithBrowseButton

    init {
        layout = BorderLayout()

        searchField = TextFieldWithBrowseButton(JTextField())
        searchField.border = JBUI.Borders.empty(8, 10)
        add(searchField, BorderLayout.CENTER)

        val descriptor = FileChooserDescriptor(true, false, false, false, false, false)
        searchField.addBrowseFolderListener("title of file chooser","description for some reason is here", project, descriptor)
    }
}
