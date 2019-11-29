package abyss.view

import abyss.model.SharedType


class AbyssTreeNodeImpl(
    override val sharedType: SharedType,
    override val title: String,
    override val action: () -> Unit = {}
): AbyssTreeNode {

    override fun toString(): String {
        return title
    }
}

interface AbyssTreeNode {
    val sharedType: SharedType
    val title: String
    val action: () -> Unit
}

enum class AbyssTreeNodeType {

}
