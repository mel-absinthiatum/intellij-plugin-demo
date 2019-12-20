package actions.psi


// TODO ExpectActualUtilKt

// TODO Generally, the word index should be accessed indirectly by using helper methods of the PsiSearchHelper class.
//  KotlinExpectOrActualGotoRelatedProvider

// TODO addFromIndex(KotlinTopLevelClassByPackageIndex.getInstance())


//val a = KotlinTypeAliasStubImpl()
//                KotlinStub
//                val key = StubIndexKey.createIndexKey<>()
//StubIndex.
//val keys = StubIndex.getInstance().getAllKeys(StubIndexKey.createIndexKey<Any, PsiElement>("expect"), project)
//keys.forEach {
//    println(" __ stub: $it")
//}
//StubBuildingVisitor

//import org.jetbrains.kotlin.idea.util.isEffectivelyActual

//                KotlinExpectOrActualGotoRelatedProvider
//KotlinIndex
//                KotlinFunctionInde


import abyss.model.DeclarationType
import abyss.model.SharedType
import abyss.model.tree.nodes.OldSharedElementNode
import abyss.model.tree.nodes.SharedElementModel
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
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.jetbrains.kotlin.idea.refactoring.fqName.getKotlinFqName
import org.jetbrains.kotlin.idea.stubindex.KotlinFullClassNameIndex
import org.jetbrains.kotlin.idea.util.actualsForExpected
import org.jetbrains.kotlin.idea.util.isExpectDeclaration
import org.jetbrains.kotlin.idea.util.sourceRoots
import org.jetbrains.kotlin.psi.*


@ExperimentalCoroutinesApi
class StubVisitorAction : AnAction() {

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project
        if (project != null) {
            println("action")
            DumbServiceImpl.getInstance(project).smartInvokeLater {
                runBlocking {
                    launch {

                        println("launch1")

                        iterateAllZones(project)

                        println("launch2")

                    }
                    println("launched")
                }
                println("block off")
            }
        }
    }

    private suspend fun iterateAllZones(project: Project) {
        val mppAuthorityZones = MppAuthorityManager().provideAuthorityZonesForProject(project)
        mppAuthorityZones.forEach { authorityZone ->
            var flow: Flow<OldSharedElementNode?>? = null
            runBlocking {

                flow = iterateTree(authorityZone, project)
                flow?.collect {
                    println("collected ${it?.model?.name}")
                    it?.sharedChildren?.forEach { nested ->
                        println("__ ${nested}")
                    }
                }
            }
            flow?.collect {
                println("2 collected ${it?.model?.name}")
                it?.sharedChildren?.forEach { nested ->
                    println("2 __ ${nested}")
                }
            }
        }
    }

    private suspend fun iterateTree(authorityZone: MppAuthorityZone, project: Project): Flow<OldSharedElementNode?> {
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

        val dumbService = DumbServiceImpl.getInstance(project)

        val flows = psiFiles.map { psiFile ->
            registerDeclaration(psiFile, SharedType.EXPECTED)
        }.toTypedArray()
        val flattenFlows = flowOf(*flows).flattenMerge()
        return flattenFlows
    }

    private fun indexes(project: Project) {
        val key = KotlinFullClassNameIndex.getInstance().key
        val valNames = StubIndex.getInstance().getAllKeys(key, project)
        valNames.forEach { println("index $it") }
    }

    private suspend fun registerDeclaration(
        element: PsiFile,
        sharedType: SharedType
    ): Flow<OldSharedElementNode?> = channelFlow {
        //        println("register declaration method")

        element.acceptChildren(
            namedDeclarationVisitor { declaration ->
                launch {
                    //                    println("register declaration method launch")

                    when (declaration) {
                        is KtAnnotation -> {
                            send(registerAnnotation(declaration, sharedType))
                        }
                        is KtClass -> {
                            send(registerClass(declaration, sharedType))
                        }
                        is KtNamedFunction -> {
                            send(registerNamedFunction(declaration, sharedType))
                        }
                        is KtProperty -> {
                            send(registerProperty(declaration, sharedType))
                        }
                        is KtObjectDeclaration -> {
                            send(registerObject(declaration, sharedType))
                        }
                        is KtTypeAlias -> {
                            val stub = declaration.stub
                            send(null)
                        }
                        else -> send(null)
                    }
                }
            })
    }


    private suspend fun registerDeclaration1(
        element: PsiElement,
        sharedType: SharedType
    ): Flow<OldSharedElementNode?> = channelFlow {
        //        println("register declaration method")

        element.acceptChildren(
            namedDeclarationVisitor { declaration ->
                launch {
                    //                    println("register declaration method launch")

                    when (declaration) {
                        is KtAnnotation -> send(registerAnnotation(declaration, sharedType))
                        is KtClass -> send(registerClass(declaration, sharedType))
                        is KtNamedFunction -> send(registerNamedFunction(declaration, sharedType))
                        is KtProperty -> send(registerProperty(declaration, sharedType))
                        is KtObjectDeclaration -> send(registerObject(declaration, sharedType))
                        is KtTypeAlias -> {
                            val stub = declaration.stub
                            send(null)
                        }
                        else -> send(null)
                    }
                }
            })
    }

    private fun registerNestedDeclaration(
        element: Array<PsiElement>,
        sharedType: SharedType
    ): Flow<OldSharedElementNode> = channelFlow {
        //        println("register nested declaration")
        element.forEach {
            it.accept(
                namedDeclarationVisitor { declaration ->
                    println("fqname: ${declaration.fqName}")
                    launch {

                        when (declaration) {
                            is KtAnnotation -> send(registerAnnotation(declaration, sharedType))
                            is KtClass -> send(registerClass(declaration, sharedType))
                            is KtNamedFunction -> send(registerNamedFunction(declaration, sharedType))
                            is KtProperty -> send(registerProperty(declaration, sharedType))
                            is KtObjectDeclaration -> send(registerObject(declaration, sharedType))
                            is KtTypeAlias -> {
                                val stub = declaration.stub
                            }
                            else -> {
                            }
                        }

                    }
                })
        }
    }

//    private fun makeElementNode(declaration: PsiElement, sharedType: SharedType): TreeNode? {
//        return when (declaration) {
//            is KtAnnotation -> registerAnnotation(declaration, sharedType)
//            is KtClass -> registerClass(declaration, sharedType)
//            is KtNamedFunction -> registerNamedFunction(declaration, sharedType)
//            is KtProperty -> registerProperty(declaration, sharedType)
//            is KtObjectDeclaration -> registerObject(declaration, sharedType)
//            is KtTypeAlias -> {
//                val stub = declaration.stub
//                null
//            }
//            else -> null
//        }
//    }


    private fun registerAnnotation(annotation: KtAnnotation, sharedType: SharedType): OldSharedElementNode {
        val stub = annotation.stub
        val model = SharedElementModel(annotation.name, DeclarationType.ANNOTATION, stub)
        return OldSharedElementNode(model, null)
    }

    private fun registerProperty(property: KtProperty, sharedType: SharedType): OldSharedElementNode {
        val stub = property.stub
        val actuals = property.actualsForExpected()
        val model = SharedElementModel(property.name, DeclarationType.PROPERTY, stub)
        return OldSharedElementNode(model, null)
    }

    private fun registerNamedFunction(function: KtNamedFunction, sharedType: SharedType): OldSharedElementNode {
        val stub = function.stub
        val model = SharedElementModel(function.name, DeclarationType.NAMED_FUNCTION, stub)
        return OldSharedElementNode(model, null)
    }

    private suspend fun registerClass(classDeclaration: KtClass, sharedType: SharedType): OldSharedElementNode {
        val stub = classDeclaration.stub
        val model = SharedElementModel(classDeclaration.name, DeclarationType.CLASS, stub)

        val node = OldSharedElementNode(model, null)

        val nested = classDeclaration.body?.children

        runBlocking {
            if (nested != null) {
                val childrenFlow = registerNestedDeclaration(nested, sharedType)
                childrenFlow.collect {
                    node.addChildNode(it)
                }
            }
        }

        return node
    }

    private suspend fun registerObject(
        objectDeclaration: KtObjectDeclaration,
        sharedType: SharedType
    ): OldSharedElementNode {
        val stub = objectDeclaration.stub

        val model = SharedElementModel(objectDeclaration.name, DeclarationType.OBJECT, stub)
        val node = OldSharedElementNode(model, null)

        val nested = objectDeclaration.body?.children

        if (nested != null) {
            val childrenFlow = registerNestedDeclaration(nested, sharedType)
            childrenFlow.collect {
                node.addChildNode(it)
            }
        }


        return node
    }


    ////////////////////// Garbage


    private fun retrieveStubs(project: Project) {
        DumbServiceImpl.getInstance(project).smartInvokeLater {


            val mppAuthorityZones = MppAuthorityManager().provideAuthorityZonesForProject(project)

            mppAuthorityZones.forEach { authorityZone ->
                val commonModule = authorityZone.commonModule
                println("Authority zone root name: ${commonModule.name}")
                val commonModulePath = commonModule.moduleFilePath
                val commonModuleVF = commonModule.moduleFile
                val commonModuleScope = commonModule.moduleScope

                val sourceRoots = commonModule.sourceRoots

                sourceRoots.forEach { vf ->
                    VfsUtilCore.iterateChildrenRecursively(vf, null, { virtualFile ->
                        val path = virtualFile.path
                        val psiFile = PsiManager.getInstance(project).findFile(virtualFile)

                        psiFile?.acceptChildren(object : KtVisitorVoid() {
                            override fun visitNamedDeclaration(declaration: KtNamedDeclaration) {
                                println("visit ${declaration.fqName} in $path")

                                val expected = declaration.isExpectDeclaration()
                                if (!expected) {
                                    return
                                }

                                val actuals = declaration.actualsForExpected()

                                actuals.forEach {
                                    println("____ actuals ${it.getKotlinFqName()}")
                                }

                                val declarationType: DeclarationType =
                                    when (declaration) {
                                        is KtAnnotation -> {
                                            val stub = declaration.stub
                                            val psi = stub?.psi
                                            val a = declaration.actualsForExpected()
                                            DeclarationType.ANNOTATION
                                        }
                                        is KtClass -> {
                                            DeclarationType.CLASS
                                        }
                                        is KtNamedFunction -> {
                                            val stub = declaration.stub
                                            val d = stub?.isTopLevel()

                                            DeclarationType.NAMED_FUNCTION
                                        }
                                        is KtProperty -> {
                                            val stub = declaration.stub
                                            DeclarationType.PROPERTY
                                        }
                                        is KtObjectDeclaration -> {
                                            val stub = declaration.stub
                                            DeclarationType.OBJECT
                                        }
                                        is KtTypeAlias -> {
                                            val stub = declaration.stub
                                            DeclarationType.CLASS
                                        }
                                        else -> return//DeclarationType.UNRESOLVED
                                    }
                                super.visitNamedDeclaration(declaration)
                            }


                        })

                        true
                    })
                }
                println("done")

            }
        }

    }

}


/*
*
            val lightElement: PsiElement? = when (declaration) {
                is KtClassOrObject -> declaration.toLightClass()
                is KtNamedFunction, is KtSecondaryConstructor -> LightClassUtil.getLightClassMethod(declaration as KtFunction)
                is KtProperty, is KtParameter -> {
                    if (declaration is KtParameter && !declaration.hasValOrVar()) return false
                    // can't rely on light element, check annotation ourselves
                    val entryPointsManager = EntryPointsManager.getInstance(declaration.project) as EntryPointsManagerBase
                    return checkAnnotatedUsingPatterns(
                        declaration,
                        entryPointsManager.additionalAnnotations + entryPointsManager.ADDITIONAL_ANNOTATIONS
                    )
                }
                else -> return false
            }*/