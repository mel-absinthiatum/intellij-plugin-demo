package abyss.treeUpdateManager.psi

// TODO: Use ReadAction.compute(() -> {
// TODO: Use coroutines
// Each specific index implementation is a class extending FileBasedIndexExtension.
// A file-based index should be registered in the <fileBasedIndex> extension point.
// Generally, the word index should be accessed indirectly by using helper methods of the PsiSearchHelper class.
// The second index is FilenameIndex. It provides a quick way to find all files matching a certain file name.
// FileTypeIndex serves a similar goal: it allows to quickly find all files of a certain file type.

import abyss.model.*
import com.intellij.openapi.project.DumbServiceImpl
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.openapi.vfs.VfsUtilCore
import com.intellij.psi.PsiManager
import org.jetbrains.kotlin.idea.actions.pathBeforeJ2K
import org.jetbrains.kotlin.idea.util.isEffectivelyActual
import org.jetbrains.kotlin.idea.util.isExpectDeclaration
import org.jetbrains.kotlin.psi.*
import org.jetbrains.kotlin.psi.psiUtil.isPublic

class SharedItemsProvider {
    fun getSharedItems(project: Project, completion: (Collection<SharedItemModel>) -> Unit) {
        DumbServiceImpl.getInstance(project).smartInvokeLater {
            val collection = retrieveSharedItems(project)
            completion(collection)
        }
    }

    private fun retrieveSharedItems(project: Project): Collection<SharedItemModel> {
        val expectedCoordinatesMap: MutableMap<ItemMetaInfo, ItemCoordinates> = mutableMapOf()
        val actualCoordinatesMap: MutableMap<ItemMetaInfo, MutableSet<ItemCoordinates>> = mutableMapOf()
        val metaInfoSet: MutableSet<ItemMetaInfo> = mutableSetOf()


        val rootManager = ProjectRootManager.getInstance(project)
        val virtualFiles = rootManager.contentSourceRoots

        for (file in virtualFiles) {
            println("\nRoot: ${file.url}")
            VfsUtilCore.iterateChildrenRecursively(file, {
                true
            }, { virtualFile ->
                val canonicalPath = virtualFile.canonicalPath
                val path = virtualFile.path
                val path2 = virtualFile.pathBeforeJ2K
                val psiFile = PsiManager.getInstance(project).findFile(virtualFile)

                if (psiFile != null && psiFile.fileType.name == "Kotlin") {
                    psiFile.accept(object : KtTreeVisitorVoid() {
                        override fun visitNamedDeclaration(declaration: KtNamedDeclaration) {
                            val expected = declaration.isExpectDeclaration()
                            val actual = declaration.isEffectivelyActual()

                            val sharedType: SharedType =
                                when {
                                    (expected) -> SharedType.EXPECTED
                                    (actual) -> SharedType.ACTUAL
                                    else -> return
                                }

                            // TODO: Handle emptiness
                            val name = declaration.name ?: ""
                            val text = declaration.text ?: ""

                            val public = declaration.isPublic

                            val declarationType: DeclarationType =
                                when (declaration) {
                                    is KtClass -> DeclarationType.CLASS
                                    is KtNamedFunction -> DeclarationType.NAMED_FUNCTION
                                    is KtProperty -> DeclarationType.PROPERTY
                                    is KtObjectDeclaration -> DeclarationType.OBJECT
                                    is KtTypeAlias -> DeclarationType.CLASS
                                    else -> return//DeclarationType.UNRESOLVED
                                }

                            val itemCoordinates =  ItemCoordinates(path, declaration.textOffset, text)
                            val itemInfo = ItemMetaInfo(name, declarationType)

                            metaInfoSet.add(itemInfo)
                            if (sharedType == SharedType.EXPECTED) {
                                expectedCoordinatesMap[itemInfo] = itemCoordinates
                            } else if (sharedType == SharedType.ACTUAL) {
                                val key = actualCoordinatesMap.keys.findLast {
                                    it == itemInfo
                                }
                                if (actualCoordinatesMap[itemInfo] != null) {
                                    (actualCoordinatesMap[itemInfo])?.add(itemCoordinates)
                                } else {
                                    (actualCoordinatesMap[itemInfo]) = mutableSetOf(itemCoordinates)
                                }
                            }

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
