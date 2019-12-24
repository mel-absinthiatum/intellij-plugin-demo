import abyss.imageManager.CustomIcons
import abyss.model.tree.nodes.NodeModel
import abyss.model.tree.nodes.TemplateNode
import javax.swing.Icon
import kotlin.test.assertEquals
import org.junit.Test as test

class TreeTests {

    data class TestNodeModel(val title: String): NodeModel {
        override fun getLabelText(): String = title
        override fun getIcon(): Icon? = CustomIcons.Nodes.File
    }

    class TestNode(title: String): TemplateNode<TestNodeModel, TestNode, TestNode>(TestNodeModel(title))

    @test fun f() {
        val root = TestNode("1").including(
            TestNode("1-1"),
            TestNode("1-2"),
            TestNode("1-3")
        )

        assertEquals(root.model, TestNodeModel("1"))
    }
}