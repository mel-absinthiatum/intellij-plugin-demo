package actions.psi

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.openapi.ui.Messages


class ProjectTreePSIListAction : AnAction() {
    override fun actionPerformed(e: AnActionEvent) {
        val project = e.getRequiredData<Project>(CommonDataKeys.PROJECT)


        val projectName = project.name
        val rootManager = ProjectRootManager.getInstance(project)
        val contentRootUrls = rootManager.contentRootUrls.joinToString("\n")
        val vFiles = rootManager.contentSourceRoots

        val sourceRootsList = vFiles.map { it.url }.joinToString("\n")


        Messages.showInfoMessage("Source roots for the $projectName plugin:\n$sourceRootsList\n\n Content roots:\n$contentRootUrls", "Project Properties")

//
//        val projectFilePath = project.projectFilePath
//        val projectRootPath = project.basePath
//
//
//        SimpleNotificationProvider.notify(projectFilePath.toString(), projectRootPath.toString())
    }
}