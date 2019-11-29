package abyss.psi

import abyss.model.ItemCoordinates
import abyss.model.ItemMetaInfo
import abyss.model.SharedItemModel
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.openapi.vfs.VfsUtilCore
import com.intellij.psi.PsiManager
import org.jetbrains.kotlin.idea.actions.pathBeforeJ2K
import org.jetbrains.kotlin.psi.KtNamedDeclaration
import org.jetbrains.kotlin.psi.KtTreeVisitorVoid

class SharedItemsProvider {

    fun retrieveSharedItems(project: Project): Collection<SharedItemModel> {
        var expectedCoordinatesMap: MutableMap<ItemMetaInfo, ItemCoordinates> = mutableMapOf()
        var actualCoordinatesMap: MutableMap<ItemMetaInfo, MutableSet<ItemCoordinates>> = mutableMapOf()
        var metaInfoSet: MutableSet<ItemMetaInfo> = mutableSetOf()


        val rootManager = ProjectRootManager.getInstance(project)
        val vFiles = rootManager.contentSourceRoots

        for (file in vFiles) {
            println("\nRoot: ${file.url}")
            VfsUtilCore.iterateChildrenRecursively(file, {
                true
            }, { virtualFile ->
                val canonicalPath = virtualFile.canonicalPath
                val path = virtualFile.path
                val path2 = virtualFile.pathBeforeJ2K
                val psiF = PsiManager.getInstance(project).findFile(virtualFile)

                if (psiF != null && psiF.fileType.name == "Kotlin") {
                    psiF.accept(object : KtTreeVisitorVoid() {
                        override fun visitNamedDeclaration(declaration: KtNamedDeclaration) {
//                            val expected = declaration.isExpectDeclaration()
//                            val actual = declaration.isEffectivelyActual()
//
//                            val sharedType: SharedType =
//                                when {
//                                    (expected) -> SharedType.EXPECTED
//                                    (actual) -> SharedType.ACTUAL
//                                    else -> return
//                                }
//
//                            // TODO: Handle emptiness
//                            val name = declaration.name ?: ""
//                            val text = declaration.text ?: ""
//
//                            val public = declaration.isPublic
//
//                            val declarationType: DeclarationType =
//                                when (declaration) {
//                                    is KtClass -> DeclarationType.CLASS
//                                    is KtNamedFunction -> DeclarationType.NAMED_FUNCTION
//                                    is KtProperty -> DeclarationType.PROPERTY
//                                    is KtObjectDeclaration -> DeclarationType.OBJECT
//                                    is KtTypeAlias -> DeclarationType.CLASS
//                                    else -> return//DeclarationType.UNRESOLVED
//                                }
//
//                            val itemCoordinates =  ItemCoordinates(path, declaration.textOffset, text)
//                            val itemInfo = ItemMetaInfo(name, declarationType)
//
//                            metaInfoSet.add(itemInfo)
//                            if (sharedType == SharedType.EXPECTED) {
//                                expectedCoordinatesMap[itemInfo] = itemCoordinates
//                            } else if (sharedType == SharedType.ACTUAL) {
//                                val key = actualCoordinatesMap.keys.findLast {
//                                    it == itemInfo
//                                }
//                                if (actualCoordinatesMap[itemInfo] != null) {
//                                    (actualCoordinatesMap[itemInfo])?.add(itemCoordinates)
//                                } else {
//                                    (actualCoordinatesMap[itemInfo]) = mutableSetOf(itemCoordinates)
//                                }
//                            }

                            super.visitNamedDeclaration(declaration)
                        }

                    })
                }

                true
            })
        }

        val items = metaInfoSet.map {
            SharedItemModel(it)
        }

        expectedCoordinatesMap.forEach { info, coordinates ->
            items.findLast { it.metaInfo == info }?.expectedItem = coordinates
        }

        actualCoordinatesMap.forEach{ info, coordinates ->
            items.findLast { it.metaInfo == info }?.actualItems = coordinates
        }

        println(items.joinToString("\n\n"))
        return items
    }
}
