package com.example.data.english

enum class FriendDescriptionType {
    BE_TRAIT,               // He/She is + trait (e.g. friendly, tall and strong)
    HAVE_FEATURE,           // He/She has + feature (e.g. glasses, long hair)
    POSSESSIVE_DESCRIPTION, // His/Her + noun + is/are + description (e.g. His shoes are blue)
    ASK_NAME,               // What's his/her name?
    ANSWER_NAME,            // His/Her name is...
    ALTERNATIVE_QUESTION,   // A boy or girl?
    IDENTITY_CONFIRMATION   // Is he Wu Binbin? / You're right.
}

enum class PronounSet {
    HE_HIS,
    SHE_HER
}

enum class HairStyle {
    SHORT,
    LONG
}

enum class FriendProfileSource {
    TEXTBOOK_CHARACTER,
    APP_VIRTUAL_CHARACTER,
    GENERATED_PRACTICE
}

data class VirtualFriendProfile(
    val characterId: String,
    val displayName: String,
    val pronounSet: PronounSet,
    val traits: Set<String>,
    val hairStyle: HairStyle?,
    val hairColour: String?,
    val accessories: Set<String>,
    val shoeColour: String?,
    val bagColour: String?,
    val sourceType: FriendProfileSource = FriendProfileSource.TEXTBOOK_CHARACTER,
    val localAvatarRef: String = ""
)

enum class FriendClueType {
    PERSONALITY,
    HEIGHT_OR_BUILD,
    HAIR,
    GLASSES,
    SHOES,
    BAG,
    PRONOUN,
    NAME
}

data class FriendClue(
    val clueId: String,
    val clueType: FriendClueType,
    val targetCharacterId: String,
    val expectedValue: String,
    val expressionId: String? = null,
    val generatedPractice: Boolean = false
)

object StandardFriendProfiles {
    val zhangPeng = VirtualFriendProfile(
        characterId = "zhang_peng",
        displayName = "Zhang Peng",
        pronounSet = PronounSet.HE_HIS,
        traits = setOf("tall", "strong", "friendly"),
        hairStyle = HairStyle.SHORT,
        hairColour = "black",
        accessories = emptySet(),
        shoeColour = "black",
        bagColour = "blue"
    )

    val wuBinbin = VirtualFriendProfile(
        characterId = "wu_binbin",
        displayName = "Wu Binbin",
        pronounSet = PronounSet.HE_HIS,
        traits = setOf("tall", "thin", "quiet"),
        hairStyle = HairStyle.SHORT,
        hairColour = "black",
        accessories = setOf("glasses"),
        shoeColour = "blue",
        bagColour = "green"
    )

    val john = VirtualFriendProfile(
        characterId = "john",
        displayName = "John",
        pronounSet = PronounSet.HE_HIS,
        traits = setOf("friendly"),
        hairStyle = HairStyle.SHORT,
        hairColour = "yellow",
        accessories = setOf("hat"),
        shoeColour = "brown",
        bagColour = "yellow"
    )

    val amy = VirtualFriendProfile(
        characterId = "amy",
        displayName = "Amy",
        pronounSet = PronounSet.SHE_HER,
        traits = setOf("quiet", "friendly"),
        hairStyle = HairStyle.SHORT,
        hairColour = "brown",
        accessories = emptySet(),
        shoeColour = "red",
        bagColour = "pink"
    )

    val lucy = VirtualFriendProfile(
        characterId = "lucy",
        displayName = "Lucy",
        pronounSet = PronounSet.SHE_HER,
        traits = setOf("tall", "thin", "friendly"),
        hairStyle = HairStyle.LONG,
        hairColour = "black",
        accessories = emptySet(),
        shoeColour = "blue",
        bagColour = "green"
    )

    val sarah = VirtualFriendProfile(
        characterId = "sarah",
        displayName = "Sarah",
        pronounSet = PronounSet.SHE_HER,
        traits = setOf("friendly"),
        hairStyle = HairStyle.LONG,
        hairColour = "yellow",
        accessories = setOf("hat"),
        shoeColour = "brown",
        bagColour = "red"
    )

    val allTextbookProfiles = listOf(zhangPeng, wuBinbin, john, amy, lucy, sarah)
}
