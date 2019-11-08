package actions.psi


import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.openapi.vfs.VfsUtilCore
import com.intellij.psi.PsiManager
import org.jetbrains.kotlin.cli.jvm.compiler.EnvironmentConfigFiles
import org.jetbrains.kotlin.cli.jvm.compiler.KotlinCoreEnvironment
import org.jetbrains.kotlin.com.intellij.lang.Language.getRegisteredLanguages
import org.jetbrains.kotlin.com.intellij.openapi.util.Disposer
import org.jetbrains.kotlin.com.intellij.psi.PsiElement
import org.jetbrains.kotlin.com.intellij.psi.PsiErrorElement
import org.jetbrains.kotlin.com.intellij.testFramework.LightVirtualFile
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.idea.KotlinFileType
import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.psi.*
import org.jetbrains.kotlin.psi.psiUtil.collectDescendantsOfType
import org.jetbrains.kotlin.psi.psiUtil.forEachDescendantOfType
import org.jetbrains.kotlin.com.intellij.openapi.project.Project as KProject
import org.jetbrains.kotlin.com.intellij.psi.PsiManager as KPsiManager

//import com.intellij.lang.*

class KotlinPsiListAction : AnAction() {

    override fun actionPerformed(e: AnActionEvent) {
        lang()
        val eventProject = e.project

        val rootManager = ProjectRootManager.getInstance(eventProject!!)

        val vFiles = rootManager.contentRoots

        for (file in vFiles) {
            println("\nRoot: ${file.url}")
            VfsUtilCore.iterateChildrenRecursively(file, {
                true
            }, {
                val doc = FileDocumentManager.getInstance().getDocument(it)
                val text = doc?.charsSequence
                val psiFile = PsiManager.getInstance(eventProject).findFile(it)
                println("File: $psiFile")

                if (text != null) {
                    val ktFile = parse(it.name, text)

                    if (ktFile != null) {
                        val importList = ktFile.importList
                        val classes = ktFile.classes
                        classes.forEach {
                            println("class: ${it.name}")
                            val fields = it.allFields


                            fields.forEach {
                                println("field: ${it.toString()} **")
                            }
                        }
                        println("File content: ")
                    }
                }
                true
            })
        }
    }

    fun parse(source: String, code: CharSequence): KtFile? {
        val ast = parsePsiFile(source, code).also { file ->
            file?.collectDescendantsOfType<PsiErrorElement>()
        }
        if (ast != null) {
            val nodes = ast.declarations.forEach { node ->
                when (node) {
                    is KtNamedFunction -> println("named fun")
                    is KtParameter -> println("parameter")
                    is KtProperty -> {
                        println("property ${node.name}")
                        val ktProperty = node as KtProperty
                        println("-- accessors ${ktProperty.accessors}\n"
                        + "-- modifiers: ${ktProperty.modifierList}")
                        val expectKW = KtTokens.EXPECT_KEYWORD
                        val exp = node.modifierList?.getModifier(expectKW)
                        println("-- Expected (pr) ${exp ?: "is absent"}")
                    }
                    is KtClass -> {
                        println("class ${node.name}")


                        val expectKW = KtTokens.EXPECT_KEYWORD
                        val exp = node.modifierList?.getModifier(expectKW)
                        println("-- Expected (class) ${exp ?: "is absent"}")

                        val ktClass = node as KtClass
                        val properties = ktClass.getProperties()
                        properties.forEach {
                            println("class property: ${it.name}")
                            println("-- accessors ${it.accessors.joinToString()}\n"
                                    + "-- modifiers: ${it.modifierList}")
                            val mList = it.modifierList
                            if (mList != null) {
                                println(mList.annotations)
                                println(mList.node.elementType)
                                mList.forEachDescendantOfType<PsiElement> {
                                    println("-- element: $it")
                                }
                                val expectKW = KtTokens.EXPECT_KEYWORD
                                val exp = mList.getModifier(expectKW)
                                println("-- Expected ${exp ?: "is absent"}")
                            }

                            val receiverTypeReference = it.receiverTypeReference
                            val typeReference = it.typeReference
                            val colon = it.colon
//                            val a = it as KtProperty

//                            val mods = a.declarations.filterIsInstance<KtNamedFunction>()
// KtVisitor visitProperty
                        }

                        val methods = node.declarations.filterIsInstance<KtNamedFunction>()
                        methods.map {
                            println("function: ${it.name}")

                            val parameters = it.valueParameters
                            for (par in parameters) {
                                if (par != null) {
                                    println("-- parameter: $par")
                                }
                                else {
                                    println("-- no par")
                                }
                            }

                            val ml = it.modifierList

                            if (ml != null) {
//                                ml.forEachDescendantOfType<PsiElement> {
//                                    println(" element: $it")
//                                }
//                                ml.forEachDescendantOfType<?> {}
//                                ml.forEachDescendantOfType<KtModifierListElementType>()
//                                val pub = ml.getModifier().isPublic()
//                                val p = ml.getModifier("public")
                            }
                        }
                    }
                    else -> error("Unable to convert node")
                }
            }
        }
        return ast
    }

    private fun parsePsiFile(name: String, code: CharSequence): KtFile? {
        val project = project()
        return KPsiManager.getInstance(project)
            .findFile(LightVirtualFile(name, KotlinFileType.INSTANCE, code)) as KtFile?
    }

    private fun project(): KProject {
        return KotlinCoreEnvironment.createForProduction(
            Disposer.newDisposable(),
            CompilerConfiguration(),
            EnvironmentConfigFiles.JVM_CONFIG_FILES
        ).project
    }

    private fun lang() {
        val languages = getRegisteredLanguages()
        println("languages count: ${languages.size}")
        languages.forEach {
            println("language ### $it")
        }

    }
}
