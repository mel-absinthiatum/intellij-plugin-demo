package actions.psi

import abyss.model.ItemCoordinates
import abyss.model.SharedItemModel
import abyss.model.SharedType
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.openapi.vfs.VfsUtilCore
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiManager
import org.jetbrains.kotlin.idea.actions.pathBeforeJ2K
import org.jetbrains.kotlin.idea.util.isEffectivelyActual
import org.jetbrains.kotlin.idea.util.isExpectDeclaration
import org.jetbrains.kotlin.psi.*
import org.jetbrains.kotlin.psi.psiUtil.isPublic

class KotlinVisitorAction : AnAction() {
    override fun actionPerformed(e: AnActionEvent) {
        retrieveExpectedElements(e)
    }

    private fun retrieveExpectedElements(e: AnActionEvent) {
        var modelList: MutableList<SharedItemModel> = arrayListOf()
        var expectedModelList: Array<SharedItemModel> = arrayOf()

        val eventProject = e.project
        val rootManager = ProjectRootManager.getInstance(eventProject!!)
        val vFiles = rootManager.contentSourceRoots

        for (file in vFiles) {
            println("\nRoot: ${file.url}")
            VfsUtilCore.iterateChildrenRecursively(file, {
                true
            }, {
                val canonicalPath = it.canonicalPath
                val path = it.path
                val path2 = it.pathBeforeJ2K
                val psiF = PsiManager.getInstance(eventProject).findFile(it)

                if (psiF != null && psiF.fileType.name == "Kotlin") {
                    psiF.acceptChildren(object : KtTreeVisitorVoid() {
                        override fun visitNamedDeclaration(declaration: KtNamedDeclaration) {
                            val expected = declaration.isExpectDeclaration()
                            val actual = declaration.isEffectivelyActual()

                            val sharedType: SharedType =
                            when {
                                (expected) -> SharedType.EXPECTED
                                (actual) -> SharedType.ACTUAL
                                else -> return
                            }

                            val name = declaration.name ?: ""
                            val text = declaration.text ?: ""

                            val public = declaration.isPublic

                            println("file name ${psiF.name}")

                            when (declaration) {
                                is KtClass -> {
                                    val itemCoordinates =  ItemCoordinates(path, declaration.textOffset)
                                    val item = SharedItemModel(name, text, sharedType, itemCoordinates)
                                    modelList.add(item)

                                }
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
