package com.example.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "holiday_homework_packs")
data class HolidayHomeworkPack(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val packId: String,
    val title: String,
    val subtitle: String,
    val grade: String,
    val semester: String,
    val sourceType: String = "USER_PRIVATE_HOMEWORK",
    val description: String,
    val startDate: String? = null,
    val endDate: String? = null,
    val isInstalled: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "holiday_tasks")
data class HolidayTask(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val packId: String,
    val subject: String, // CHINESE / MATH / ENGLISH / LIFE / PRACTICE / MOVIE
    val category: String, // 读书积累 / 阅读训练 / 习作 / 练字 / 背诵默写 / 数学练习 / 英语听力等
    val title: String,
    val description: String,
    val requirement: String,
    val taskType: String, // CHECK_IN, COUNT_PROGRESS, WRITING_PRACTICE, DICTATION_LINK, RECITATION_MEMORIZE, COMPOSITION, PARENT_CONFIRM
    val totalCount: Int = 1,
    val completedCount: Int = 0,
    val unitLabel: String? = "次",
    val frequencyRule: String? = "DAILY", // DAILY, WEEKLY, ONCE, CUSTOM
    val linkedLevelId: Long? = null,
    val sortOrder: Int = 0,
    val isRequired: Boolean = true,
    val tierRule: String = "ALL", // ALL, MATH_90_ABOVE, MATH_90_BELOW, ENG_88_ABOVE, ENG_88_BELOW
    val status: String = "PENDING", // PENDING, IN_PROGRESS, COMPLETED
    val isRecited: Boolean = false,
    val isMemorized: Boolean = false,
    val isDraftDone: Boolean = false,
    val isFinalWritten: Boolean = false,
    val isParentConfirmed: Boolean = false,
    val linkedMaterialIdsStr: String? = null,
    val materialRunnerType: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "holiday_task_check_ins")
data class HolidayTaskCheckIn(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val taskId: Long,
    val date: String, // YYYY-MM-DD
    val progressDelta: Int = 1,
    val note: String? = null,
    val parentConfirmed: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "holiday_work_sessions")
data class HolidayWorkSession(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val taskId: Long,
    val packId: String = "summer_homework_grade5_to_6_2026_private_v1",
    val subject: String,
    val taskType: String,
    val startedAt: Long = System.currentTimeMillis(),
    val endedAt: Long? = null,
    val durationMinutes: Int = 0,
    val progressDelta: Int = 0,
    val note: String? = null,
    val titleInput: String? = null,
    val extraJson: String? = null,
    val parentConfirmed: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "holiday_study_materials")
data class HolidayStudyMaterial(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val packId: String = "summer_homework_grade5_to_6_2026_private_v1",
    val materialId: String,
    val subject: String = "CHINESE",
    val grade: String = "六年级",
    val semester: String = "上册",
    val unitName: String? = null,
    val lessonNo: String? = null,
    val lessonTitle: String,
    val materialType: String, // RECITATION_TEXT, POEM, CLASSICAL_TEXT, DAILY_ACCUMULATION, WRITING_TABLE, WORD_TABLE, COPYWORK
    val title: String,
    val author: String? = null,
    val fullText: String,
    val sectionsJson: String? = null,
    val annotationJson: String? = null,
    val sourceNote: String = "五升六暑假作业及六上教材必背要求",
    val sourcePage: String? = null,
    val memorizeRequired: Boolean = true,
    val dictationRequired: Boolean = true,
    val copyRequired: Boolean = false,
    val isPrivate: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "holiday_material_progress")
data class HolidayMaterialProgress(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val materialId: String,
    val taskId: Long? = null,
    val readCount: Int = 0,
    val reciteStatus: String = "NOT_STARTED", // NOT_STARTED, READING, FAMILIAR, RECITED, MEMORIZED
    val dictationStatus: String = "NOT_STARTED", // NOT_STARTED, PASSED, NEED_RETRY
    val copyCount: Int = 0,
    val parentConfirmed: Boolean = false,
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "holiday_recitation_records")
data class HolidayRecitationRecord(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val materialId: String,
    val materialTitle: String,
    val audioPath: String,
    val durationMs: Long,
    val mode: String, // PRACTICE, MEMORIZE
    val parentStatus: String = "PENDING", // PENDING, PASSED, NEED_RETRY
    val parentComment: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "holiday_dictation_records")
data class HolidayDictationRecord(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val materialId: String,
    val materialTitle: String,
    val sentenceIndex: Int,
    val standardText: String,
    val handwrittenStrokesJson: String,
    val status: String = "PENDING", // PENDING, PASSED, NEED_RETRY
    val durationMs: Long = 0,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "player_profile")
data class PlayerProfile(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(defaultValue = "0") val accountId: Long = 0,
    val playerName: String,
    val avatarId: Int,
    val petId: String? = null,
    @ColumnInfo(defaultValue = "1") val level: Int = 1,
    @ColumnInfo(defaultValue = "0") val exp: Int = 0,
    @ColumnInfo(defaultValue = "0") val coins: Int = 0,
    @ColumnInfo(defaultValue = "0") val totalStudyDays: Int = 0,
    @ColumnInfo(defaultValue = "0") val streakDays: Int = 0,
    @ColumnInfo(defaultValue = "0") val highestCombo: Int = 0,
    @ColumnInfo(defaultValue = "0") val isSelected: Boolean = false,
    @ColumnInfo(defaultValue = "0") val isDeleted: Boolean = false,
    @ColumnInfo(defaultValue = "default_black") val equippedBrushId: String = "default_black",
    @ColumnInfo(defaultValue = "default_black,practice_wood") val unlockedBrushIds: String = "default_black,practice_wood",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "local_accounts")
data class LocalAccount(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val accountName: String,
    val pinHash: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val lastLoginAt: Long = System.currentTimeMillis(),
    @ColumnInfo(defaultValue = "1") val isActive: Boolean = true,
    @ColumnInfo(defaultValue = "0") val isDeleted: Boolean = false
)

@Entity(tableName = "pet_bindings")
data class PetBinding(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val accountId: Long,
    val playerId: Long,
    val petId: String,
    val petName: String,
    @ColumnInfo(defaultValue = "1") val level: Int = 1,
    @ColumnInfo(defaultValue = "100") val intimacy: Int = 100,
    @ColumnInfo(defaultValue = "1") val isActive: Boolean = true,
    @ColumnInfo(defaultValue = "HATCHLING") val lifeStage: String = "HATCHLING",
    val customName: String? = null,
    @ColumnInfo(defaultValue = "0") val hatchProgress: Int = 0,
    @ColumnInfo(defaultValue = "0") val growthExp: Int = 0,
    @ColumnInfo(defaultValue = "0") val spiritPower: Int = 0,
    @ColumnInfo(defaultValue = "0") val isSleeping: Boolean = false,
    val hatchedAt: Long? = null,
    val lastFedAt: Long? = null,
    @ColumnInfo(defaultValue = "0") val fedCountToday: Int = 0,
    val lastFedDay: String? = null,
    @ColumnInfo(defaultValue = "100") val hunger: Int = 100,
    @ColumnInfo(defaultValue = "0") val discoveredType: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "local_sessions")
data class LocalSession(
    @PrimaryKey val id: Int = 1,
    val currentAccountId: Long? = null,
    val currentPlayerId: Long? = null,
    val lastRoute: String? = null,
    val updatedAt: Long = System.currentTimeMillis()
)

