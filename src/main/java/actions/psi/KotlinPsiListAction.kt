package actions.psi

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.openapi.vfs.VfsUtilCore
import com.intellij.psi.PsiManager
import org.jetbrains.kotlin.cli.jvm.compiler.EnvironmentConfigFiles
import org.jetbrains.kotlin.cli.jvm.compiler.KotlinCoreEnvironment
import org.jetbrains.kotlin.com.intellij.openapi.util.Disposer
import org.jetbrains.kotlin.com.intellij.testFramework.LightVirtualFile
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.idea.KotlinFileType
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.com.intellij.openapi.project.Project as KProject
import org.jetbrains.kotlin.com.intellij.psi.PsiManager as KPsiManager


class KotlinPsiListAction : AnAction() {

    override fun actionPerformed(e: AnActionEvent) {
        val eventProject = e.project

        val rootManager = ProjectRootManager.getInstance(eventProject!!)

        val vFiles = rootManager.contentSourceRoots

        for (file in vFiles) {
            println("\nRoot: ${file.url}")
            VfsUtilCore.iterateChildrenRecursively(file, {
                true
            }, {
                val doc = FileDocumentManager.getInstance().getDocument(it)
                val text = doc?.charsSequence
                val psiFile = PsiManager.getInstance(eventProject).findFile(it)
                println("File: $psiFile")

                if (text != null) {
                    val ktFile = parsePsiFile(it.name, text)

                    if (ktFile != null) {
                        println("File content: $text")
                    }
                }
                true
            })
        }
    }

    private fun parsePsiFile(name: String, code: CharSequence): KtFile? {
        val project = project()
        return KPsiManager.getInstance(project)
            .findFile(LightVirtualFile(name, KotlinFileType.INSTANCE, code)) as KtFile?
    }

    private fun project(): KProject {
        return KotlinCoreEnvironment.createForProduction(
            Disposer.newDisposable(),
            CompilerConfiguration(),
            EnvironmentConfigFiles.JVM_CONFIG_FILES
        ).project
    }
}
