package abyss.model

class SharedItemModel(
    val metaInfo: ItemMetaInfo,
    var expectedItem: ItemCoordinates? = null,
    var actualItems: MutableSet<ItemCoordinates> = mutableSetOf()) {

    override fun toString(): String {
        return "$metaInfo\nexpected: $expectedItem \nactual: ${actualItems.joinToString("\n")}"
    }
}

enum class SharedType {
    EXPECTED, ACTUAL
}

data class ItemCoordinates(var path: String, var offset: Int, var text: String) {
    override fun toString(): String {
        return "path: $path, offset: $offset"
    }
}

data class ItemMetaInfo(val name: String, val type: DeclarationType) {
    override fun toString(): String {
        return "name: $name,\ndeclaration type: $type"
    }
}
