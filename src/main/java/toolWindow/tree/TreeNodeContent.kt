package toolWindow.tree


class TreeNodeContentImpl(override val title: String, override val action: () -> Unit = { println("$title node is selected.")}): TreeNodeContent {
    override fun toString(): String {
        return title
    }
}

interface TreeNodeContent {
    val title: String
    val action: () -> Unit
}