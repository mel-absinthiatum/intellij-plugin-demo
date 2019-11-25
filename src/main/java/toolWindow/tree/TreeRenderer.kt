package toolWindow.tree


import com.intellij.ui.IdeBorderFactory
import com.intellij.ui.JBDefaultTreeCellRenderer
import com.intellij.ui.components.JBLabel
import com.intellij.util.ui.JBUI
import java.awt.Component
import java.awt.Font
import javax.swing.ImageIcon
import javax.swing.JLabel
import javax.swing.JTree
import javax.swing.tree.DefaultMutableTreeNode

class TreeRenderer(tree: JTree) : JBDefaultTreeCellRenderer(tree) {

    override fun getTreeCellRendererComponent(tree: JTree?, value: Any?, sel: Boolean, expanded: Boolean,
                                              leaf: Boolean, row: Int, hasFocus: Boolean): Component {
        super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus)
        if (value is DefaultMutableTreeNode) {
            val label = JBLabel()
            val obj = value.userObject
            when (obj) {
                is TreeNodeContent -> {
                    label.text = obj.title
                    label.font = Font(JLabel().font.fontName, Font.BOLD, JBUI.scale(14))
                    val imageIcon = ImageIcon(javaClass.getResource("/toolWindowResources/star.png"))

                    label.icon = imageIcon
                    label.border = IdeBorderFactory.createEmptyBorder(8, 0, 4, 0)
                }
// TODO: Use enum with assosiated values
            }
            return label
        }
        return this
    }
}
