package abyss.model

class SharedItemAgregatedModel(
    var expectedItem: SharedItemModel,
    var actualItems: Array<SharedItemModel>
)

enum class SharedType {
    EXPECTED, ACTUAL
}

class SharedItemModel(name: String, text: String, type: DeclarationType, sharedType: SharedType, coordinates: ItemCoordinates)

class ItemCoordinates(var path: String, var offset: Int)