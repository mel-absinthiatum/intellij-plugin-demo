package abyss.view


import abyss.model.tree.nodes.TemplateNode
import com.intellij.ui.JBDefaultTreeCellRenderer
import com.intellij.ui.components.JBLabel
import java.awt.Component
import javax.swing.Icon
import javax.swing.JTree

class AbyssTreeCellRenderer(tree: JTree) : JBDefaultTreeCellRenderer(tree) {

    override fun getTreeCellRendererComponent(tree: JTree?, value: Any?, sel: Boolean, expanded: Boolean,
                                              leaf: Boolean, row: Int, hasFocus: Boolean): Component {
        super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus)

        if (value is TemplateNode<*, *, *>) {
            return makeComponent(value.model.getLabelText(), value.model.getIcon())
        }
        // TODO: add expanded
//        when (value) {
//            is RootNode, is MppAuthorityZoneNode, is PackageNode -> { return makeComponent(value.sourceNodeModel.getLabelText, CustomIcons.Nodes.File) }
//            is SharedElementNode -> { return makeComponent(value.sourceNodeModel.name ?: "#error", CustomIcons.Nodes.Annotation) }
//            is ExpectOrActualNode -> { return makeComponent(value.sourceNodeModel.type.toString(), CustomIcons.Nodes.Actual) }
//        }

        return this
    }


    private fun makeComponent(title: String, icon: Icon?): JBLabel {
        val label = JBLabel()
        label.text = title
        label.icon = icon
        return label
    }
}
/*
* myTree.setCellRenderer(new NodeRenderer() {
      @Override
      public void customizeCellRenderer(@NotNull JTree tree,
                                        Object value,
                                        boolean selected,
                                        boolean expanded,
                                        boolean leaf,
                                        int row,
                                        boolean hasFocus) {
        super.customizeCellRenderer(tree, value, selected, expanded, leaf, row, hasFocus);*/