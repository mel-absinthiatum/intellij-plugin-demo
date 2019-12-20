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
import abyss.model.tree.nodes.ExpectOrActualModel
import abyss.model.tree.nodes.OldExpectOrActualNode
import abyss.modulesRoutines.MppAuthorityManager
import abyss.modulesRoutines.MppAuthorityZone
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.DumbService
import com.intellij.openapi.project.DumbServiceImpl
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VfsUtilCore
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiManager
import com.intellij.psi.stubs.StubIndex
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.jetbrains.kotlin.idea.refactoring.fqName.getKotlinFqName
import org.jetbrains.kotlin.idea.stubindex.KotlinFullClassNameIndex
import org.jetbrains.kotlin.idea.util.actualsForExpected
import org.jetbrains.kotlin.idea.util.isExpectDeclaration
import org.jetbrains.kotlin.idea.util.sourceRoots
import org.jetbrains.kotlin.psi.*
import javax.swing.tree.TreeNode
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine


class StubVisitorActionBackup : AnAction() {

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project
        if (project != null) {
            println("action")
            val dumbService = DumbServiceImpl.getInstance(project)
//            dumbService.smartInvokeLater {

            runBlocking {
                launch {

                    println("launch1")

                    iterateAllZones(project)

                    println("launch2")

                }
                println("launched")
            }
//            }
            println("block off")
        }
    }

    private fun registerTree(project: Project) {
        val mppAuthorityZones = MppAuthorityManager().provideAuthorityZonesForProject(project)

        mppAuthorityZones.forEach { authorityZone ->
            val commonModule = authorityZone.commonModule
            val sourceRoots = commonModule.sourceRoots
            runBlocking() {
                sourceRoots.forEach { vf ->
                    VfsUtilCore.iterateChildrenRecursively(vf, null, { virtualFile ->
                        val path = virtualFile.path
                        println("path $path")
                        val psiFile = PsiManager.getInstance(project).findFile(virtualFile)
                        println("before launch")
                        launch() {

                            println(" launch")
                            if (psiFile != null) {
                                registerDeclaration(psiFile, SharedType.EXPECTED, DumbServiceImpl.getInstance(project))
                            }
                            println("registered")

                            println("after launch")
                        }

                        true
                    })


                }
            }
        }
    }

    private suspend fun iterateAllZones(project: Project) {
        val mppAuthorityZones = MppAuthorityManager().provideAuthorityZonesForProject(project)
        mppAuthorityZones.forEach { authorityZone ->
            val flow = iterateTree(authorityZone, project)
            flow.collect {
                if (it is OldExpectOrActualNode) {
                    println("__ collected ${it.model.stub}")
                }
                println("collected: $it")
            }
        }
    }

    private suspend fun iterateTree1(authorityZone: MppAuthorityZone, project: Project): Flow<TreeNode?> {
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
            registerDeclaration(psiFile, SharedType.EXPECTED, dumbService)
        }.toTypedArray()
        val flattenFlows = flowOf(*flows).flattenMerge()
        return flattenFlows
    }


    @UseExperimental(FlowPreview::class)
    suspend fun iterateTree(authorityZone: MppAuthorityZone, project: Project): Flow<TreeNode?> {
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
            waitInvocation(psiFile, SharedType.EXPECTED, dumbService)
        }.toTypedArray()

        val flattenFlows = flowOf(*flows).flattenMerge()

        return flattenFlows
    }

    private fun indexes(project: Project) {
        val key = KotlinFullClassNameIndex.getInstance().key
        val valNames = StubIndex.getInstance().getAllKeys(key, project)
        valNames.forEach { println("index $it") }
    }

    private suspend fun registerDeclaration1(element: PsiFile, sharedType: SharedType, dumbService: DumbService): TreeNode? =
        suspendCoroutine { cont ->
            println("register declaration method")
            dumbService.smartInvokeLater {
                element.acceptChildren(
                    namedDeclarationVisitor { declaration ->
                        when (declaration) {
                            is KtAnnotation -> {
                                cont.resume(registerAnnotation(declaration, sharedType))
                            }
                            is KtClass -> {
                                cont.resume(registerClass(declaration, sharedType))
                            }
                            is KtNamedFunction -> {
                                cont.resume(registerNamedFunction(declaration, sharedType))
                            }
                            is KtProperty -> {
                                cont.resume(registerProperty(declaration, sharedType))
                            }
                            is KtObjectDeclaration -> {
                                cont.resume(registerObject(declaration, sharedType))
                            }
                            is KtTypeAlias -> {
                                val stub = declaration.stub
                                DeclarationType.CLASS
                                cont.resume(null)

                            }
                            else -> cont.resume(null)
                        }
                    }
                )
            }
        }

    private suspend fun waitInvocation(
        element: PsiFile,
        sharedType: SharedType,
        dumbService: DumbService
    ): Flow<TreeNode?> = suspendCoroutine { cont ->
        dumbService.smartInvokeLater {
            //            launch {
            var d:Flow<TreeNode?>? = null
            runBlocking {
                launch {
                    d = registerDeclaration(element, sharedType, dumbService)
                }

            }
            if (d != null) {
                cont.resume(d!!)

            }
//            }
        }
    }




    private suspend fun registerDeclaration2(
        element: PsiFile,
        sharedType: SharedType,
        dumbService: DumbService
    ): Flow<TreeNode?> = channelFlow {
        println("register declaration method")
// TODO: Use Dumb service
        dumbService.smartInvokeLater {
            //                    launch {

            element.acceptChildren(
                namedDeclarationVisitor { declaration ->
                    runBlocking {

                        //                                launch {
                        println("register declaration method launch")

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
//                                }
                    }
                })

        }

//        }
    }
    private suspend fun registerDeclaration(
        element: PsiFile,
        sharedType: SharedType,
        dumbService: DumbService
    ): Flow<TreeNode?> = channelFlow {
        println("register declaration method")

        element.acceptChildren(
            namedDeclarationVisitor { declaration ->
                launch {
                    println("register declaration method launch")

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
            }

        )

    }

//    private suspend fun registerItem(declaration:)


    private fun registerAnnotation(annotation: KtAnnotation, sharedType: SharedType): TreeNode {
        val stub = annotation.stub
        val model = ExpectOrActualModel(annotation, sharedType, stub)
        println(annotation.name)
        return OldExpectOrActualNode(model, null)
    }

    private fun registerProperty(property: KtProperty, sharedType: SharedType): TreeNode {
        val stub = property.stub
        val model = ExpectOrActualModel(property, sharedType, stub)
        println(property.name)

        return OldExpectOrActualNode(model, null)
    }

    private fun registerNamedFunction(function: KtNamedFunction, sharedType: SharedType): TreeNode {
        val stub = function.stub
        val model = ExpectOrActualModel(function, sharedType, stub)
        return  OldExpectOrActualNode(model, null)
    }

    private fun registerClass(classDeclaration: KtClass, sharedType: SharedType): TreeNode {
        val stub = classDeclaration.stub

        val model = ExpectOrActualModel(classDeclaration, sharedType, stub)
        println(classDeclaration.name)

        return  OldExpectOrActualNode(model, null)

    }

    private fun registerObject(objectDeclaration: KtObjectDeclaration, sharedType: SharedType): TreeNode {
        val stub = objectDeclaration.stub

        val model = ExpectOrActualModel(objectDeclaration, sharedType, stub)
        return OldExpectOrActualNode(model, null)

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