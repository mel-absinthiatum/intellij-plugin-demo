package actions.psi

//import org.jetbrains.kotlin.cli.jvm.compiler.EnvironmentConfigFiles
//import org.jetbrains.kotlin.cli.jvm.compiler.KotlinCoreEnvironment
//import org.jetbrains.kotlin.openapi.util.Disposer

import com.intellij.lang.Language
import com.intellij.lang.Language.getRegisteredLanguages
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.openapi.vfs.VfsUtilCore
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiErrorElement
import com.intellij.psi.PsiManager
import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.psi.*
import org.jetbrains.kotlin.psi.psiUtil.collectDescendantsOfType
import org.jetbrains.kotlin.psi.psiUtil.forEachDescendantOfType

//import org.jetbrains.kotlin.com.intellij.openapi.project.Project as KProject
//import org.jetbrains.kotlin.com.intellij.psi.PsiManager as KPsiManager


//import com.intellij.lang.*

class KotlinPsiListAction : AnAction() {

    private var kotlinLanguage: Language? = null

    override fun actionPerformed(e: AnActionEvent) {
        printLanguages()
        printExpendedElementsByKtAnalysis(e)
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
                if (kotlinLanguage != null) {
                    val psiMan = PsiManager.getInstance(eventProject)
                    val psi = psiMan.findFile(it)

                    if (psi != null) {
                        val viewProvider = psi.viewProvider
                        val psi = viewProvider.getPsi(kotlinLanguage!!)


                    }

                }


                val doc = FileDocumentManager.getInstance().getDocument(it)
                val text = doc?.charsSequence
                val psiFile = PsiManager.getInstance(eventProject).findFile(it)

                true
            })
        }
        println("done.")
    }


    private fun printExpendedElementsByKtAnalysis(e: AnActionEvent) {
        val eventProject = e.project

        val rootManager = ProjectRootManager.getInstance(eventProject!!)

        val vFiles = rootManager.contentSourceRoots

        for (file in vFiles) {
            println("\nRoot: ${file.url}")
            VfsUtilCore.iterateChildrenRecursively(file, {
                true
            }, {
                val psiF = PsiManager.getInstance(eventProject).findFile(it)
                val ast = psiF?.collectDescendantsOfType<PsiErrorElement>()
                println("file type: ${psiF?.fileType?.name}")

                if (psiF != null && psiF.fileType.name == "Kotlin") {
//                    val visitor = namedDeclarationVisitor { declaredName ->
//                        println("Declaration name ${declaredName.name}")
//                    }
                    println("fcgfc ${psiF.name}")
                    psiF.acceptChildren(object: KtTreeVisitorVoid() {
                        override fun visitNamedDeclaration(declaration: KtNamedDeclaration) {
                            declaration.name?.let { declaredName ->
                                println("Declaration name $declaredName")
                                super.visitNamedDeclaration(declaration)
                            }
                        }
                    })

                    println()
                    println()
                }

//                    // TODO
////                        val importList = ktFile.importList

                true
            })
        }
    }
            // Shenmue I game!! Akira game

//    private fun parse(source: String, code: CharSequence): KtFile? {
//        val ast = parsePsiFile(source, code).also { file ->
//            file?.collectDescendantsOfType<PsiErrorElement>()
//        }
//        if (ast != null) {
//            println("Kotlin class: $ast")
//            ast.declarations.forEach { node ->
//                when (node) {
//                    is KtNamedFunction -> node.parse()
//                    is KtProperty -> node.parse()
//                    is KtClass -> node.parse()
//                    is KtNamedDeclaration -> node.parse()
//                    is KtObjectDeclaration -> node.parse()
//                    is KtParameter -> println("parameter")
//
//                    else -> println("Unknown node: $node")
//                }
//            }
//        }
//        return ast
//    }

//    private fun parsePsiFile(name: String, code: CharSequence): KtFile? {
//        val project = project()
//        return KPsiManager.getInstance(project)
//            .findFile(LightVirtualFile(name, KotlinFileType.INSTANCE, code)) as KtFile?
//    }
//
//    private fun project(): KProject {
//        return KotlinCoreEnvironment.createForProduction(
//            Disposer.newDisposable(),
//            CompilerConfiguration(),
//            EnvironmentConfigFiles.JVM_CONFIG_FILES
//        ).project
//    }

    private fun printLanguages() {
        val languages = getRegisteredLanguages()
        println("languages count: ${languages.size}")
        languages.forEach {
            println("language ### $it")
            if (it.isKindOf("kotlin")) {
                kotlinLanguage = it
                println("%%% kotlin")
            }
        }
    }

    private fun visit(file: KtFile) {
        // TODO: Explore KtVisitor
//        val visitor = KtVisitor
        val visitor = KtVisitorVoid()
        val v = KtTreeVisitor<PsiElement>()


        file.accept(object : KtTreeVisitor<PsiElement>() {
            override fun visitKtElement(element: KtElement, data: PsiElement?): Void? {
                super.visitKtElement(element, data)

                System.out.println("Found a variable at offset " + element.getTextRange().getStartOffset())
                return null
            }

        })

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

        val references = this.references
        references.forEach {
            // Refers to nothing as it should be
            println("refer: ${it.resolve()}")
        }

        if (receiverTypeReference != null) {
            // Null
            println("receiver type reference: ${receiverTypeReference.typeElement}")
        }

        if (typeReference != null) {
            // Prints USER_TYPE
            println("type reference: $typeReference , element: ${typeReference.typeElement}")
        }


//        val refSearch = ReferencesSearch.search(this)
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



// TODO: - Tips:

//        val project = e.getRequiredData<Project>(CommonDataKeys.PROJECT) as Project?
//        val project = e.project as Project?
//import org.jetbrains.kotlin.builder.KotlinPsiManager

//kotlin/compiler/psi/src/org/jetbrains/kotlin/psi/
//import org.jetbrains.kotlin.psi.KtVisitor
//import javax.tools.FileObject
//import com.intellij.openapi.project.Project

//import com.intellij.psi.util.PsiTreeUtil
//import com.intellij.openapi.vfs.VirtualFile
//import org.jetbrains.kotlin.com.intellij.openapi.roots.impl.PackageDirectoryCache
//import org.jetbrains.kotlin.com.intellij.openapi.vfs.*
//import org.jetbrains.kotlin.com.intellij.openapi.vfs.VirtualFile as KVirtualFile
//import org.jetbrains.kotlin.psi.KtFunction
//import com.intellij.openapi.actionSystem.CommonDataKeys

//fun getKtFile(file: FileObject): KtFile {
//    return KotlinPsiManager.INSTANCE.getParsedFile(file)
//}

//                    val psiFile = PsiManager.getInstance(project).findFile(it) as? KtFile
//                    val kotlinLang = Language.findLanguageByID("kotlin")