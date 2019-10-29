package actions.psi

import com.intellij.lang.Language
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.openapi.vfs.VfsUtilCore
import com.intellij.psi.PsiManager


class ProjectTreePSIListAction : AnAction() {
    override fun actionPerformed(e: AnActionEvent) {
        val project = e.getRequiredData<Project>(CommonDataKeys.PROJECT)


        val projectName = project.name
        val rootManager = ProjectRootManager.getInstance(project)
        val contentRootUrls = rootManager.contentRootUrls.joinToString("\n")
        val vFiles = rootManager.contentSourceRoots

        val sourceRootsList = vFiles.map { it.url }.joinToString("\n")
        for (file in vFiles) {
            println("\n*** root: ${file.url}")
            VfsUtilCore.iterateChildrenRecursively(file, {
                true
            }, {
                val psiFile = PsiManager.getInstance(project).findFile(it)
                val kotlinLang = Language.findLanguageByID("kotlin")
                if (psiFile != null && kotlinLang != null && psiFile.viewProvider.hasLanguage(kotlinLang)) {
                    val fileViewProvider = psiFile.viewProvider
                    val language = fileViewProvider.baseLanguage
                    val languages = fileViewProvider.languages

                    val kotlinTree = fileViewProvider.getPsi(kotlinLang)
//                    PsiTreeUtil.getChildOfType()
//                    PsiRecursiveElementWalkingVisitor

//                    Messages.showInfoMessage(kotlinTree.toString(), "PSI")
                }

                true
            })
        }
        //org.jetbrains.kotlin.idea.KotlinFileType

    //        Messages.showInfoMessage("Source roots for the $projectName plugin:\n$sourceRootsList\n\n Content roots:\n$contentRootUrls", "Project Properties")


    //        LocalFileSystem


    //        VirtualFileManager.addVirtualFileListener()
            //
//        val projectFilePath = project.projectFilePath
//        val projectRootPath = project.basePath
//
//
//        SimpleNotificationProvider.notify(projectFilePath.toString(), projectRootPath.toString())
    }
}