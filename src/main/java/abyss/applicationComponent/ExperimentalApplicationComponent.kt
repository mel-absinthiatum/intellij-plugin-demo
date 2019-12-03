package abyss.applicationComponent

import com.intellij.openapi.components.*
import com.intellij.openapi.project.ProjectManager

// Application level component.
class ExperimentalApplicationComponent: BaseComponent {

}

// Project level component.
@State(name = "abyss.applicationComponent.ExperimentalProjectService", storages = [Storage(value = "default.xml")])
class ExperimentalProjectComponent: ProjectComponent, PersistentStateComponent<Int> {
    override fun loadState(state: Int) {

    }

    override fun getState(): Int? {
        return 1
    }

    var title = "project component"

    override fun disposeComponent() {
        println("component dispose")
    }

    override fun getComponentName(): String {
        return "pro_component"
    }

    override fun initComponent() {
        println("component init")
    }

    override fun projectOpened() {
        println("component project opened")

        val PM = ProjectManager.getInstance()
        val AllProjects = PM.openProjects

        val experimentProjectService = ServiceManager.getService(ExperimentalProjectService::class.java)

        println("service: ${experimentProjectService.a}")
    }

    override fun projectClosed() {
        println("component project closed")
    }
}

@State(name = "abyss.applicationComponent.ExperimentalProjectService", storages = [Storage(value = "default.xml")])
class ExperimentalProjectService: PersistentStateComponent<ExperimentalProjectService.State> {
    class State(var value: Int = 0)

    var myState = State()

    override fun getState(): State {
        return myState
    }

    override fun loadState(state: State) {
        myState = state
    }

    val a = "a"
}
