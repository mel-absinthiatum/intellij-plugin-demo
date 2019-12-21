package abyss.toolWindow

import abyss.extensionPoints.SharedElementsTopics
import abyss.extensionPoints.SharedElementsTopicsNotifier
import abyss.model.tree.nodes.ExpectOrActualNode
import abyss.view.AbyssTreeCellRenderer
import com.intellij.codeInsight.highlighting.HighlightManager
import com.intellij.openapi.editor.ScrollType
import com.intellij.openapi.editor.colors.EditorColors
import com.intellij.openapi.editor.colors.EditorColorsManager
import com.intellij.openapi.editor.event.EditorMouseEvent
import com.intellij.openapi.editor.event.EditorMouseListener
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.treeStructure.Tree
import com.intellij.util.messages.MessageBus
import org.jetbrains.kotlin.psi.psiUtil.endOffset
import org.jetbrains.kotlin.psi.psiUtil.startOffset
import javax.swing.*
import javax.swing.tree.DefaultTreeCellRenderer
import javax.swing.tree.TreeCellRenderer
import javax.swing.tree.TreeSelectionModel


class MppToolWindow(private val project: Project, private val toolWindow: ToolWindow) {


    var content: JPanel

    init {
        val panel = JPanel()
        panel.layout = BoxLayout(panel, BoxLayout.LINE_AXIS)
        subscribe(project.messageBus)

        content = panel
    }

    private fun subscribe(bus: MessageBus) {
        val bus = project.messageBus
        bus.connect().subscribe(SharedElementsTopics.SHARED_ELEMENTS_TREE_TOPIC, object : SharedElementsTopicsNotifier {
            override fun sharedElementsTreeUpdated(tree: Tree) {
                tree.run {
                    isRootVisible = false
                    selectionModel.selectionMode = TreeSelectionModel.SINGLE_TREE_SELECTION
                    cellRenderer = AbyssTreeCellRenderer(this)


                    addTreeSelectionListener { event ->
                        val source = event.source as JTree
                        val node = source.lastSelectedPathComponent as? ExpectOrActualNode
                        source.clearSelection()
                        val psi = node?.model?.psi
                        val file = psi?.containingFile?.virtualFile
                        if (psi != null && file != null && file.isValid && !file.isDirectory) {
                            val fileEditorManager = FileEditorManager.getInstance(project)
                            val editors = fileEditorManager.openFile(file, true)
                            val currentEditor = FileEditorManager.getInstance(project).selectedTextEditor
                            if (currentEditor != null) {
                                val attributes = EditorColorsManager.getInstance().globalScheme.getAttributes(
                                    EditorColors.SEARCH_RESULT_ATTRIBUTES
                                )

                                HighlightManager.getInstance(project).addOccurrenceHighlight(
                                    currentEditor,
                                    psi.startOffset,
                                    psi.endOffset,
                                    attributes,
                                    HighlightManager.HIDE_BY_ANY_KEY,
                                    null,
                                    null
                                )
                                val listener = currentEditor.addEditorMouseListener(object: EditorMouseListener {
                                    override fun mouseClicked(event: EditorMouseEvent) {
                                        println("mouse event")

                                        currentEditor.removeEditorMouseListener(this)
                                    }
                                })

                                val caretModel = currentEditor.caretModel
                                caretModel.primaryCaret.moveToOffset(psi.startOffset)
                                val scrollingModel = currentEditor.scrollingModel

                                scrollingModel.scrollToCaret(ScrollType.CENTER_UP)
                            }
                        }
                    }
                }

                val scrollPane = JBScrollPane(tree)

                scrollPane.border = BorderFactory.createEmptyBorder()
                content.add(scrollPane)
            }
        })
    }


    private fun makeTreeCellRenderer(): TreeCellRenderer {
        val imageIcon = ImageIcon(javaClass.getResource("/abyss/class.png"))
        val renderer = DefaultTreeCellRenderer()
        renderer.leafIcon = imageIcon
        renderer.borderSelectionColor = null
        renderer.backgroundSelectionColor = null
        return renderer
    }
}

//com.intellij.codeInsight.highlighting.HighlightUsagesHandlerBase
//HighlightManager#addOccurrenceHighlights(Editor, PsiElement[], TextAttributes, boolean, ArrayList]]>)