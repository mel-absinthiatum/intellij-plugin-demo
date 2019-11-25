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
        printLanguages()
        parseTree(e)
    }

    fun parseTree(e: AnActionEvent) {
        val eventProject = e.project

        val rootManager = ProjectRootManager.getInstance(eventProject!!)

        val vFiles = rootManager.contentSourceRoots

        for (file in vFiles) {
            println("\nRoot: ${file.url}")
            VfsUtilCore.iterateChildrenRecursively(file, {
                true
            }, {
                val doc = FileDocumentManager.getInstance().getDocument(it)
                val text = doc?.charsSequence
                val psiFile = PsiManager.getInstance(eventProject).findFile(it)

                if (text != null) {
                    val ktFile = parse(it.name, text)
                    println()
                    println()

//                    if (ktFile != null) {
                        // TODO
//                        val importList = ktFile.importList
//                        val classes = ktFile.classes
//                        classes.forEach {
//                            println("class: ${it.name}")
//                            val fields = it.allFields
//
//
//                            fields.forEach {
//                                println("field: ${it.toString()} **")
//                            }
//                        }

//                    }
                }
                true
            })
        }
        println("done.")
    }

    private fun parse(source: String, code: CharSequence): KtFile? {
        val ast = parsePsiFile(source, code).also { file ->
            file?.collectDescendantsOfType<PsiErrorElement>()
        }
        if (ast != null) {
            println("Kotlin class: $ast")
            ast.declarations.forEach { node ->
                when (node) {
                    is KtNamedFunction -> node.parse()
                    is KtProperty -> node.parse()
                    is KtClass -> node.parse()
//                    is KtNamedDeclaration -> node.parse()
                    is KtObjectDeclaration -> node.parse()
                    is KtParameter -> println("parameter")

                    else -> println("Unknown node: $node")
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

    private fun printLanguages() {
        val languages = getRegisteredLanguages()
        println("languages count: ${languages.size}")
        languages.forEach {
            println("language ### $it")
            if (it.isKindOf("kotlin")) {
                println("%%% kotlin")
            }
        }
    }

    private fun visit() {
        // TODO: Explore KtVisitor
//        val visitor = KtVisitor
        val visitor = KtVisitorVoid()
    }


    private fun KtProperty.parse() {
        println("property ${this.name}")
        println("-- accessors ${this.accessors}\n")

        // Modifiers.
        val expectKW = KtTokens.EXPECT_KEYWORD
        val exp = this.modifierList?.getModifier(expectKW)
        if (exp != null) {
            println("---- Expected (property)")
        }

        // Iterate modifiers.
        val ml = this.modifierList
        if (ml != null) {
            println("-- annotations: ${ml.annotations}")

            ml.forEachDescendantOfType<PsiElement> {
                println("-- modifier psi: $it name: ${it.text}")
            }
        }

        // References.
        val receiverTypeReference = this.receiverTypeReference
        val typeReference = this.typeReference
        val colon = this.colon
    }

    private fun KtClass.parse() {
        println("class ${this.name}")

        val exp = this.modifierList?.getModifier(KtTokens.EXPECT_KEYWORD)
        val pub = this.modifierList?.getModifier(KtTokens.PUBLIC_KEYWORD)
        if (exp != null) {
            println("---- Expected (class)")
        }
        val properties = this.getProperties()
        properties.forEach {
            it.parse()
        }

        val methods = this.declarations.filterIsInstance<KtNamedFunction>()
        methods.map {
            it.parse()
        }
    }

    private fun KtNamedFunction.parse() {
        println("function: ${this.name}")

        // Parameters.
        val parameters = this.valueParameters
        for (parameter in parameters) {
            if (parameter != null) {
                println("-- parameter: ${parameter.name}")
            }
        }

        // Modifiers.
        val ml = this.modifierList
        val expectKW = KtTokens.EXPECT_KEYWORD
        val exp = ml?.getModifier(expectKW)
        if (exp != null) {
            println("---- Expected (function)")
        }
    }

    private fun KtNamedDeclaration.parse() {
        println("named declaration $this")
        // Modifiers.
        val ml = this.modifierList
        val expectKW = KtTokens.EXPECT_KEYWORD
        val exp = ml?.getModifier(expectKW)
        if (exp != null) {
            println("---- Expected (?)")
        }
    }

    private fun KtObjectDeclaration.parse() {
        println("object declaration $this")
        // Modifiers.
        val ml = this.modifierList
        val expectKW = KtTokens.EXPECT_KEYWORD
        val exp = ml?.getModifier(expectKW)
        if (exp != null) {
            println("---- Expected (object declaration)")
        }
    }
}
