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
import abyss.modulesRoutines.MppAuthorityManager
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.DumbServiceImpl
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VfsUtilCore
import com.intellij.psi.PsiManager
import org.jetbrains.kotlin.idea.refactoring.fqName.getKotlinFqName
import org.jetbrains.kotlin.idea.util.actualsForExpected
import org.jetbrains.kotlin.idea.util.isExpectDeclaration
import org.jetbrains.kotlin.idea.util.sourceRoots
import org.jetbrains.kotlin.psi.*

class StubVisitorAction : AnAction() {

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project
        if (project != null) {
            retrieveStubs(project)
        }
    }

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