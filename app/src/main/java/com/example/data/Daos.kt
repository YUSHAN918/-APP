package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface PlayerBrushConfigDao {
    @Query("SELECT * FROM player_brush_configs WHERE playerId = :playerId AND brushId = :brushId")
    fun getConfigFlow(playerId: Long, brushId: String): Flow<PlayerBrushConfig?>

    @Query("SELECT * FROM player_brush_configs WHERE playerId = :playerId AND brushId = :brushId")
    suspend fun getConfigDirect(playerId: Long, brushId: String): PlayerBrushConfig?
    
    @Query("SELECT * FROM player_brush_configs WHERE playerId = :playerId")
    fun getAllConfigsForPlayerFlow(playerId: Long): Flow<List<PlayerBrushConfig>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateConfig(config: PlayerBrushConfig): Long
}

@Dao
interface WordDao {
    @Query("SELECT * FROM words")
    fun getAllWords(): Flow<List<WordItem>>

    @Query("SELECT * FROM words")
    suspend fun getAllWordsDirect(): List<WordItem>

    @Query("SELECT * FROM words WHERE unitName = :unitName")
    fun getWordsByUnit(unitName: String): Flow<List<WordItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWords(words: List<WordItem>): List<Long>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWord(word: WordItem)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWordAndGetId(word: WordItem): Long
    
    @Query("SELECT * FROM words WHERE id IN (:ids)")
    suspend fun getWordsByIds(ids: List<Int>): List<WordItem>
    
    @Query("DELETE FROM words WHERE id = :id")
    suspend fun deleteWord(id: Int)
}

@Dao
interface WrongWordDao {
    @Query("SELECT * FROM wrong_words ORDER BY lastErrorTime DESC")
    fun getAllWrongWords(): Flow<List<WrongWordItem>>

    @Query("SELECT * FROM wrong_words WHERE text = :text LIMIT 1")
    suspend fun getWrongWordByText(text: String): WrongWordItem?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWrongWord(wrongWord: WrongWordItem)
    
    @Update
    suspend fun updateWrongWord(wrongWord: WrongWordItem)

    @Query("DELETE FROM wrong_words WHERE text = :text")
    suspend fun deleteWrongWordByText(text: String)
}

@Dao
interface LevelDao {
    @Query("SELECT * FROM levels")
    fun getAllLevels(): Flow<List<Level>>

    @Query("SELECT * FROM levels")
    suspend fun getAllLevelsDirect(): List<Level>

    @Query("SELECT * FROM levels WHERE id = :id LIMIT 1")
    suspend fun getLevelById(id: Int): Level?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLevels(levels: List<Level>)

    @Update
    suspend fun updateLevel(level: Level)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLevel(level: Level): Long
    
    @Query("DELETE FROM levels WHERE id = :id")
    suspend fun deleteLevel(id: Int)
}

@Dao
interface UserStatsDao {
    @Query("SELECT * FROM user_stats WHERE id = 1")
    fun getUserStats(): Flow<UserStats?>

    @Query("SELECT * FROM user_stats WHERE id = 1")
    suspend fun getUserStatsDirect(): UserStats?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateStats(stats: UserStats)
}

@Dao
interface PracticeSessionDao {
    @Query("SELECT * FROM practice_sessions ORDER BY startedAt DESC")
    fun getAllSessions(): Flow<List<PracticeSession>>

    @Query("SELECT * FROM practice_sessions WHERE sessionId = :id LIMIT 1")
    suspend fun getSessionById(id: Int): PracticeSession?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: PracticeSession): Long

    @Update
    suspend fun updateSession(session: PracticeSession)

    @Query("DELETE FROM practice_sessions WHERE sessionId = :id")
    suspend fun deleteSession(id: Int)
}

@Dao
interface HolidayHomeworkDao {
    @Query("SELECT * FROM holiday_homework_packs ORDER BY createdAt DESC")
    fun getAllPacks(): Flow<List<HolidayHomeworkPack>>

    @Query("SELECT * FROM holiday_homework_packs ORDER BY createdAt DESC")
    suspend fun getAllPacksDirect(): List<HolidayHomeworkPack>

    @Query("SELECT * FROM holiday_homework_packs WHERE packId = :packId LIMIT 1")
    suspend fun getPackById(packId: String): HolidayHomeworkPack?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPack(pack: HolidayHomeworkPack): Long

    @Update
    suspend fun updatePack(pack: HolidayHomeworkPack)

    @Query("DELETE FROM holiday_homework_packs WHERE packId = :packId")
    suspend fun deletePackByPackId(packId: String)

    @Query("SELECT * FROM holiday_tasks ORDER BY sortOrder ASC, id ASC")
    fun getAllTasks(): Flow<List<HolidayTask>>

    @Query("SELECT * FROM holiday_tasks WHERE packId = :packId ORDER BY sortOrder ASC, id ASC")
    fun getTasksByPackId(packId: String): Flow<List<HolidayTask>>

    @Query("SELECT * FROM holiday_tasks WHERE packId = :packId ORDER BY sortOrder ASC, id ASC")
    suspend fun getTasksByPackIdDirect(packId: String): List<HolidayTask>

    @Query("SELECT * FROM holiday_tasks WHERE id = :id LIMIT 1")
    suspend fun getTaskById(id: Long): HolidayTask?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: HolidayTask): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTasks(tasks: List<HolidayTask>)

    @Update
    suspend fun updateTask(task: HolidayTask)

    @Query("DELETE FROM holiday_tasks WHERE packId = :packId")
    suspend fun deleteTasksByPackId(packId: String)

    @Query("DELETE FROM holiday_tasks WHERE id = :id")
    suspend fun deleteTaskById(id: Long)

    @Query("SELECT * FROM holiday_task_check_ins WHERE taskId = :taskId ORDER BY createdAt DESC")
    fun getCheckInsForTask(taskId: Long): Flow<List<HolidayTaskCheckIn>>

    @Query("SELECT * FROM holiday_task_check_ins WHERE taskId = :taskId AND date = :date LIMIT 1")
    suspend fun getTodayCheckInForTask(taskId: Long, date: String): HolidayTaskCheckIn?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCheckIn(checkIn: HolidayTaskCheckIn): Long

    @Query("SELECT * FROM holiday_task_check_ins ORDER BY createdAt DESC")
    fun getAllCheckIns(): Flow<List<HolidayTaskCheckIn>>

    @Query("DELETE FROM holiday_task_check_ins WHERE taskId = :taskId AND date = :date")
    suspend fun deleteCheckInForTaskAndDate(taskId: Long, date: String)

    // Work Sessions
    @Query("SELECT * FROM holiday_work_sessions ORDER BY createdAt DESC")
    fun getAllWorkSessions(): Flow<List<HolidayWorkSession>>

    @Query("SELECT * FROM holiday_work_sessions WHERE taskId = :taskId ORDER BY createdAt DESC")
    fun getWorkSessionsForTask(taskId: Long): Flow<List<HolidayWorkSession>>

    @Query("SELECT * FROM holiday_work_sessions WHERE id = :id LIMIT 1")
    suspend fun getWorkSessionById(id: Long): HolidayWorkSession?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorkSession(session: HolidayWorkSession): Long

    @Update
    suspend fun updateWorkSession(session: HolidayWorkSession)

    @Query("DELETE FROM holiday_work_sessions WHERE id = :id")
    suspend fun deleteWorkSession(id: Long)

    // Materials & Progress
    @Query("SELECT * FROM holiday_study_materials ORDER BY id ASC")
    fun getAllMaterials(): Flow<List<HolidayStudyMaterial>>

    @Query("SELECT * FROM holiday_study_materials ORDER BY id ASC")
    suspend fun getAllMaterialsDirect(): List<HolidayStudyMaterial>

    @Query("SELECT * FROM holiday_study_materials WHERE packId = :packId ORDER BY id ASC")
    fun getMaterialsByPackId(packId: String): Flow<List<HolidayStudyMaterial>>

    @Query("SELECT * FROM holiday_study_materials WHERE materialId = :materialId LIMIT 1")
    suspend fun getMaterialByMaterialId(materialId: String): HolidayStudyMaterial?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMaterials(materials: List<HolidayStudyMaterial>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMaterial(material: HolidayStudyMaterial): Long

    @Query("SELECT * FROM holiday_material_progress")
    fun getAllMaterialProgress(): Flow<List<HolidayMaterialProgress>>

    @Query("SELECT * FROM holiday_material_progress WHERE materialId = :materialId LIMIT 1")
    fun getMaterialProgress(materialId: String): Flow<HolidayMaterialProgress?>

    @Query("SELECT * FROM holiday_material_progress WHERE materialId = :materialId LIMIT 1")
    suspend fun getMaterialProgressDirect(materialId: String): HolidayMaterialProgress?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateMaterialProgress(progress: HolidayMaterialProgress): Long

    @Query("SELECT * FROM holiday_recitation_records ORDER BY createdAt DESC")
    fun getAllRecitationRecords(): Flow<List<HolidayRecitationRecord>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecitationRecord(record: HolidayRecitationRecord): Long

    @Query("UPDATE holiday_recitation_records SET parentStatus = :status WHERE id = :id")
    suspend fun updateRecitationParentStatus(id: Long, status: String)

    @Query("SELECT * FROM holiday_dictation_records ORDER BY createdAt DESC")
    fun getAllDictationRecords(): Flow<List<HolidayDictationRecord>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDictationRecord(record: HolidayDictationRecord): Long

    @Query("UPDATE holiday_dictation_records SET status = :status WHERE id = :id")
    suspend fun updateDictationParentStatus(id: Long, status: String)
}

@Dao
interface PlayerProfileDao {
    @Query("SELECT * FROM player_profile ORDER BY id DESC LIMIT 1")
    fun getPlayerProfile(): Flow<PlayerProfile?>

    @Query("SELECT * FROM player_profile ORDER BY id DESC LIMIT 1")
    suspend fun getPlayerProfileDirect(): PlayerProfile?

    @Query("SELECT * FROM player_profile WHERE id = :id LIMIT 1")
    fun getProfileFlow(id: Long): Flow<PlayerProfile?>

    @Query("SELECT * FROM player_profile WHERE id = :id LIMIT 1")
    suspend fun getProfileById(id: Long): PlayerProfile?

    @Query("SELECT * FROM player_profile WHERE accountId = :accountId AND isDeleted = 0")
    fun getProfilesByAccountFlow(accountId: Long): Flow<List<PlayerProfile>>

    @Query("SELECT * FROM player_profile WHERE accountId = :accountId AND isDeleted = 0")
    suspend fun getProfilesByAccountDirect(accountId: Long): List<PlayerProfile>

    @Query("SELECT * FROM player_profile WHERE isDeleted = 0")
    suspend fun getAllProfilesDirect(): List<PlayerProfile>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateProfile(profile: PlayerProfile): Long

    @Query("UPDATE player_profile SET isSelected = 0 WHERE accountId = :accountId")
    suspend fun deselectAllProfilesForAccount(accountId: Long)

    @Query("UPDATE player_profile SET isSelected = 1 WHERE id = :profileId")
    suspend fun selectProfile(profileId: Long)

    @Query("UPDATE player_profile SET exp = exp + :amount, updatedAt = :updatedAt WHERE id = :id")
    suspend fun addPlayerExp(id: Long, amount: Int, updatedAt: Long = System.currentTimeMillis())

    @Query("UPDATE player_profile SET coins = coins + :amount, updatedAt = :updatedAt WHERE id = :id")
    suspend fun addPlayerCoins(id: Long, amount: Int, updatedAt: Long = System.currentTimeMillis())

    @Query("UPDATE player_profile SET streakDays = :streak, updatedAt = :updatedAt WHERE id = :id")
    suspend fun updateStreak(id: Long, streak: Int, updatedAt: Long = System.currentTimeMillis())
}

@Dao
interface LocalAccountDao {
    @Query("SELECT * FROM local_accounts WHERE isDeleted = 0 ORDER BY lastLoginAt DESC")
    fun getAllAccountsFlow(): Flow<List<LocalAccount>>

    @Query("SELECT * FROM local_accounts WHERE isDeleted = 0 ORDER BY lastLoginAt DESC")
    suspend fun getAllAccountsDirect(): List<LocalAccount>

    @Query("SELECT * FROM local_accounts WHERE id = :id LIMIT 1")
    suspend fun getAccountById(id: Long): LocalAccount?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateAccount(account: LocalAccount): Long
}

@Dao
interface PetBindingDao {
    @Query("SELECT * FROM pet_bindings WHERE playerId = :playerId AND isActive = 1 LIMIT 1")
    fun getActivePetBindingFlow(playerId: Long): Flow<PetBinding?>

    @Query("SELECT * FROM pet_bindings WHERE playerId = :playerId AND isActive = 1 LIMIT 1")
    suspend fun getActivePetBindingDirect(playerId: Long): PetBinding?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateBinding(binding: PetBinding): Long

    @Query("UPDATE pet_bindings SET isActive = 0, updatedAt = :updatedAt WHERE playerId = :playerId AND isActive = 1")
    suspend fun deactivateActiveBinding(playerId: Long, updatedAt: Long = System.currentTimeMillis())

    @Query("SELECT * FROM pet_bindings WHERE playerId = :playerId AND lifeStage = 'SOUL_SLEEP'")
    fun getSleepingPetsFlow(playerId: Long): Flow<List<PetBinding>>
}

@Dao
interface LocalSessionDao {
    @Query("SELECT * FROM local_sessions WHERE id = 1 LIMIT 1")
    fun getSessionFlow(): Flow<LocalSession?>

    @Query("SELECT * FROM local_sessions WHERE id = 1 LIMIT 1")
    suspend fun getSessionDirect(): LocalSession?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateSession(session: LocalSession)

    @Query("DELETE FROM local_sessions WHERE id = 1")
    suspend fun clearSession()
}

@Dao
interface MonsterCodexDao {
    @Query("SELECT * FROM monster_codex_entries WHERE playerId = :playerId ORDER BY lastEncounterAt DESC")
    fun getCodexForPlayerFlow(playerId: Long): Flow<List<MonsterCodexEntry>>

    @Query("SELECT * FROM monster_codex_entries WHERE playerId = :playerId ORDER BY lastEncounterAt DESC")
    suspend fun getCodexForPlayerDirect(playerId: Long): List<MonsterCodexEntry>

    @Query("SELECT * FROM monster_codex_entries WHERE playerId = :playerId AND monsterKey = :monsterKey LIMIT 1")
    suspend fun getCodexEntry(playerId: Long, monsterKey: String): MonsterCodexEntry?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateCodexEntry(entry: MonsterCodexEntry): Long
}

@Dao
interface LootDropDao {
    @Query("SELECT * FROM loot_drop_records WHERE playerId = :playerId ORDER BY createdAt DESC")
    fun getLootRecordsForPlayerFlow(playerId: Long): Flow<List<LootDropRecord>>

    @Query("SELECT * FROM loot_drop_records WHERE playerId = :playerId ORDER BY createdAt DESC")
    suspend fun getLootRecordsForPlayerDirect(playerId: Long): List<LootDropRecord>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLootRecord(record: LootDropRecord): Long
}

@Dao
interface AchievementDao {
    @Query("SELECT * FROM achievement_records WHERE playerId = :playerId ORDER BY id ASC")
    fun getAchievementsForPlayerFlow(playerId: Long): Flow<List<AchievementRecord>>

    @Query("SELECT * FROM achievement_records WHERE playerId = :playerId ORDER BY id ASC")
    suspend fun getAchievementsForPlayerDirect(playerId: Long): List<AchievementRecord>

    @Query("SELECT * FROM achievement_records WHERE playerId = :playerId AND achievementKey = :achievementKey LIMIT 1")
    suspend fun getAchievementByKey(playerId: Long, achievementKey: String): AchievementRecord?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateAchievement(achievement: AchievementRecord): Long
}

@Dao
interface PlayerInventoryDao {
    @Query("SELECT * FROM player_inventory_items WHERE playerId = :playerId")
    fun getInventoryForPlayerFlow(playerId: Long): Flow<List<PlayerInventoryItem>>

    @Query("SELECT * FROM player_inventory_items WHERE playerId = :playerId")
    suspend fun getInventoryForPlayerDirect(playerId: Long): List<PlayerInventoryItem>

    @Query("SELECT * FROM player_inventory_items WHERE playerId = :playerId AND itemId = :itemId LIMIT 1")
    suspend fun getInventoryItem(playerId: Long, itemId: String): PlayerInventoryItem?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateInventoryItem(item: PlayerInventoryItem): Long

    @Query("UPDATE player_inventory_items SET isEquipped = 0 WHERE playerId = :playerId AND itemType = :itemType")
    suspend fun unequipAllOfType(playerId: Long, itemType: String)

    @Query("UPDATE player_inventory_items SET isEquipped = 1 WHERE playerId = :playerId AND itemId = :itemId")
    suspend fun equipItem(playerId: Long, itemId: String)
}





