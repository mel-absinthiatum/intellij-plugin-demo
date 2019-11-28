package actions.psi

/**
* UNUSED.
* */

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


class KotlinPsiListAction : AnAction() {

    private var kotlinLanguage: Language? = null

    override fun actionPerformed(e: AnActionEvent) {
        printLanguages()
        printExpectedElementsByKtAnalysis(e)
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


    private fun printExpectedElementsByKtAnalysis(e: AnActionEvent) {
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

//                TODO
//                val refSearch = ReferencesSearch.search(this)
//                val importList = ktFile.importList

                true
            })
        }
    }

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
        // TODO: explore difference between various visitors
        // val visitor = namedDeclarationVisitor { declaredName ->
        //     println("Declaration name ${declaredName.name}")
        // }
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
