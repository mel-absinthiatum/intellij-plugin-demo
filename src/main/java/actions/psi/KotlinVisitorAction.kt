package actions.psi

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.openapi.vfs.VfsUtilCore
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiManager
import org.jetbrains.kotlin.idea.util.isEffectivelyActual
import org.jetbrains.kotlin.idea.util.isExpectDeclaration
import org.jetbrains.kotlin.psi.*
import org.jetbrains.kotlin.psi.psiUtil.isPublic

class KotlinVisitorAction : AnAction() {
    override fun actionPerformed(e: AnActionEvent) {
        retrieveExpectedElements(e)
    }

    private fun retrieveExpectedElements(e: AnActionEvent) {
        val eventProject = e.project

        val rootManager = ProjectRootManager.getInstance(eventProject!!)

        val vFiles = rootManager.contentSourceRoots

        for (file in vFiles) {
            println("\nRoot: ${file.url}")
            VfsUtilCore.iterateChildrenRecursively(file, {
                true
            }, {
                val psiF = PsiManager.getInstance(eventProject).findFile(it)

                if (psiF != null && psiF.fileType.name == "Kotlin") {
                    psiF.acceptChildren(object : KtTreeVisitorVoid() {
                        override fun visitNamedDeclaration(declaration: KtNamedDeclaration) {
                            val public = declaration.isPublic
                            val expected = declaration.isExpectDeclaration()
                            val actual = declaration.isEffectivelyActual()

                            if (expected || actual) {
                                println("file name ${psiF.name}")
                                println("declaration: ${declaration.text}")

                                when (declaration) {
                                    is KtClass -> println("class")
                                    is KtNamedFunction -> {
                                        println("ssf ${declaration.bodyExpression}")
                                        println("function")
                                    }
                                    is KtProperty -> println("property")
                                }

                                declaration.name?.let { declaredName ->
                                    println("Declaration name $declaredName")

                                    super.visitNamedDeclaration(declaration)
                                }
                            }
                        }
                    })

                    println()
                    println()
                }

//                TODO
//                val refSearch = ReferencesSearch.search(this)
//                val importList = ktFile.importList

                true
            })
        }
    }

    private fun visit(file: KtFile) {
        // TODO: explore difference between various visitors
        // val visitor = namedDeclarationVisitor { declaredName ->
        //     println("Declaration name ${declaredName.name}")
        // }
        file.accept(object : KtTreeVisitor<PsiElement>() {
            override fun visitKtElement(element: KtElement, data: PsiElement?): Void? {
                super.visitKtElement(element, data)

                System.out.println("Found a variable at offset " + element.getTextRange().getStartOffset())
                return null
            }
        })
    }
}
