package abyss.view.toolWindow

import abyss.extensionPoints.SharedElementsTopics
import abyss.extensionPoints.SharedElementsTopicsNotifier
import abyss.imageManager.CustomIcons
import abyss.model.tree.nodes.ExpectOrActualNode
import abyss.model.tree.nodes.MppAuthorityZoneNode
import abyss.model.tree.nodes.RootNode
import abyss.view.AbyssTreeCellRenderer
import com.intellij.codeInsight.highlighting.HighlightManager
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.editor.ScrollType
import com.intellij.openapi.editor.colors.EditorColors
import com.intellij.openapi.editor.colors.EditorColorsManager
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.ui.AnActionButton
import com.intellij.ui.ToolbarDecorator
import com.intellij.ui.treeStructure.Tree
import com.intellij.util.messages.MessageBus
import org.jetbrains.kotlin.psi.psiUtil.endOffset
import org.jetbrains.kotlin.psi.psiUtil.startOffset
import javax.swing.JPanel
import javax.swing.JTree
import javax.swing.tree.DefaultTreeModel
import javax.swing.tree.TreeSelectionModel


class MppToolWindow(private val project: Project, private val toolWindow: ToolWindow) {

    var sharedElementsTree = Tree(RootNode())
    var content: JPanel

    init {
        val decorator = decorator(sharedElementsTree)
        val panel = decorator.createPanel()
        content = panel

        subscribe(project.messageBus)

        configureSharedElementsTree()

        // TODO
//        SharedElementsUpdatesManager().launchUpdatesTimer(project, 5000)
    }

    private fun decorator(tree: Tree): ToolbarDecorator {
        val refreshActionButton = AnActionButton.fromAction(ActionManager.getInstance().getAction("RefreshTree"))
        refreshActionButton.templatePresentation.icon = CustomIcons.Actions.Refresh
        // refreshActionButton.setShortcut(CommonActionsPanel.getCommonShortcut(CommonActionsPanel.Buttons.ALL))

        return ToolbarDecorator.createDecorator(tree)
            .initPosition()
            .disableAddAction().disableRemoveAction().disableDownAction().disableUpAction()
            .addExtraAction(refreshActionButton)
    }

    private fun subscribe(bus: MessageBus) {
        val bus = project.messageBus
        bus.connect().subscribe(SharedElementsTopics.SHARED_ELEMENTS_TREE_TOPIC, object : SharedElementsTopicsNotifier {
            override fun sharedElementsUpdated(nodes: List<MppAuthorityZoneNode>) {
                val treeModel = sharedElementsTree.model as DefaultTreeModel
                val rootNode = treeModel.root as RootNode
                rootNode.remove(rootNode.children)
                rootNode.add(nodes)
                treeModel.reload(rootNode)

            }
        })
    }

    private fun configureSharedElementsTree() {
        sharedElementsTree.isRootVisible = false
        sharedElementsTree.selectionModel.selectionMode = TreeSelectionModel.SINGLE_TREE_SELECTION
        sharedElementsTree.cellRenderer = AbyssTreeCellRenderer(sharedElementsTree)
        addTreeListener()
    }

    private fun addTreeListener() {
        sharedElementsTree.addTreeSelectionListener { event ->
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

                    val caretModel = currentEditor.caretModel
                    caretModel.primaryCaret.moveToOffset(psi.startOffset)
                    val scrollingModel = currentEditor.scrollingModel

                    scrollingModel.scrollToCaret(ScrollType.CENTER_UP)
                }
            }
        }
    }
}

//com.intellij.codeInsight.highlighting.HighlightUsagesHandlerBase
//HighlightManager#addOccurrenceHighlights(Editor, PsiElement[], TextAttributes, boolean, ArrayList]]>)