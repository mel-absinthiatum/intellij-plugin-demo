package actions.psi


import abyss.model.SharedType
import abyss.model.tree.nodes.ExpectOrActuaItemlNode
import abyss.model.tree.nodes.SharedElementModel
import abyss.model.tree.nodes.SharedItemNode
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
import org.jetbrains.kotlin.idea.util.sourceRoots
import org.jetbrains.kotlin.psi.*


@ExperimentalCoroutinesApi
class PlainStubVisitorAction : AnAction() {

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project
        if (project != null) {
            DumbServiceImpl.getInstance(project).smartInvokeLater {

                iterateAllZones(project)

            }
        }
    }

    private fun iterateAllZones(project: Project) {
        val mppAuthorityZones = MppAuthorityManager().provideAuthorityZonesForProject(project)
        mppAuthorityZones.forEach { authorityZone ->
            val list = iterateTree(authorityZone, project)
            list.forEach { node ->
                println("collected ${node.model.name}")

                node.children.forEach { child ->
                    when (child) {
                        is SharedItemNode -> println("collected: ${child.model.name}")
                        is ExpectOrActuaItemlNode -> println("expect or actual")
                    }
                }
            }
        }
    }

    private fun iterateTree(authorityZone: MppAuthorityZone, project: Project): List<SharedItemNode> {
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

        val list = psiFiles.map { psiFile ->
            registerDeclaration(psiFile, SharedType.EXPECTED)
        }.flatten()
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
                val node =
                    when (declaration) {
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
                if (node != null) {
                    list.add(node)
                }
            })
        return list
    }

    private fun registerDeclaration1(
        element: Array<PsiElement>,
        sharedType: SharedType
    ): List<SharedItemNode> {
        val list = mutableListOf<SharedItemNode>()
        element.forEach {
            it.accept(
                namedDeclarationVisitor { declaration ->
                    println("fqname: ${declaration.fqName}")

                    val node =
                        when (declaration) {
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
                    if (node != null) {
                        list.add(node)
                    }
                })
        }
        return list
    }


//    private suspend fun registerDeclaration1(
//        element: PsiElement,
//        sharedType: SharedType
//    ): Flow<SharedElementNode?> = channelFlow {
//        println("register declaration method")
//
//        element.acceptChildren(
//            namedDeclarationVisitor { declaration ->
//                launch {
//                    println("register declaration method launch")
//
//                    when (declaration) {
//                        is KtAnnotation -> send(registerAnnotation(declaration, sharedType))
//                        is KtClass -> send(registerClass(declaration, sharedType))
//                        is KtNamedFunction -> send(registerNamedFunction(declaration, sharedType))
//                        is KtProperty -> send(registerProperty(declaration, sharedType))
//                        is KtObjectDeclaration -> send(registerObject(declaration, sharedType))
//                        is KtTypeAlias -> {
//                            val stub = declaration.stub
//                            send(null)
//                        }
//                        else -> send(null)
//                    }
//                }
//            })
//    }

    private fun makeElementNode(declaration: PsiElement, sharedType: SharedType): SharedItemNode? {
        return when (declaration) {
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
        if (stub == null) {
            println("Achtung!!!")
        }

        val model = SharedElementModel(classDeclaration.name, sharedType, stub)

        val node = SharedItemNode(model)

        val nested = classDeclaration.body?.children

        if (nested != null) {
            val children = registerDeclaration1(nested, sharedType)
            node.addChildren(children)
        }


//        childrenFlow.collect {
//            println("azis;jnedsin")
//            if (it is SharedElementNode) {
//                node.addChildNode(it)
//            }
//        }

        return node
    }

    private fun registerObject(
        objectDeclaration: KtObjectDeclaration,
        sharedType: SharedType
    ): SharedItemNode {
        val stub = objectDeclaration.stub

        val model = SharedElementModel(objectDeclaration.name, sharedType, stub)

        val node = SharedItemNode(model)

        val children = registerDeclaration(objectDeclaration, sharedType)

        node.addChildren(children)

        return node
    }

}
