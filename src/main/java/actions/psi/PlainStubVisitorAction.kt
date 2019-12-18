package actions.psi


import abyss.model.SharedType
import abyss.model.tree.nodes.*
import abyss.modulesRoutines.MppAuthorityManager
import abyss.modulesRoutines.MppAuthorityZone
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.DumbServiceImpl
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VfsUtilCore
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiManager
import com.intellij.psi.stubs.StubIndex
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.jetbrains.kotlin.idea.stubindex.KotlinFullClassNameIndex
import org.jetbrains.kotlin.idea.util.actualsForExpected
import org.jetbrains.kotlin.idea.util.sourceRoots
import org.jetbrains.kotlin.psi.*


@ExperimentalCoroutinesApi
class PlainStubVisitorAction : AnAction() {

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project
        if (project != null) {
            DumbServiceImpl.getInstance(project).smartInvokeLater {

                val nodes = iterateAllZones(project)

                nodes.forEach { mppAuthorityZoneNode ->
                    println("authority zone : ${mppAuthorityZoneNode.model.title}")
                    mppAuthorityZoneNode.children.forEach { fileNode ->
                        println("_file node: ${fileNode.model.title}")

                        fileNode.children.forEach { node ->
                            println("__collected ${node.model.name}")

                            node.children.forEach { child ->
                                when (child) {
                                    is SharedItemNode -> println("____collected: ${child.model.name}")
                                    is ExpectOrActuaItemlNode -> println("_____expect or actual")
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun iterateAllZones(project: Project): List<MppAuthorityZoneNode> {
        val mppAuthorityZones = MppAuthorityManager().provideAuthorityZonesForProject(project)
        return mppAuthorityZones.mapNotNull { authorityZone ->
            val list = iterateTree(authorityZone, project)

            if (list.isNotEmpty()) {
                val mppNodeModel = MppAuthorityZoneModel(authorityZone.commonModule.name)
                val node = MppAuthorityZoneNode(mppNodeModel)
                node.addChildren(list)
                node
            } else {
                null
            }
//            list.forEach { fileNode ->
//                println("file node: ${fileNode.model.title}")
//
//                fileNode.children.forEach { node ->
//                    println("__collected ${node.model.name}")
//
//                    node.children.forEach { child ->
//                        when (child) {
//                            is SharedItemNode -> println("____collected: ${child.model.name}")
//                            is ExpectOrActuaItemlNode -> println("_____expect or actual")
//                        }
//                    }
//                }
//
//            }
        }
    }

    private fun iterateTree(authorityZone: MppAuthorityZone, project: Project): List<PackageNode> {
        val sourceRoots = authorityZone.commonModule.sourceRoots

        val psiFiles = mutableListOf<PsiFile>()
        sourceRoots.forEach { vf ->
            VfsUtilCore.iterateChildrenRecursively(vf, null, { virtualFile ->
                val psiFile = PsiManager.getInstance(project).findFile(virtualFile)
                if (psiFile != null) {
                    psiFiles.add(psiFile)
                }
                true
            })
        }

        val list = psiFiles.mapNotNull { psiFile ->
            val children = registerDeclaration(psiFile, SharedType.EXPECTED)

            if (children.isNotEmpty()) {
                val fileNodeModel = PackageModel(psiFile.name, psiFile.virtualFile)
                val node = PackageNode(fileNodeModel)
                node.addChildren(children)
                node
            } else {
                null
            }
        }
        return list
    }

    private fun indexes(project: Project) {
        val key = KotlinFullClassNameIndex.getInstance().key
        val valNames = StubIndex.getInstance().getAllKeys(key, project)
        valNames.forEach { println("index $it") }
    }

    private fun registerDeclaration(
        element: PsiElement,
        sharedType: SharedType
    ): List<SharedItemNode> {
        val list = mutableListOf<SharedItemNode>()
        element.acceptChildren(
            namedDeclarationVisitor { declaration ->
                val node = makeElementNode(declaration, sharedType)
                if (node != null) {
                    list.add(node)
                }
            })
        return list
    }

    private fun registerNestedDeclaration(
        element: Array<PsiElement>,
        sharedType: SharedType
    ): List<SharedItemNode> {
        val list = mutableListOf<SharedItemNode>()
        element.forEach {
            it.accept(
                namedDeclarationVisitor { declaration ->
                    println("fqname: ${declaration.fqName}")

                    val node = makeElementNode(declaration, sharedType)
                    if (node != null) {
                        list.add(node)
                    }
                })
        }
        return list
    }

    private fun makeElementNode(declaration: PsiElement, sharedType: SharedType): SharedItemNode? {
        val node = when (declaration) {
            is KtAnnotation -> registerAnnotation(declaration, sharedType)
            is KtClass -> registerClass(declaration, sharedType)
            is KtNamedFunction -> registerNamedFunction(declaration, sharedType)
            is KtProperty -> registerProperty(declaration, sharedType)
            is KtObjectDeclaration -> registerObject(declaration, sharedType)
            is KtTypeAlias -> {
                val stub = declaration.stub
                null
            }
            else -> null
        }
        if (declaration is KtDeclaration) {
            node?.addChild(makeExpectNodeForElement(declaration))
            node?.addChildren(makeActualNodesForElement(declaration))
        }
        return node
    }


    private fun makeExpectNodeForElement(element: PsiElement): ExpectOrActuaItemlNode {
        val model = ExpectOrActualModel(element, SharedType.EXPECTED, null)
        return ExpectOrActuaItemlNode(model)
    }

    private fun makeActualNodesForElement(element: KtDeclaration): List<ExpectOrActuaItemlNode> {
        val actuals = element.actualsForExpected()
        return actuals.map {
            val model = ExpectOrActualModel(it, SharedType.ACTUAL, null)
            ExpectOrActuaItemlNode(model)
        }
    }


    private fun registerAnnotation(annotation: KtAnnotation, sharedType: SharedType): SharedItemNode {
        val stub = annotation.stub

        val model = SharedElementModel(annotation.name, sharedType, stub)
        println(annotation.name)
        return SharedItemNode(model)
    }

    private fun registerProperty(property: KtProperty, sharedType: SharedType): SharedItemNode {
        val stub = property.stub
        val model = SharedElementModel(property.name, sharedType, stub)
        println(property.name)

        return SharedItemNode(model)
    }

    private fun registerNamedFunction(function: KtNamedFunction, sharedType: SharedType): SharedItemNode {
        val stub = function.stub

        val model = SharedElementModel(function.name, sharedType, stub)
        return SharedItemNode(model)
    }

    private fun registerClass(classDeclaration: KtClass, sharedType: SharedType): SharedItemNode {
        val stub = classDeclaration.stub

        val model = SharedElementModel(classDeclaration.name, sharedType, stub)

        val node = SharedItemNode(model)

        val nested = classDeclaration.body?.children

        if (nested != null) {
            val children = registerNestedDeclaration(nested, sharedType)
            node.addChildren(children)
        }

        return node
    }

    private fun registerObject(
        objectDeclaration: KtObjectDeclaration,
        sharedType: SharedType
    ): SharedItemNode {
        val stub = objectDeclaration.stub

        val model = SharedElementModel(objectDeclaration.name, sharedType, stub)

        val node = SharedItemNode(model)

        val nested = objectDeclaration.body?.children

        if (nested != null) {
            val children = registerNestedDeclaration(nested, sharedType)
            node.addChildren(children)
        }

        return node
    }

}
