package actions.psi

import abyss.model.*
import abyss.modulesRoutines.MppAuthorityManager
import com.intellij.ProjectTopics
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.module.ModuleManager
import com.intellij.openapi.project.ModuleListener
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.openapi.vfs.VfsUtilCore
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiManager
import org.jetbrains.kotlin.idea.actions.pathBeforeJ2K
import org.jetbrains.kotlin.idea.caches.project.isMPPModule
import org.jetbrains.kotlin.idea.caches.project.isNewMPPModule
import org.jetbrains.kotlin.idea.project.platform
import org.jetbrains.kotlin.idea.util.isEffectivelyActual
import org.jetbrains.kotlin.idea.util.isExpectDeclaration
import org.jetbrains.kotlin.idea.util.projectStructure.sdk
import org.jetbrains.kotlin.platform.impl.CommonIdePlatformKind
import org.jetbrains.kotlin.platform.impl.JvmIdePlatformKind
import org.jetbrains.kotlin.platform.impl.NativeIdePlatformKind
import org.jetbrains.kotlin.psi.*
import org.jetbrains.kotlin.psi.psiUtil.isPublic


class KotlinVisitorAction : AnAction() {
    override fun actionPerformed(e: AnActionEvent) {
//        retrieveModules(e)

        val project = e.project
        if (project != null) {
            retrieveStubs(project)
        }
    }

    private fun retrieveStubs(project: Project) {
        val mppAuthorityZones = MppAuthorityManager().provideAuthorityZonesForProject(project)

        mppAuthorityZones.forEach { authorityZone ->
            val commonModule = authorityZone.commonModule
            println("Authority zone root name: ${commonModule.name}")
            val commonModulePath = commonModule.moduleFilePath
            val commonModuleVF = commonModule.moduleFile
            val commonModuleScope = commonModule.moduleScope

        }

    }

    private fun retrieveModules(e: AnActionEvent) {
        val project = e.project
        if (project != null) {
            project.getMessageBus().connect().subscribe(ProjectTopics.MODULES, object : ModuleListener {
                fun moduleAdded(project: Project, module: Module) {
                    // TODO: Handle module routines.
                }
            })

            val modules = ModuleManager.getInstance(project).modules
            modules.filter{ it.isMPPModule }.forEach { module ->
                if (module.isMPPModule == true) {
                    println()
                    println("module path: ${module.moduleFilePath}")
                    println("module name ${module.name}")
                    println("module type: ${module.moduleTypeName}")
                    println("module type MPP: ${module.isNewMPPModule}")
                    println("module sdk: ${module.sdk}")
                    println("platform ${module.platform}")

                    val platformKind = module.platform?.kind
                    println("platform native: ${platformKind == NativeIdePlatformKind}")
                    println("platform common ${platformKind == CommonIdePlatformKind}")
                    println("platform jvm ${platformKind == JvmIdePlatformKind}")

                    if (platformKind == CommonIdePlatformKind) {
                        val dependantModules = ModuleManager.getInstance(project).getModuleDependentModules(module)
                        dependantModules.filter{ it.isMPPModule }.forEach {
                            println(" ____ ${it.name}")
                        }
                    }

                }
            }
        }
    }

    private fun retrieveExpectedElements(e: AnActionEvent) {
        var expectedCoordinatesMap: MutableMap<ItemMetaInfo, ItemCoordinates> = mutableMapOf()
        var actualCoordinatesMap: MutableMap<ItemMetaInfo, MutableSet<ItemCoordinates>> = mutableMapOf()
        var metaInfoSet: MutableSet<ItemMetaInfo> = mutableSetOf()

        val eventProject = e.project
        val rootManager = ProjectRootManager.getInstance(eventProject!!)
        val vFiles = rootManager.contentSourceRoots

        for (file in vFiles) {
            println("\nRoot: ${file.url}")
            VfsUtilCore.iterateChildrenRecursively(file, {
                true
            }, { virtualFile ->
                val canonicalPath = virtualFile.canonicalPath
                val path = virtualFile.path
                val path2 = virtualFile.pathBeforeJ2K
                val psiF = PsiManager.getInstance(eventProject).findFile(virtualFile)

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

//                TODO
//                val refSearch = ReferencesSearch.search(this)
//                val importList = ktFile.importList

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
    }

    private fun visit(file: KtFile) {
        // TODO: explore difference between various visitors
        // val visitor = namedDeclarationVisitor { declaredName ->
        //     println("Declaration name ${declaredName.name}")
        // }
        file.accept(object : KtTreeVisitor<PsiElement>() {
            override fun visitKtElement(element: KtElement, data: PsiElement?): Void? {
                super.visitKtElement(element, data)

                println("Found a variable at offset " + element.getTextRange().getStartOffset())
                return null
            }
        })
    }
}
