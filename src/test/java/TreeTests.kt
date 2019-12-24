import abyss.imageManager.CustomIcons
import abyss.model.tree.diff.TreesDiffManager
import abyss.model.tree.nodes.NodeModel
import abyss.model.tree.nodes.TemplateNode
import javax.swing.Icon
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import org.junit.Test as test

class TreeTests {

    data class TestNodeModel(val title: String): NodeModel {
        override fun getLabelText(): String = title
        override fun getIcon(): Icon? = CustomIcons.Nodes.File
    }

    class TestNode(title: String): TemplateNode<TestNodeModel, TestNode, TestNode>(TestNodeModel(title))

    @test fun `Test root node mutations`() {
        val sourceTreeRoot = TestNode("1").including(
            TestNode("1-1"),
            TestNode("1-2"),
            TestNode("1-3")
        )

        val resultTreeRoot = TestNode("1").including(
            TestNode("1-1"),
            TestNode("1-3"),
            TestNode("1-4")
        )

        val mutationsTree = TreesDiffManager().makeMutationsTree(sourceTreeRoot, resultTreeRoot)

        assertNotNull(mutationsTree, "Mutations tree must be non null")

        val rootModel = mutationsTree.userObject as? TreesDiffManager.DiffNodeModel<*>

        assertNotNull(rootModel, "Root mutations must be non null")

        val rootMutations = rootModel.mutations

        assertEquals(rootMutations.size, 2)

        assertEquals(sourceTreeRoot.model, TestNodeModel("1"))
    }
}