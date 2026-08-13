package com.example.data.english

import com.squareup.moshi.JsonClass

enum class EnglishSpatialRelation {
    IN,
    ON,
    UNDER
}

enum class SpatialAnchorType {
    DESK,
    CHAIR,
    BOX,
    BAG,
    BOOK,
    OTHER_CONTAINER,
    OTHER_SURFACE
}

@JsonClass(generateAdapter = true)
data class EnglishObjectPlacement(
    val itemId: String,
    val relation: EnglishSpatialRelation,
    val anchorId: String
)

@JsonClass(generateAdapter = true)
data class SpatialVisualZones(
    val onX: Float = 0.5f,
    val onY: Float = 0.2f,
    val inX: Float = 0.5f,
    val inY: Float = 0.5f,
    val underX: Float = 0.5f,
    val underY: Float = 0.8f
)

@JsonClass(generateAdapter = true)
data class EnglishSpatialAnchor(
    val anchorId: String,
    val anchorType: SpatialAnchorType,
    val allowedRelations: List<EnglishSpatialRelation>,
    val displayName: String,
    val visualZones: SpatialVisualZones = SpatialVisualZones()
)

@JsonClass(generateAdapter = true)
data class SpatialSceneModel(
    val sceneId: String,
    val anchors: List<EnglishSpatialAnchor>,
    val items: List<String>, // list of item IDs
    val placements: List<EnglishObjectPlacement>,
    val allowedActions: List<String> = emptyList(),
    val sourceReference: String = ""
)
