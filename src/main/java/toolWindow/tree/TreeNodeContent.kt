package toolWindow.tree


class TreeNodeContentImpl(val title: String, override val action: () -> Unit = { println("$title node is selected.")}): TreeNodeContent {
    override fun toString(): String {
        return title
    }
}

interface TreeNodeContent {
    val action: () -> Unit
}