package com.example.data.english

object EnglishSpatialAnswerEvaluator {

    /**
     * Evaluates if a given object placement matches the expected spatial relation and anchor.
     */
    fun evaluatePlacement(
        itemId: String,
        relation: EnglishSpatialRelation,
        anchorId: String,
        expected: EnglishObjectPlacement
    ): Boolean {
        return itemId.lowercase().trim() == expected.itemId.lowercase().trim() &&
               relation == expected.relation &&
               anchorId.lowercase().trim() == expected.anchorId.lowercase().trim()
    }

    /**
     * Generates a precise spatial description sentence.
     * E.g., "It's under the chair."
     */
    fun getWhereAnswer(
        itemId: String,
        placements: List<EnglishObjectPlacement>,
        anchors: List<EnglishSpatialAnchor>
    ): String {
        val placement = placements.find { it.itemId.lowercase().trim() == itemId.lowercase().trim() } ?: return "I don't know."
        val anchor = anchors.find { it.anchorId.lowercase().trim() == placement.anchorId.lowercase().trim() }
        val anchorName = anchor?.displayName?.lowercase() ?: "desk"
        val prep = when (placement.relation) {
            EnglishSpatialRelation.IN -> "in"
            EnglishSpatialRelation.ON -> "on"
            EnglishSpatialRelation.UNDER -> "under"
        }
        return "It's $prep the $anchorName."
    }

    /**
     * Evaluates a Yes/No confirmation question.
     * E.g., "Is it in the box?" -> Returns true if it is indeed in the box, false otherwise.
     */
    fun evaluateYesNoLocation(
        itemId: String,
        relation: EnglishSpatialRelation,
        anchorId: String,
        placements: List<EnglishObjectPlacement>
    ): Boolean {
        val placement = placements.find { it.itemId.lowercase().trim() == itemId.lowercase().trim() } ?: return false
        return placement.relation == relation && placement.anchorId.lowercase().trim() == anchorId.lowercase().trim()
    }
}
