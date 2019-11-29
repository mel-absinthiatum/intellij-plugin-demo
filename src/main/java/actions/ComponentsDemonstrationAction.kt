package actions

import abyss.applicationComponent.ExperimentalProjectComponent
import abyss.applicationComponent.ExperimentalProjectService
import com.intellij.ide.util.PropertiesComponent
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.components.ServiceManager
import com.intellij.openapi.ui.Messages

class ComponentsDemonstrationAction: AnAction() {

    /**
     * Replaces the run of selected text selected by the primary caret with a fixed string.
     * Displays the message with caret logical and visual position info.
     * @param e  Event related to this action
     */
    override fun actionPerformed(e: AnActionEvent) {
        val component = e.project?.getComponent(ExperimentalProjectComponent::class.java)
        if (component != null) {
            Messages.showInfoMessage("Component: ${component.title}", "Components demo")
        } else {
            Messages.showInfoMessage("Component not loaded", "Components demo")

        }
    }
}

class ServiceDemonstrationAction: AnAction() {

    override fun actionPerformed(e: AnActionEvent) {
        // TODO: Figure out how to use PropertiesComponent
        val propertiesComponent = PropertiesComponent.getInstance()
        val propCounter = propertiesComponent.getInt("counter", 10)
        propertiesComponent.setValue("counter", propCounter + 1, propCounter + 1)
        val service = ServiceManager.getService(ExperimentalProjectService::class.java)
        if (service != null) {
            val counter = service.state.value
            service.loadState(ExperimentalProjectService.State(counter + 1))
            Messages.showInfoMessage("Component called: $counter times\n"
                + "Properties counter: $propCounter", "Components demo")
        } else {
            Messages.showInfoMessage("Component not loaded", "Components demo")
        }
    }
}