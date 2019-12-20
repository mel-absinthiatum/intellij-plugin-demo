package abyss.view


import abyss.imageManager.CustomIcons
import abyss.model.tree.nodes.*
import com.intellij.ui.JBDefaultTreeCellRenderer
import com.intellij.ui.components.JBLabel
import java.awt.Component
import javax.swing.Icon
import javax.swing.JTree

class AbyssTreeCellRenderer(tree: JTree) : JBDefaultTreeCellRenderer(tree) {

    override fun getTreeCellRendererComponent(tree: JTree?, value: Any?, sel: Boolean, expanded: Boolean,
                                              leaf: Boolean, row: Int, hasFocus: Boolean): Component {
        super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus)

        when (value) {
            is RootNode -> { return this }
            is MppAuthorityZoneNode -> { return makeComponent("#authority zone", CustomIcons.Nodes.Root) }
            is PackageNode -> { return makeComponent(value.model.title, CustomIcons.Nodes.File) }
            is SharedElementNode -> { return makeComponent(value.model.name ?: "#error", CustomIcons.Nodes.Annotation) }
            is ExpectOrActualNode -> { return makeComponent(value.model.type.toString(), CustomIcons.Nodes.Actual) }
        }

        return this
    }


    private fun makeComponent(title: String, icon: Icon): JBLabel {
        val label = JBLabel()
        label.text = title
        label.icon = icon
        return label
    }
}
