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


        return this
    }



    private fun makeComponent(title: String, icon: Icon?): JBLabel {
        val label = JBLabel()
        label.text = title
        label.icon = icon
        return label
    }
}
