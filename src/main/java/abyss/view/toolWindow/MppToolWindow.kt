package abyss.view.toolWindow

import abyss.extensionPoints.SharedElementsTopics
import abyss.extensionPoints.SharedElementsTopicsNotifier
import abyss.imageManager.CustomIcons
import abyss.model.tree.diff.Insert
import abyss.model.tree.diff.Remove
import abyss.model.tree.diff.TreeMutation
import abyss.model.tree.diff.TreesDiffManager
import abyss.model.tree.nodes.CustomNodeInterface
import abyss.model.tree.nodes.ExpectOrActualNode
import abyss.model.tree.nodes.RootNode
import abyss.model.tree.nodes.TemplateNodeInterface
import abyss.treeUpdateManager.SharedElementsUpdatesManager
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
import java.util.*
import javax.swing.JPanel
import javax.swing.JTree
import javax.swing.tree.DefaultMutableTreeNode
import javax.swing.tree.DefaultTreeModel
import javax.swing.tree.TreePath
import javax.swing.tree.TreeSelectionModel


class MppToolWindow(private val project: Project, private val toolWindow: ToolWindow) {

    private val sharedElementsTree: Tree
    private val treeModel: DefaultTreeModel
    private var treeRoot: RootNode = RootNode()
    var content: JPanel

    init {
        treeModel = DefaultTreeModel(treeRoot)
        sharedElementsTree = Tree(treeModel)

        val decorator = decorator(sharedElementsTree)
        val panel = decorator.createPanel()
        content = panel

        subscribe(project.messageBus)

        configureSharedElementsTree()

        // TODO
        SharedElementsUpdatesManager().launchUpdatesTimer(project, 5000)
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
            override fun sharedElementsUpdated(root: RootNode) {
                val diffTree = TreesDiffManager().makeMutationsTree(treeRoot, root) ?: return
                updateTree(treeRoot, diffTree)
                reloadTree()
            }
        })
    }

    private fun updateTree(sourceNode: CustomNodeInterface, diffNode: DefaultMutableTreeNode) {
        val diffNodeModel = diffNode.userObject as? TreesDiffManager.DiffNodeModel<*> ?: return
        if (sourceNode.nodeModel() != diffNodeModel.sourceNodeModel) {
            println("sourceNodeModel type cast error")
            return
        }

        val mutatedChildren = diffNode.children().toList()
        mutatedChildren.forEach {
            val mutatedChildNode = it as? DefaultMutableTreeNode ?: return@forEach
            val mutatedChildModel = mutatedChildNode.userObject as? TreesDiffManager.DiffNodeModel<*> ?: return@forEach
            for (sourceChild in sourceNode.childNodes()) {
                if (sourceChild.nodeModel() == mutatedChildModel.sourceNodeModel) {
                    updateTree(sourceChild, mutatedChildNode)
                    break
                }
                println("Matching error")
            }
        }

        val mutations = diffNodeModel.mutations
        mutations.forEach { handleMutation(sourceNode, it) }
    }

    private fun handleMutation(node: CustomNodeInterface, mutation: TreeMutation) {
        val targetNode = node as? TemplateNodeInterface<*, *, *> ?: return
        when (mutation) {
            is Insert -> {
                targetNode.insert(mutation.node, targetNode.childCount)
            }
            is Remove -> {
                targetNode.remove(mutation.node)
            }
        }
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

    fun reloadTree() {
        val expanded = ArrayList<TreePath>()
        for (i in 0 until sharedElementsTree.rowCount - 1) {
            val currPath = sharedElementsTree.getPathForRow(i)
            val nextPath = sharedElementsTree.getPathForRow(i + 1)
            if (currPath.isDescendant(nextPath)) {
                expanded.add(currPath)
            }
        }
        treeModel.reload()
        for (path in expanded) {
            sharedElementsTree.expandPath(path)
        }
    }
}

/*
//create a class which implements the MouseListener interface and
//implement the following in your overridden mouseClicked method

@Override
public void mouseClicked(MouseEvent e) {

    if (SwingUtilities.isRightMouseButton(e)) {

        int row = tree.getClosestRowForLocation(e.getX(), e.getY());
        tree.setSelectionRow(row);
        popupMenu.show(e.getComponent(), e.getX(), e.getY());
    }
}


 // If you are interested in detecting either double-click events or when a user clicks on a node, regardless of whether or not it was selected, we recommend you do the following:

 final JTree tree = ...;

 MouseListener ml = new MouseAdapter() {
     public void mousePressed(MouseEvent e) {
         int selRow = tree.getRowForLocation(e.getX(), e.getY());
         TreePath selPath = tree.getPathForLocation(e.getX(), e.getY());
         if(selRow != -1) {
             if(e.getClickCount() == 1) {
                 mySingleClick(selRow, selPath);
             }
             else if(e.getClickCount() == 2) {
                 myDoubleClick(selRow, selPath);
             }
         }
     }
 };
 tree.addMouseListener(ml);
*/

//com.intellij.codeInsight.highlighting.HighlightUsagesHandlerBase
//HighlightManager#addOccurrenceHighlights(Editor, PsiElement[], TextAttributes, boolean, ArrayList]]>)