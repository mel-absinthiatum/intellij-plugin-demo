package abyss.psi


import abyss.model.SharedType
import abyss.model.tree.nodes.*
import abyss.modulesRoutines.MppAuthorityManager
import abyss.modulesRoutines.MppAuthorityZone
import com.intellij.openapi.project.DumbServiceImpl
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VfsUtilCore
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiManager
import com.intellij.psi.stubs.StubIndex
import com.intellij.ui.treeStructure.Tree
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.jetbrains.kotlin.idea.stubindex.KotlinFullClassNameIndex
import org.jetbrains.kotlin.idea.util.actualsForExpected
import org.jetbrains.kotlin.idea.util.sourceRoots
import org.jetbrains.kotlin.psi.*
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine


@ExperimentalCoroutinesApi
class SharedTreeProvider {

    fun experimentString(project: Project, completion: (String) -> Unit) {
        DumbServiceImpl.getInstance(project).smartInvokeLater {
            runBlocking {
                println("before")
                delay(5000)
                println("after")
            }

            completion(project.name)
        }
    }

    suspend fun suspendedStringExperiment(project: Project): String = suspendCoroutine { cont ->
        DumbServiceImpl.getInstance(project).smartInvokeLater {
            runBlocking {
                //launch {

                println("before")
                delay(5000)
                println("after")
                //}
            }

            cont.resume(project.name)
        }
    }

    fun experimentStringRight(project: Project, completion: (String) -> Unit) {
        DumbServiceImpl.getInstance(project).smartInvokeLater {
            runBlocking {
                launch {
                    println("before")
                    delay(5000)
                    println("after")
                }
            }

            completion(project.name)
        }
    }


    fun tree(project: Project): Tree {
        val rootNode = RootNode()

        val nodes = iterateAllZones(project)
        rootNode.add(nodes)


        // DEBUG
        nodes.forEach { mppAuthorityZoneNode ->
            println("authority zone : ${mppAuthorityZoneNode.model.title}")
            mppAuthorityZoneNode.children.forEach { fileNode ->
                println("_file node: ${fileNode.model.title}")

                fileNode.children.forEach { node ->
                    println("__collected ${node.model.name}")

                    node.children.forEach { child ->
                        when (child) {
                            is SharedElementNode1 -> println("____collected: ${child.model.name}")
                            is ExpectOrActualNode1 -> println("_____expect or actual")
                        }
                    }
                }
            }
        }

        return Tree(rootNode)
    }

    private fun iterateAllZones(project: Project): List<MppAuthorityZoneNode1> {
        val mppAuthorityZones = MppAuthorityManager().provideAuthorityZonesForProject(project)
        return mppAuthorityZones.mapNotNull { authorityZone ->
            val list = iterateTree(authorityZone, project)

            if (list.isNotEmpty()) {
                val mppNodeModel = MppAuthorityZoneModel(authorityZone.commonModule.name)
                val node = MppAuthorityZoneNode1(mppNodeModel)
                node.add(list)
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

    private fun iterateTree(authorityZone: MppAuthorityZone, project: Project): List<PackageNode1> {
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
                val node = PackageNode1(fileNodeModel)
                node.add(children)
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
    ): List<SharedElementNode1> {
        val list = mutableListOf<SharedElementNode1>()
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
    ): List<SharedElementNode1> {
        val list = mutableListOf<SharedElementNode1>()
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

    private fun makeElementNode(declaration: PsiElement, sharedType: SharedType): SharedElementNode1? {
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
            node?.add(makeExpectNodeForElement(declaration))
            node?.add(makeActualNodesForElement(declaration))
        }
        return node
    }


    private fun makeExpectNodeForElement(element: PsiElement): ExpectOrActualNode1 {
        val model = ExpectOrActualModel(element, SharedType.EXPECTED, null)
        return ExpectOrActualNode1(model)
    }

    private fun makeActualNodesForElement(element: KtDeclaration): List<ExpectOrActualNode1> {
        val actuals = element.actualsForExpected()
        return actuals.map {
            val model = ExpectOrActualModel(it, SharedType.ACTUAL, null)
            ExpectOrActualNode1(model)
        }
    }


    private fun registerAnnotation(annotation: KtAnnotation, sharedType: SharedType): SharedElementNode1 {
        val stub = annotation.stub

        val model = SharedElementModel(annotation.name, sharedType, stub)
        println(annotation.name)
        return SharedElementNode1(model)
    }

    private fun registerProperty(property: KtProperty, sharedType: SharedType): SharedElementNode1 {
        val stub = property.stub
        val model = SharedElementModel(property.name, sharedType, stub)
        println(property.name)

        return SharedElementNode1(model)
    }

    private fun registerNamedFunction(function: KtNamedFunction, sharedType: SharedType): SharedElementNode1 {
        val stub = function.stub

        val model = SharedElementModel(function.name, sharedType, stub)
        return SharedElementNode1(model)
    }

    private fun registerClass(classDeclaration: KtClass, sharedType: SharedType): SharedElementNode1 {
        val stub = classDeclaration.stub

        val model = SharedElementModel(classDeclaration.name, sharedType, stub)

        val node = SharedElementNode1(model)

        val nested = classDeclaration.body?.children

        if (nested != null) {
            val children = registerNestedDeclaration(nested, sharedType)
            node.add(children)
        }

        return node
    }

    private fun registerObject(
        objectDeclaration: KtObjectDeclaration,
        sharedType: SharedType
    ): SharedElementNode1 {
        val stub = objectDeclaration.stub

        val model = SharedElementModel(objectDeclaration.name, sharedType, stub)

        val node = SharedElementNode1(model)

        val nested = objectDeclaration.body?.children

        if (nested != null) {
            val children = registerNestedDeclaration(nested, sharedType)
            node.add(children)
        }

        return node
    }

}
