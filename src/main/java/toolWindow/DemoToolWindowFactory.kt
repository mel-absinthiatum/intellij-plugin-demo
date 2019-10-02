package toolWindow

import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.*
import com.intellij.ui.content.*


class DemoToolWindowFactory : ToolWindowFactory {
    // Create the tool window content.
    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val myToolWindow = DemoToolWindow(project, toolWindow)
        val contentFactory = ContentFactory.SERVICE.getInstance()
        val content = contentFactory.createContent(myToolWindow.content, "", false)
        toolWindow.contentManager.addContent(content)
    }
}
