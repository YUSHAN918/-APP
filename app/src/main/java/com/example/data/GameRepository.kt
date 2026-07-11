package com.example.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.ExperimentalCoroutinesApi
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class GameRepository(private val db: AppDatabase) {
    val allLevels: Flow<List<Level>> = db.levelDao().getAllLevels()
    val allWords: Flow<List<WordItem>> = db.wordDao().getAllWords()
    val allWrongWords: Flow<List<WrongWordItem>> = db.wrongWordDao().getAllWrongWords()
    val userStats: Flow<UserStats?> = db.userStatsDao().getUserStats()
    val allSessions: Flow<List<PracticeSession>> = db.practiceSessionDao().getAllSessions()
    val allHolidayPacks: Flow<List<HolidayHomeworkPack>> = db.holidayHomeworkDao().getAllPacks()
    val allHolidayTasks: Flow<List<HolidayTask>> = db.holidayHomeworkDao().getAllTasks()
    val allHolidayCheckIns: Flow<List<HolidayTaskCheckIn>> = db.holidayHomeworkDao().getAllCheckIns()
    val allHolidayWorkSessions: Flow<List<HolidayWorkSession>> = db.holidayHomeworkDao().getAllWorkSessions()
    val allHolidayMaterials: Flow<List<HolidayStudyMaterial>> = db.holidayHomeworkDao().getAllMaterials()
    val allHolidayMaterialProgress: Flow<List<HolidayMaterialProgress>> = db.holidayHomeworkDao().getAllMaterialProgress()
    val allRecitationRecords: Flow<List<HolidayRecitationRecord>> = db.holidayHomeworkDao().getAllRecitationRecords()
    val allDictationRecords: Flow<List<HolidayDictationRecord>> = db.holidayHomeworkDao().getAllDictationRecords()

    val localAccounts: Flow<List<LocalAccount>> = db.localAccountDao().getAllAccountsFlow()
    val currentSession: Flow<LocalSession?> = db.localSessionDao().getSessionFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val playerProfile: Flow<PlayerProfile?> = currentSession.flatMapLatest { session ->
        val pid = session?.currentPlayerId
        if (pid != null) {
            db.playerProfileDao().getProfileFlow(pid)
        } else {
            flowOf(null)
        }
    }

    suspend fun checkAndPerformAutoMigration() {
        val accounts = db.localAccountDao().getAllAccountsDirect()
        if (accounts.isEmpty()) {
            val legacyProfiles = db.playerProfileDao().getAllProfilesDirect()
            if (legacyProfiles.isNotEmpty()) {
                val defaultAccount = LocalAccount(
                    accountName = "我的冒险档案",
                    createdAt = System.currentTimeMillis(),
                    updatedAt = System.currentTimeMillis()
                )
                val accountId = db.localAccountDao().insertOrUpdateAccount(defaultAccount)
                
                var firstPlayerId: Long? = null
                legacyProfiles.forEachIndexed { index, profile ->
                    val updatedProfile = profile.copy(
                        accountId = accountId,
                        isSelected = index == 0,
                        updatedAt = System.currentTimeMillis()
                    )
                    db.playerProfileDao().insertOrUpdateProfile(updatedProfile)
                    if (index == 0) {
                        firstPlayerId = profile.id
                    }
                }
                
                val localSession = LocalSession(
                    id = 1,
                    currentAccountId = accountId,
                    currentPlayerId = firstPlayerId,
                    updatedAt = System.currentTimeMillis()
                )
                db.localSessionDao().insertOrUpdateSession(localSession)
            }
        }
    }

    fun getProfilesForAccount(accountId: Long): Flow<List<PlayerProfile>> {
        return db.playerProfileDao().getProfilesByAccountFlow(accountId)
    }

    fun getActivePetForPlayer(playerId: Long): Flow<PetBinding?> {
        return db.petBindingDao().getActivePetBindingFlow(playerId)
    }

    fun getSleepingPetsForPlayer(playerId: Long): Flow<List<PetBinding>> {
        return db.petBindingDao().getSleepingPetsFlow(playerId)
    }

    suspend fun updatePetBinding(binding: PetBinding) {
        db.petBindingDao().insertOrUpdateBinding(binding)
    }

    suspend fun getPlayerProfileDirect(): PlayerProfile? {
        val session = db.localSessionDao().getSessionDirect() ?: return null
        val pid = session.currentPlayerId ?: return null
        return db.playerProfileDao().getProfileById(pid)
    }

    suspend fun savePlayerProfile(profile: PlayerProfile): Long {
        return db.playerProfileDao().insertOrUpdateProfile(profile)
    }

    suspend fun createAccount(name: String, pin: String? = null): Long {
        val account = LocalAccount(
            accountName = name,
            pinHash = pin,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis(),
            lastLoginAt = System.currentTimeMillis()
        )
        return db.localAccountDao().insertOrUpdateAccount(account)
    }

    suspend fun renameAccount(accountId: Long, newName: String) {
        val account = db.localAccountDao().getAccountById(accountId) ?: return
        db.localAccountDao().insertOrUpdateAccount(
            account.copy(accountName = newName, updatedAt = System.currentTimeMillis())
        )
    }

    suspend fun deleteAccount(accountId: Long) {
        val account = db.localAccountDao().getAccountById(accountId) ?: return
        db.localAccountDao().insertOrUpdateAccount(
            account.copy(isDeleted = true, updatedAt = System.currentTimeMillis())
        )
        val players = db.playerProfileDao().getProfilesByAccountDirect(accountId)
        players.forEach { player ->
            db.playerProfileDao().insertOrUpdateProfile(
                player.copy(isDeleted = true, isSelected = false, updatedAt = System.currentTimeMillis())
            )
        }
        val session = db.localSessionDao().getSessionDirect()
        if (session?.currentAccountId == accountId) {
            db.localSessionDao().insertOrUpdateSession(
                LocalSession(id = 1, currentAccountId = null, currentPlayerId = null, updatedAt = System.currentTimeMillis())
            )
        }
    }

    suspend fun selectAccount(accountId: Long) {
        val account = db.localAccountDao().getAccountById(accountId) ?: return
        db.localAccountDao().insertOrUpdateAccount(
            account.copy(lastLoginAt = System.currentTimeMillis(), updatedAt = System.currentTimeMillis())
        )
        val players = db.playerProfileDao().getProfilesByAccountDirect(accountId)
        val selectedPlayer = players.find { it.isSelected } ?: players.firstOrNull()
        
        db.localSessionDao().insertOrUpdateSession(
            LocalSession(
                id = 1,
                currentAccountId = accountId,
                currentPlayerId = selectedPlayer?.id,
                updatedAt = System.currentTimeMillis()
            )
        )
    }

    suspend fun createPlayer(accountId: Long, name: String, avatarId: Int, petId: String): Long {
        db.playerProfileDao().deselectAllProfilesForAccount(accountId)
        val player = PlayerProfile(
            accountId = accountId,
            playerName = name,
            avatarId = avatarId,
            petId = petId,
            level = 1,
            exp = 0,
            coins = 100,
            totalStudyDays = 1,
            streakDays = 1,
            highestCombo = 0,
            isSelected = true,
            isDeleted = false,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        val playerId = db.playerProfileDao().insertOrUpdateProfile(player)
        
        val petName = when (petId) {
            "小墨龙" -> "小墨龙"
            "小书灵" -> "小书灵"
            "小云狐" -> "小云狐"
            "小竹猫" -> "小竹猫"
            else -> petId
        }
        db.petBindingDao().insertOrUpdateBinding(
            PetBinding(
                accountId = accountId,
                playerId = playerId,
                petId = petId,
                petName = "神秘灵蛋",
                lifeStage = "EGG",
                hatchProgress = 0,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )
        )

        db.localSessionDao().insertOrUpdateSession(
            LocalSession(
                id = 1,
                currentAccountId = accountId,
                currentPlayerId = playerId,
                updatedAt = System.currentTimeMillis()
            )
        )
        initializeDefaultInventoryForPlayer(accountId, playerId)
        return playerId
    }

    suspend fun selectPlayer(accountId: Long, playerId: Long) {
        db.playerProfileDao().deselectAllProfilesForAccount(accountId)
        db.playerProfileDao().selectProfile(playerId)
        
        initializeDefaultInventoryForPlayer(accountId, playerId)
        
        db.localSessionDao().insertOrUpdateSession(
            LocalSession(
                id = 1,
                currentAccountId = accountId,
                currentPlayerId = playerId,
                updatedAt = System.currentTimeMillis()
            )
        )
    }

    suspend fun updatePlayerProfile(playerId: Long, name: String, avatarId: Int) {
        val player = db.playerProfileDao().getProfileById(playerId) ?: return
        db.playerProfileDao().insertOrUpdateProfile(
            player.copy(playerName = name, avatarId = avatarId, updatedAt = System.currentTimeMillis())
        )
    }

    suspend fun deletePlayer(playerId: Long) {
        val player = db.playerProfileDao().getProfileById(playerId) ?: return
        db.playerProfileDao().insertOrUpdateProfile(
            player.copy(isDeleted = true, isSelected = false, updatedAt = System.currentTimeMillis())
        )
        db.petBindingDao().deactivateActiveBinding(playerId)
        
        val session = db.localSessionDao().getSessionDirect()
        if (session?.currentPlayerId == playerId) {
            val players = db.playerProfileDao().getProfilesByAccountDirect(player.accountId)
            val newSelectedPlayer = players.firstOrNull()
            if (newSelectedPlayer != null) {
                db.playerProfileDao().selectProfile(newSelectedPlayer.id)
            }
            db.localSessionDao().insertOrUpdateSession(
                LocalSession(
                    id = 1,
                    currentAccountId = player.accountId,
                    currentPlayerId = newSelectedPlayer?.id,
                    updatedAt = System.currentTimeMillis()
                )
            )
        }
    }

    suspend fun bindPet(accountId: Long, playerId: Long, petId: String, petName: String) {
        db.petBindingDao().deactivateActiveBinding(playerId)
        
        val player = db.playerProfileDao().getProfileById(playerId)
        if (player != null) {
            db.playerProfileDao().insertOrUpdateProfile(
                player.copy(petId = petId, updatedAt = System.currentTimeMillis())
            )
        }
        
        db.petBindingDao().insertOrUpdateBinding(
            PetBinding(
                accountId = accountId,
                playerId = playerId,
                petId = petId,
                petName = petName,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )
        )
    }

    suspend fun unbindPet(playerId: Long) {
        db.petBindingDao().deactivateActiveBinding(playerId)
        
        val player = db.playerProfileDao().getProfileById(playerId)
        if (player != null) {
            db.playerProfileDao().insertOrUpdateProfile(
                player.copy(petId = null, updatedAt = System.currentTimeMillis())
            )
        }
    }

    suspend fun logout() {
        db.localSessionDao().insertOrUpdateSession(
            LocalSession(
                id = 1,
                currentAccountId = null,
                currentPlayerId = null,
                updatedAt = System.currentTimeMillis()
            )
        )
    }

    suspend fun addPlayerExp(amount: Int) {
        val session = db.localSessionDao().getSessionDirect() ?: return
        val playerId = session.currentPlayerId ?: return
        val currentProfile = db.playerProfileDao().getProfileById(playerId) ?: return
        var newExp = currentProfile.exp + amount
        var newLevel = currentProfile.level
        while (newExp >= newLevel * 100) {
            newExp -= newLevel * 100
            newLevel++
        }
        db.playerProfileDao().insertOrUpdateProfile(
            currentProfile.copy(
                exp = newExp,
                level = newLevel,
                updatedAt = System.currentTimeMillis()
            )
        )
    }

    suspend fun addPlayerCoins(amount: Int) {
        val session = db.localSessionDao().getSessionDirect() ?: return
        val playerId = session.currentPlayerId ?: return
        val currentProfile = db.playerProfileDao().getProfileById(playerId) ?: return
        db.playerProfileDao().insertOrUpdateProfile(
            currentProfile.copy(
                coins = currentProfile.coins + amount,
                updatedAt = System.currentTimeMillis()
            )
        )
    }

    suspend fun addPetIntimacy(amount: Int) {
        val session = db.localSessionDao().getSessionDirect() ?: return
        val playerId = session.currentPlayerId ?: return
        val activeBinding = db.petBindingDao().getActivePetBindingDirect(playerId) ?: return
        val newIntimacy = (activeBinding.intimacy + amount).coerceIn(0, 100)
        db.petBindingDao().insertOrUpdateBinding(
            activeBinding.copy(
                intimacy = newIntimacy,
                updatedAt = System.currentTimeMillis()
            )
        )
    }

    suspend fun updateStreak(streak: Int) {
        val session = db.localSessionDao().getSessionDirect() ?: return
        val playerId = session.currentPlayerId ?: return
        val currentProfile = db.playerProfileDao().getProfileById(playerId) ?: return
        val highest = if (streak > currentProfile.highestCombo) streak else currentProfile.highestCombo
        val totalDays = if (streak > currentProfile.streakDays) currentProfile.totalStudyDays + (streak - currentProfile.streakDays) else currentProfile.totalStudyDays
        db.playerProfileDao().insertOrUpdateProfile(
            currentProfile.copy(
                streakDays = streak,
                highestCombo = highest,
                totalStudyDays = totalDays,
                updatedAt = System.currentTimeMillis()
            )
        )
    }

    suspend fun insertSession(session: PracticeSession): Long {
        return db.practiceSessionDao().insertSession(session)
    }

    suspend fun updateSession(session: PracticeSession) {
        db.practiceSessionDao().updateSession(session)
    }

    suspend fun getSessionById(id: Int): PracticeSession? {
        return db.practiceSessionDao().getSessionById(id)
    }

    suspend fun deleteWrongWordByText(text: String) {
        db.wrongWordDao().deleteWrongWordByText(text)
    }


    suspend fun getWordsByUnit(unitName: String): List<WordItem> {
        return db.wordDao().getWordsByUnit(unitName).firstOrNull() ?: emptyList()
    }

    suspend fun checkAndInitDefaultData(context: android.content.Context) {
        val stats = db.userStatsDao().getUserStats().firstOrNull()
        if (stats == null) {
            db.userStatsDao().insertOrUpdateStats(UserStats())
        }

        // 1. Clean up old preset 1st-5th unit levels if present in existing database
        val existingLevels = db.levelDao().getAllLevels().firstOrNull() ?: emptyList()
        val presetLevelsToDelete = existingLevels.filter { 
            it.isPreset || 
            it.sourcePackId == "original_grade_5" || 
            it.name in listOf("第一单元挑战", "第二单元挑战", "第三单元挑战", "第四单元挑战", "第五单元挑战")
        }
        for (lvl in presetLevelsToDelete) {
            db.levelDao().deleteLevel(lvl.id)
        }

        // Clean up old generic preset words
        val existingWords = db.wordDao().getAllWords().firstOrNull() ?: emptyList()
        val presetUnits = listOf("第一单元", "第二单元", "第三单元", "第四单元", "第五单元")
        val presetWordsToDelete = existingWords.filter { it.unitName in presetUnits && it.sourceLesson.isBlank() }
        for (w in presetWordsToDelete) {
            db.wordDao().deleteWord(w.id)
        }

        // Mark original_grade_5 as uninstalled
        ContentPackManager.setPackInstalled(context, "original_grade_5", false)

        // 2. Auto install BuiltinPrivatePacks if no levels exist
        val remainingLevels = db.levelDao().getAllLevels().firstOrNull() ?: emptyList()
        if (remainingLevels.isEmpty()) {
            val privatePack = BuiltinPrivatePacks.packs.firstOrNull() ?: return
            val prefix = privatePack.name
            val groupCounters = mutableMapOf<Int, Int>()
            
            for (unit in privatePack.units) {
                val cleanUnitName = unit.unitName.split("：").first().trim()
                val levelName = "$prefix · $cleanUnitName"
                
                val unitGroup = when {
                    unit.unitName.contains("第一单元") -> 1
                    unit.unitName.contains("第二单元") -> 2
                    unit.unitName.contains("第三单元") -> 3
                    unit.unitName.contains("第四单元") -> 4
                    unit.unitName.contains("第五单元") -> 5
                    unit.unitName.contains("第六单元") -> 6
                    unit.unitName.contains("第七单元") -> 7
                    unit.unitName.contains("第八单元") -> 8
                    else -> 0
                }
                val count = (groupCounters[unitGroup] ?: 0) + 1
                groupCounters[unitGroup] = count
                val sortIdx = if (unitGroup > 0) unitGroup * 100 + count else unit.orderIndex

                val wordIds = mutableListOf<Int>()
                for (item in unit.items) {
                    val rawWord = WordItem(
                        text = item.text,
                        type = item.type,
                        unitName = levelName,
                        difficulty = item.difficulty,
                        promptMode = item.promptMode,
                        hiddenIndicesStr = item.hiddenIndicesStr,
                        visiblePrompt = item.visiblePrompt,
                        ttsPrompt = item.ttsPrompt,
                        contextText = item.contextText,
                        targetAnswer = item.targetAnswer,
                        clueText = item.clueText,
                        meaningHint = item.meaningHint,
                        sourceLesson = unit.lessonName ?: ""
                    )
                    val smartWord = com.example.util.SmartPromptGenerator.generateSmartPrompt(rawWord, com.example.util.SmartPromptGenerator.STRATEGY_SMART_RECOMMEND)
                    val wordId = insertWordAndGetId(smartWord)
                    wordIds.add(wordId.toInt())
                }

                val level = Level(
                    name = levelName,
                    unitName = levelName,
                    isUnlocked = true,
                    isCompleted = false,
                    isPreset = false,
                    wordIdsStr = wordIds.joinToString(","),
                    sortIndex = sortIdx,
                    sourcePackId = privatePack.id,
                    practiceLimitMode = "ALL"
                )
                insertLevel(level)
            }
            ContentPackManager.setPackInstalled(context, privatePack.id, true)
        }

        // Auto-install default holiday homework pack if none exists
        val existingHolidayPacks = db.holidayHomeworkDao().getAllPacks().firstOrNull() ?: emptyList()
        if (existingHolidayPacks.isEmpty()) {
            installHolidayPack(BuiltinHolidayHomeworkPacks.defaultPack.packId)
        } else {
            // Ensure materials are populated even if pack was installed earlier
            val existingMaterials = db.holidayHomeworkDao().getAllMaterials().firstOrNull() ?: emptyList()
            if (existingMaterials.isEmpty()) {
                db.holidayHomeworkDao().insertMaterials(BuiltinHolidayStudyMaterials.materials)
            }
        }
    }

    suspend fun installHolidayPack(packId: String) {
        if (packId == BuiltinHolidayHomeworkPacks.defaultPack.packId) {
            val pack = BuiltinHolidayHomeworkPacks.defaultPack
            val tasks = BuiltinHolidayHomeworkPacks.defaultTasks
            db.holidayHomeworkDao().insertPack(pack.copy(isInstalled = true, updatedAt = System.currentTimeMillis()))
            db.holidayHomeworkDao().insertTasks(tasks)
            db.holidayHomeworkDao().insertMaterials(BuiltinHolidayStudyMaterials.materials)
        }
    }

    suspend fun repairHolidayPack(packId: String) {
        if (packId == BuiltinHolidayHomeworkPacks.defaultPack.packId) {
            val existingTasks = db.holidayHomeworkDao().getTasksByPackId(packId).firstOrNull() ?: emptyList()
            val existingProgressMap = existingTasks.associate { it.title to Triple(it.completedCount, it.status, Pair(it.isRecited, it.isMemorized)) }
            
            val pack = BuiltinHolidayHomeworkPacks.defaultPack
            val tasks = BuiltinHolidayHomeworkPacks.defaultTasks.map { task ->
                val prev = existingProgressMap[task.title]
                if (prev != null) {
                    task.copy(
                        completedCount = prev.first,
                        status = prev.second,
                        isRecited = prev.third.first,
                        isMemorized = prev.third.second
                    )
                } else {
                    task
                }
            }
            db.holidayHomeworkDao().insertPack(pack.copy(isInstalled = true, updatedAt = System.currentTimeMillis()))
            db.holidayHomeworkDao().insertTasks(tasks)
            db.holidayHomeworkDao().insertMaterials(BuiltinHolidayStudyMaterials.materials)
        }
    }

    suspend fun uninstallHolidayPack(packId: String) {
        db.holidayHomeworkDao().deleteTasksByPackId(packId)
        db.holidayHomeworkDao().deletePackByPackId(packId)
    }

    suspend fun checkInHolidayTask(taskId: Long, delta: Int = 1, note: String? = null) {
        val task = db.holidayHomeworkDao().getTaskById(taskId) ?: return
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        
        val newCompleted = (task.completedCount + delta).coerceIn(0, task.totalCount)
        val newStatus = if (newCompleted >= task.totalCount) "COMPLETED" else if (newCompleted > 0) "IN_PROGRESS" else "PENDING"
        
        val updatedTask = task.copy(
            completedCount = newCompleted,
            status = newStatus,
            updatedAt = System.currentTimeMillis()
        )
        db.holidayHomeworkDao().updateTask(updatedTask)

        val checkIn = HolidayTaskCheckIn(
            taskId = taskId,
            date = today,
            progressDelta = delta,
            note = note,
            parentConfirmed = updatedTask.isParentConfirmed
        )
        db.holidayHomeworkDao().insertCheckIn(checkIn)
    }

    suspend fun cancelTodayCheckIn(taskId: Long) {
        val task = db.holidayHomeworkDao().getTaskById(taskId) ?: return
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val checkIn = db.holidayHomeworkDao().getTodayCheckInForTask(taskId, today)
        val delta = checkIn?.progressDelta ?: 1

        db.holidayHomeworkDao().deleteCheckInForTaskAndDate(taskId, today)

        val newCompleted = (task.completedCount - delta).coerceIn(0, task.totalCount)
        val newStatus = if (newCompleted >= task.totalCount) "COMPLETED" else if (newCompleted > 0) "IN_PROGRESS" else "PENDING"

        val updatedTask = task.copy(
            completedCount = newCompleted,
            status = newStatus,
            updatedAt = System.currentTimeMillis()
        )
        db.holidayHomeworkDao().updateTask(updatedTask)
    }

    suspend fun updateTaskProgressDirect(taskId: Long, newCount: Int) {
        val task = db.holidayHomeworkDao().getTaskById(taskId) ?: return
        val newCompleted = newCount.coerceIn(0, task.totalCount)
        val newStatus = if (newCompleted >= task.totalCount) "COMPLETED" else if (newCompleted > 0) "IN_PROGRESS" else "PENDING"

        val updatedTask = task.copy(
            completedCount = newCompleted,
            status = newStatus,
            updatedAt = System.currentTimeMillis()
        )
        db.holidayHomeworkDao().updateTask(updatedTask)
    }

    suspend fun recordWorkSession(session: HolidayWorkSession, deltaProgress: Int = 1, updateTaskProgress: Boolean = true) {
        db.holidayHomeworkDao().insertWorkSession(session)
        if (updateTaskProgress && session.taskId > 0) {
            checkInHolidayTask(session.taskId, delta = deltaProgress, note = session.note)
        }
    }

    suspend fun updateWorkSession(session: HolidayWorkSession) {
        db.holidayHomeworkDao().updateWorkSession(session)
    }

    suspend fun deleteWorkSession(sessionId: Long) {
        db.holidayHomeworkDao().deleteWorkSession(sessionId)
    }

    suspend fun getMaterialByMaterialId(materialId: String): HolidayStudyMaterial? {
        return db.holidayHomeworkDao().getMaterialByMaterialId(materialId)
    }

    suspend fun updateMaterialReciteStatus(materialId: String, reciteStatus: String, taskId: Long? = null) {
        val current = db.holidayHomeworkDao().getMaterialProgressDirect(materialId) ?: HolidayMaterialProgress(materialId = materialId, taskId = taskId)
        val updated = current.copy(
            reciteStatus = reciteStatus,
            readCount = if (reciteStatus == "FAMILIAR" || reciteStatus == "RECITED") current.readCount + 1 else current.readCount,
            taskId = taskId ?: current.taskId,
            updatedAt = System.currentTimeMillis()
        )
        db.holidayHomeworkDao().insertOrUpdateMaterialProgress(updated)
    }

    suspend fun updateMaterialDictationStatus(materialId: String, dictationStatus: String, taskId: Long? = null) {
        val current = db.holidayHomeworkDao().getMaterialProgressDirect(materialId) ?: HolidayMaterialProgress(materialId = materialId, taskId = taskId)
        val updated = current.copy(
            dictationStatus = dictationStatus,
            taskId = taskId ?: current.taskId,
            updatedAt = System.currentTimeMillis()
        )
        db.holidayHomeworkDao().insertOrUpdateMaterialProgress(updated)
    }

    suspend fun toggleMaterialParentConfirmed(materialId: String) {
        val current = db.holidayHomeworkDao().getMaterialProgressDirect(materialId) ?: HolidayMaterialProgress(materialId = materialId)
        val updated = current.copy(
            parentConfirmed = !current.parentConfirmed,
            updatedAt = System.currentTimeMillis()
        )
        db.holidayHomeworkDao().insertOrUpdateMaterialProgress(updated)
    }

    suspend fun insertRecitationRecord(record: HolidayRecitationRecord) = db.holidayHomeworkDao().insertRecitationRecord(record)
    suspend fun updateRecitationParentStatus(id: Long, status: String) = db.holidayHomeworkDao().updateRecitationParentStatus(id, status)
    suspend fun insertDictationRecord(record: HolidayDictationRecord) = db.holidayHomeworkDao().insertDictationRecord(record)
    suspend fun updateDictationParentStatus(id: Long, status: String) = db.holidayHomeworkDao().updateDictationParentStatus(id, status)

    suspend fun updateHolidayTask(task: HolidayTask) {
        val newStatus = if (task.completedCount >= task.totalCount) "COMPLETED" else if (task.completedCount > 0) "IN_PROGRESS" else "PENDING"
        db.holidayHomeworkDao().updateTask(task.copy(status = newStatus, updatedAt = System.currentTimeMillis()))
    }

    suspend fun updateStats(updateBlock: (UserStats) -> UserStats) {
        val currentStats = db.userStatsDao().getUserStats().firstOrNull() ?: UserStats()
        
        // Handle daily reset
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        var statsToUpdate = currentStats
        if (currentStats.lastPracticeDate != today) {
            statsToUpdate = currentStats.copy(dailyPracticeCount = 0, lastPracticeDate = today)
        }
        
        val newStats = updateBlock(statsToUpdate).copy(lastPracticeDate = today)
        // Level up logic: every 100 xp is 1 level
        val newLevel = (newStats.experience / 100) + 1
        db.userStatsDao().insertOrUpdateStats(newStats.copy(level = newLevel))
    }

    suspend fun recordWrongWord(text: String, type: String, unitName: String, errorLevel: String) {
        val existing = db.wrongWordDao().getWrongWordByText(text)
        if (existing != null) {
            db.wrongWordDao().updateWrongWord(existing.copy(
                errorCount = existing.errorCount + 1,
                lastErrorTime = System.currentTimeMillis(),
                isMastered = false,
                correctStreak = 0,
                errorLevel = errorLevel
            ))
        } else {
            db.wrongWordDao().insertWrongWord(WrongWordItem(
                text = text,
                type = type,
                unitName = unitName,
                errorCount = 1,
                lastErrorTime = System.currentTimeMillis(),
                isMastered = false,
                correctStreak = 0,
                errorLevel = errorLevel
            ))
        }
    }
    
    suspend fun updateWrongWordCorrect(text: String) {
        val existing = db.wrongWordDao().getWrongWordByText(text)
        if (existing != null) {
            val newStreak = existing.correctStreak + 1
            db.wrongWordDao().updateWrongWord(existing.copy(
                correctStreak = newStreak,
                isMastered = newStreak >= 3
            ))
        }
    }

    suspend fun markLevelCompletedByName(name: String) {
        val levels = db.levelDao().getAllLevels().firstOrNull() ?: return
        val lvl = levels.find { it.name == name }
        if (lvl != null && !lvl.isCompleted) {
            db.levelDao().updateLevel(lvl.copy(isCompleted = true))
        }
    }

    suspend fun unlockNextLevel(currentUnit: String, currentLevelName: String = "") {
        if (currentLevelName.isNotEmpty()) {
            markLevelCompletedByName(currentLevelName)
        }
        val levels = db.levelDao().getAllLevels().firstOrNull() ?: return
        val currentIdx = levels.indexOfFirst { it.unitName == currentUnit }
        if (currentIdx != -1) {
            // mark current completed
            val curr = levels[currentIdx]
            db.levelDao().updateLevel(curr.copy(isCompleted = true))
            
            // unlock next
            if (currentIdx + 1 < levels.size) {
                val next = levels[currentIdx + 1]
                db.levelDao().updateLevel(next.copy(isUnlocked = true))
            }
        }
    }

    suspend fun addCustomWords(words: List<WordItem>): List<Long> {
        return db.wordDao().insertWords(words)
    }

    suspend fun getLevelById(id: Int): Level? {
        return db.levelDao().getLevelById(id)
    }

    suspend fun getWordsByIds(ids: List<Int>): List<WordItem> {
        return db.wordDao().getWordsByIds(ids)
    }

    suspend fun insertLevel(level: Level): Long {
        return db.levelDao().insertLevel(level)
    }

    suspend fun updateLevel(level: Level) {
        db.levelDao().updateLevel(level)
    }

    suspend fun deleteLevel(id: Int) {
        db.levelDao().deleteLevel(id)
    }

    suspend fun deleteWord(id: Int) {
        db.wordDao().deleteWord(id)
    }

    suspend fun insertWordAndGetId(word: WordItem): Long {
        return db.wordDao().insertWordAndGetId(word)
    }

    suspend fun insertWord(word: WordItem) {
        db.wordDao().insertWord(word)
    }

    suspend fun getAllWordsList(): List<WordItem> {
        return db.wordDao().getAllWords().firstOrNull() ?: emptyList()
    }

    suspend fun updateUserStats(stats: UserStats) {
        db.userStatsDao().insertOrUpdateStats(stats)
    }

    suspend fun addHatchEnergy(amount: Int): String? {
        val session = db.localSessionDao().getSessionDirect() ?: return null
        val playerId = session.currentPlayerId ?: return null
        val activeBinding = db.petBindingDao().getActivePetBindingDirect(playerId) ?: return null
        
        if (activeBinding.lifeStage == "EGG") {
            val newProgress = (activeBinding.hatchProgress + amount).coerceAtMost(100)
            
            db.petBindingDao().insertOrUpdateBinding(
                activeBinding.copy(
                    hatchProgress = newProgress,
                    updatedAt = System.currentTimeMillis()
                )
            )
            
            if (newProgress >= 100 && activeBinding.hatchProgress < 100) {
                return "灵蛋能量已满！快去宠物小屋孵化它吧！🎉"
            }
        } else if (activeBinding.lifeStage != "SOUL_SLEEP") {
            // Also accumulate growth exp if not egg or asleep
            db.petBindingDao().insertOrUpdateBinding(
                activeBinding.copy(
                    growthExp = activeBinding.growthExp + amount,
                    updatedAt = System.currentTimeMillis()
                )
            )
        }
        return null
    }

    suspend fun hatchEgg(): String? {
        val session = db.localSessionDao().getSessionDirect() ?: return "未找到当前角色会话"
        val playerId = session.currentPlayerId ?: return "请先选择角色"
        val activeBinding = db.petBindingDao().getActivePetBindingDirect(playerId) ?: return "未契约宠物"

        if (activeBinding.lifeStage == "EGG" && activeBinding.hatchProgress >= 100) {
            val possiblePets = listOf("小墨龙", "小书灵", "小云狐", "小竹猫")
            val newPetId = possiblePets.random()
            val newPetName = newPetId
            
            db.petBindingDao().insertOrUpdateBinding(
                activeBinding.copy(
                    petId = newPetId,
                    petName = newPetName,
                    lifeStage = "INFANT",
                    hatchedAt = System.currentTimeMillis(),
                    updatedAt = System.currentTimeMillis()
                )
            )
            return "孵化成功！诞生了 $newPetName！🎉"
        }
        return "孵化条件未满足"
    }

    suspend fun feedPet(foodType: String): String {
        val session = db.localSessionDao().getSessionDirect() ?: return "未找到当前角色会话"
        val playerId = session.currentPlayerId ?: return "请先选择角色"
        val activeBinding = db.petBindingDao().getActivePetBindingDirect(playerId) ?: return "未契约宠物"
        val player = db.playerProfileDao().getProfileById(playerId) ?: return "未找到角色"
        
        if (activeBinding.lifeStage == "EGG") {
            return "灵蛋还在孵化中，暂时不能投喂哦！"
        }
        
        if (activeBinding.lifeStage == "SOUL_SLEEP" || activeBinding.isSleeping) {
            return "字灵正在灵魂庭院中长眠，需要先复苏它！"
        }

        val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        var currentFedCount = activeBinding.fedCountToday
        val lastFedD = activeBinding.lastFedDay
        
        if (lastFedD != todayStr) {
            currentFedCount = 0
        }
        
        if (currentFedCount >= 5) {
            return "今天已经投喂太多次了，它已经吃饱啦，明天再来吧！🐾"
        }

        val cost = if (foodType == "ELITE") 50 else 10
        if (player.coins < cost) {
            return "金币不足，投喂需要 $cost 金币！"
        }

        val intimacyGain = if (foodType == "ELITE") 15 else 5
        val growthGain = if (foodType == "ELITE") 15 else 5
        val hungerGain = if (foodType == "ELITE") 40 else 20

        db.playerProfileDao().insertOrUpdateProfile(
            player.copy(coins = player.coins - cost, updatedAt = System.currentTimeMillis())
        )

        var newIntimacy = (activeBinding.intimacy + intimacyGain).coerceIn(0, 100)
        var newGrowthExp = activeBinding.growthExp + growthGain
        var newLevel = activeBinding.level
        var newHunger = (activeBinding.hunger + hungerGain).coerceAtMost(100)
        var logMsg = "投喂成功！消耗了 $cost 金币，饱食度恢复了 $hungerGain。"

        if (newGrowthExp >= 100) {
            if (newIntimacy >= 60) {
                if (newLevel < 20) {
                    newLevel++
                    newGrowthExp -= 100
                    logMsg += " 恭喜！字灵升级到 Lv.$newLevel！🎉"
                } else {
                    newGrowthExp = 100
                    logMsg += " 字灵已达到最高等级！"
                }
            } else {
                newGrowthExp = 99
                logMsg += " 亲密度不足，多学习和互动才能继续升级哦！❤️"
            }
        }

        var newStage = activeBinding.lifeStage
        if (newStage == "INFANT" && newLevel >= 5) {
            newStage = "ADULT"
            logMsg += " 字灵成长到了成长期！"
        } else if (newStage == "ADULT" && newLevel >= 15) {
            newStage = "AWAKENED"
            logMsg += " 字灵觉醒了全部力量！"
        }

        db.petBindingDao().insertOrUpdateBinding(
            activeBinding.copy(
                intimacy = newIntimacy,
                growthExp = newGrowthExp,
                level = newLevel,
                hunger = newHunger,
                lifeStage = newStage,
                fedCountToday = currentFedCount + 1,
                lastFedDay = todayStr,
                updatedAt = System.currentTimeMillis()
            )
        )

        return logMsg
    }

    suspend fun awakenPet(useCoins: Boolean): String {
        val session = db.localSessionDao().getSessionDirect() ?: return "未找到当前角色会话"
        val playerId = session.currentPlayerId ?: return "请先选择角色"
        val activeBinding = db.petBindingDao().getActivePetBindingDirect(playerId) ?: return "未契约宠物"
        val player = db.playerProfileDao().getProfileById(playerId) ?: return "未找到角色"

        if (!activeBinding.isSleeping && activeBinding.lifeStage != "SOUL_SLEEP") {
            return "宠物不需要被唤醒哦！"
        }

        if (useCoins) {
            if (player.coins < 100) {
                return "金币不足，复苏灵魂需要 100 金币！"
            }
            db.playerProfileDao().insertOrUpdateProfile(
                player.copy(coins = player.coins - 100, updatedAt = System.currentTimeMillis())
            )
        } else {
            if (activeBinding.intimacy < 100) {
                return "亲密度未满 100，还无法进行免消耗复苏！"
            }
        }

        val targetStage = if (activeBinding.level >= 15) "AWAKENED" else if (activeBinding.level >= 5) "ADULT" else "INFANT"

        db.petBindingDao().insertOrUpdateBinding(
            activeBinding.copy(
                isSleeping = false,
                lifeStage = targetStage,
                intimacy = 80,
                hunger = 50,
                updatedAt = System.currentTimeMillis()
            )
        )

        return "契约重铸！${activeBinding.customName ?: activeBinding.petName} 在温暖的光芒中回到了你的身边！✨🐾"
    }

    suspend fun petDailyDecay(context: android.content.Context): String? {
        val session = db.localSessionDao().getSessionDirect() ?: return null
        val playerId = session.currentPlayerId ?: return null
        val activeBinding = db.petBindingDao().getActivePetBindingDirect(playerId) ?: return null
        
        if (activeBinding.isSleeping || activeBinding.lifeStage == "SOUL_SLEEP" || activeBinding.lifeStage == "EGG") {
            return null
        }

        val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val sp = context.getSharedPreferences("word_crusade_pet_decay", android.content.Context.MODE_PRIVATE)
        val lastDecayDay = sp.getString("last_decay_${playerId}", "")
        
        if (lastDecayDay != todayStr) {
            sp.edit().putString("last_decay_${playerId}", todayStr).apply()
            
            val newHunger = (activeBinding.hunger - 10).coerceAtLeast(0)
            val isSleepingNow = newHunger == 0
            val newStage = if (isSleepingNow) "SOUL_SLEEP" else activeBinding.lifeStage
            
            db.petBindingDao().insertOrUpdateBinding(
                activeBinding.copy(
                    hunger = newHunger,
                    isSleeping = isSleepingNow,
                    lifeStage = newStage,
                    updatedAt = System.currentTimeMillis()
                )
            )
            
            if (isSleepingNow) {
                return "噢不，${activeBinding.customName ?: activeBinding.petName}饱食度耗尽，在灵魂庭院的指引下进入了「长眠状态」，快去唤醒它吧！💤"
            }
        }
        return null
    }

    suspend fun equipBrush(brushId: String): Boolean {
        val session = db.localSessionDao().getSessionDirect() ?: return false
        val playerId = session.currentPlayerId ?: return false
        val currentProfile = db.playerProfileDao().getProfileById(playerId) ?: return false
        val unlockedList = currentProfile.unlockedBrushIds.split(",")
        if (unlockedList.contains(brushId)) {
            db.playerProfileDao().insertOrUpdateProfile(
                currentProfile.copy(equippedBrushId = brushId, updatedAt = System.currentTimeMillis())
            )
            return true
        }
        return false
    }

    suspend fun unlockBrush(brushId: String): Boolean {
        val session = db.localSessionDao().getSessionDirect() ?: return false
        val playerId = session.currentPlayerId ?: return false
        val currentProfile = db.playerProfileDao().getProfileById(playerId) ?: return false
        val unlockedSet = currentProfile.unlockedBrushIds.split(",").toMutableSet()
        if (!unlockedSet.contains(brushId)) {
            unlockedSet.add(brushId)
            db.playerProfileDao().insertOrUpdateProfile(
                currentProfile.copy(
                    unlockedBrushIds = unlockedSet.joinToString(","),
                    updatedAt = System.currentTimeMillis()
                )
            )
            return true
        }
        return false
    }

    suspend fun getPlayerBrushConfig(playerId: Long, brushId: String): PlayerBrushConfig? {
        return db.playerBrushConfigDao().getConfigDirect(playerId, brushId)
    }

    fun getPlayerBrushConfigFlow(playerId: Long, brushId: String): Flow<PlayerBrushConfig?> {
        return db.playerBrushConfigDao().getConfigFlow(playerId, brushId)
    }

    suspend fun savePlayerBrushConfig(config: PlayerBrushConfig) {
        db.playerBrushConfigDao().insertOrUpdateConfig(config)
    }

    // Codex, Loot and Achievements Systems for V0.5-H
    fun getCodexForPlayerFlow(playerId: Long): Flow<List<MonsterCodexEntry>> {
        return db.monsterCodexDao().getCodexForPlayerFlow(playerId)
    }

    suspend fun getCodexForPlayerDirect(playerId: Long): List<MonsterCodexEntry> {
        return db.monsterCodexDao().getCodexForPlayerDirect(playerId)
    }

    fun getLootRecordsForPlayerFlow(playerId: Long): Flow<List<LootDropRecord>> {
        return db.lootDropDao().getLootRecordsForPlayerFlow(playerId)
    }

    suspend fun getLootRecordsForPlayerDirect(playerId: Long): List<LootDropRecord> {
        return db.lootDropDao().getLootRecordsForPlayerDirect(playerId)
    }

    fun getAchievementsForPlayerFlow(playerId: Long): Flow<List<AchievementRecord>> {
        return db.achievementDao().getAchievementsForPlayerFlow(playerId)
    }

    suspend fun getAchievementsForPlayerDirect(playerId: Long): List<AchievementRecord> {
        return db.achievementDao().getAchievementsForPlayerDirect(playerId)
    }

    private data class MonsterDef(
        val monsterType: String,
        val emoji: String,
        val rarity: String,
        val displayName: String
    )

    data class BattleProcessResult(
        val newMonstersUnlocked: List<MonsterCodexEntry>,
        val newlyUnlockedAchievements: List<AchievementRecord>,
        val drops: List<LootDropRecord>
    )

    suspend fun processBattleResult(
        accountId: Long,
        playerId: Long,
        levelId: Int,
        levelName: String,
        questionResults: List<QuestionResult>,
        finalAccuracy: Double,
        rewardCoins: Int,
        rewardExp: Int,
        highestCombo: Int
    ): BattleProcessResult {
        val newMonsters = mutableListOf<MonsterCodexEntry>()
        val drops = mutableListOf<LootDropRecord>()
        val unlockedAchievements = mutableListOf<AchievementRecord>()

        val wordIds = questionResults.map { it.questionId }
        val wordsMap = db.wordDao().getWordsByIds(wordIds).associateBy { it.id }

        var hasBossPerfect = false
        var hasWrongWordPerfect = false

        questionResults.forEach { qResult ->
            val word = wordsMap[qResult.questionId]
            val isCorrect = qResult.finalResult == "CORRECT"

            val difficulty = word?.difficulty ?: "普通"
            val type = word?.type ?: "字"
            val visibilityPolicy = word?.visibilityPolicy ?: "TEST_SAFE"

            val def = when {
                difficulty == "BOSS" || type == "成语" -> MonsterDef("BOSS", "🐲", "LEGEND", "深渊侵蚀之龙")
                difficulty == "易错" -> MonsterDef("ELITE", "😈", "EPIC", "远古易错领主")
                type == "古诗句" || type == "课文重点句" -> MonsterDef("POEM_GUARD", "📜", "RARE", "千秋墨意守卫")
                visibilityPolicy == "REVIEW_ONLY" -> MonsterDef("WRONG_WORD", "🦹", "UNCOMMON", "噩梦错字之灵")
                else -> MonsterDef("NORMAL", "👾", "COMMON", "迷惘字词魔兽")
            }

            val targetText = qResult.getEffectiveTargetAnswer()
            val monsterKey = "monster_word_${targetText}"
            val sourceType = when (def.monsterType) {
                "BOSS" -> "BOSS挑战"
                "ELITE" -> "易错字词"
                "POEM_GUARD" -> "古诗默写"
                "WRONG_WORD" -> "错题复习"
                else -> "普通听写"
            }

            if (def.monsterType == "BOSS" && isCorrect) {
                hasBossPerfect = true
            }
            if (def.monsterType == "WRONG_WORD" && isCorrect) {
                hasWrongWordPerfect = true
            }

            // 1. Get or create Codex Entry
            val existingEntry = db.monsterCodexDao().getCodexEntry(playerId, monsterKey)
            val now = System.currentTimeMillis()
            
            val newEncounterCount = (existingEntry?.encounterCount ?: 0) + 1
            val newPurifiedCount = (existingEntry?.purifiedCount ?: 0) + (if (isCorrect) 1 else 0)
            
            val wasPurifiedBefore = existingEntry?.isPurified == true
            val isNowPurified = wasPurifiedBefore || isCorrect
            val isNowUnlocked = true // Any encounter unlocks it

            val relatedWordFull = targetText
            val relatedWordMasked = targetText.map { if (it.isLetterOrDigit()) '?' else it }.joinToString("")

            val entry = MonsterCodexEntry(
                id = existingEntry?.id ?: 0,
                accountId = accountId,
                playerId = playerId,
                monsterKey = monsterKey,
                monsterName = def.displayName,
                monsterType = def.monsterType,
                rarity = def.rarity,
                sourceType = sourceType,
                relatedWordMasked = relatedWordMasked,
                relatedWordFullForParent = relatedWordFull,
                encounterCount = newEncounterCount,
                purifiedCount = newPurifiedCount,
                bestCombo = maxOf(existingEntry?.bestCombo ?: 0, highestCombo),
                bestAccuracy = maxOf(existingEntry?.bestAccuracy ?: 0.0, if (isCorrect) 1.0 else 0.0),
                firstEncounterAt = existingEntry?.firstEncounterAt ?: now,
                lastEncounterAt = now,
                isUnlocked = isNowUnlocked,
                isPurified = isNowPurified
            )

            db.monsterCodexDao().insertOrUpdateCodexEntry(entry)

            val newlyPurified = isCorrect && !wasPurifiedBefore
            val newlyDiscovered = existingEntry == null
            if (newlyDiscovered || newlyPurified) {
                newMonsters.add(entry)
            }
        }

        // 2. Process Loot Drops
        val battleId = "level_${levelId}_${System.currentTimeMillis()}"
        
        // Base rewards
        val wordShardAmount = (1..3).random()
        val wordShard = LootDropRecord(
            accountId = accountId,
            playerId = playerId,
            sourceBattleId = battleId,
            lootType = "WORD_SHARD",
            lootKey = "word_shard",
            lootName = "字灵勋章碎片",
            amount = wordShardAmount,
            rarity = "COMMON"
        )
        db.lootDropDao().insertLootRecord(wordShard)
        drops.add(wordShard)
        addLootToInventory(accountId, playerId, "WORD_SHARD", wordShardAmount)

        if (rewardCoins > 0) {
            val coinDrop = LootDropRecord(
                accountId = accountId,
                playerId = playerId,
                sourceBattleId = battleId,
                lootType = "GOLD",
                lootKey = "gold_coins",
                lootName = "金币",
                amount = rewardCoins,
                rarity = "COMMON"
            )
            db.lootDropDao().insertLootRecord(coinDrop)
            drops.add(coinDrop)
        }

        if (rewardExp > 0) {
            val expDrop = LootDropRecord(
                accountId = accountId,
                playerId = playerId,
                sourceBattleId = battleId,
                lootType = "EXP",
                lootKey = "exp_points",
                lootName = "历练经验",
                amount = rewardExp,
                rarity = "COMMON"
            )
            db.lootDropDao().insertLootRecord(expDrop)
            drops.add(expDrop)
        }

        // Perfect Accuracy
        if (finalAccuracy >= 1.0) {
            val extraCoins = LootDropRecord(
                accountId = accountId,
                playerId = playerId,
                sourceBattleId = battleId,
                lootType = "GOLD",
                lootKey = "gold_coins_perfect",
                lootName = "完美讨伐金币加成",
                amount = 50,
                rarity = "RARE"
            )
            db.lootDropDao().insertLootRecord(extraCoins)
            drops.add(extraCoins)
            
            db.playerProfileDao().addPlayerCoins(playerId, 50)

            val hatchEnergy = LootDropRecord(
                accountId = accountId,
                playerId = playerId,
                sourceBattleId = battleId,
                lootType = "HATCH_ENERGY",
                lootKey = "pet_hatch_energy",
                lootName = "宠物灵力结晶",
                amount = 10,
                rarity = "RARE"
            )
            db.lootDropDao().insertLootRecord(hatchEnergy)
            drops.add(hatchEnergy)

            val activePet = db.petBindingDao().getActivePetBindingDirect(playerId)
            if (activePet != null) {
                val updatedPet = activePet.copy(
                    hatchProgress = (activePet.hatchProgress + 10).coerceAtMost(100),
                    growthExp = activePet.growthExp + 10,
                    updatedAt = System.currentTimeMillis()
                )
                db.petBindingDao().insertOrUpdateBinding(updatedPet)
            }

            if (Math.random() < 0.3) {
                val brushShard = LootDropRecord(
                    accountId = accountId,
                    playerId = playerId,
                    sourceBattleId = battleId,
                    lootType = "BRUSH_SHARD",
                    lootKey = "brush_shard_wooden",
                    lootName = "画道神笔碎片",
                    amount = 1,
                    rarity = "EPIC"
                )
                db.lootDropDao().insertLootRecord(brushShard)
                drops.add(brushShard)
                addLootToInventory(accountId, playerId, "BRUSH_SHARD", 1)
            }
        }

        // Combo >= 5
        if (highestCombo >= 5) {
            val comboShard = LootDropRecord(
                accountId = accountId,
                playerId = playerId,
                sourceBattleId = battleId,
                lootType = "COMBO_SHARD",
                lootKey = "combo_mark_shard",
                lootName = "连击徽记碎片",
                amount = 1,
                rarity = "RARE"
            )
            db.lootDropDao().insertLootRecord(comboShard)
            drops.add(comboShard)
            addLootToInventory(accountId, playerId, "COMBO_SHARD", 1)
        }

        // BOSS Perfect
        if (hasBossPerfect) {
            val bossShard = LootDropRecord(
                accountId = accountId,
                playerId = playerId,
                sourceBattleId = battleId,
                lootType = "BOSS_SHARD",
                lootKey = "boss_dragon_scale",
                lootName = "深渊古龙逆鳞",
                amount = 1,
                rarity = "LEGENDARY"
            )
            db.lootDropDao().insertLootRecord(bossShard)
            drops.add(bossShard)
            addLootToInventory(accountId, playerId, "BOSS_SHARD", 1)
        }

        // Wrong Word Purified
        if (hasWrongWordPerfect) {
            val jinghua = LootDropRecord(
                accountId = accountId,
                playerId = playerId,
                sourceBattleId = battleId,
                lootType = "JINGHUA",
                lootKey = "jinghua_crystal",
                lootName = "太清净化结晶",
                amount = 1,
                rarity = "RARE"
            )
            db.lootDropDao().insertLootRecord(jinghua)
            drops.add(jinghua)
            addLootToInventory(accountId, playerId, "JINGHUA", 1)
        }

        // 3. Process Achievements
        val achievementKeys = listOf(
            "first_purification" to Triple("初次净化", "第一次击败并净化任意字词魔物", 1),
            "combo_3" to Triple("连击新手", "在一次讨伐中达成3连击", 3),
            "combo_5" to Triple("连击大师", "在一次讨伐中达成5连击", 5),
            "error_hunter_3" to Triple("错题猎手", "成功净化3个错题魔物", 3),
            "perfect_conquest" to Triple("完美讨伐", "以100%正确率完成一关", 1),
            "codex_collector_10" to Triple("图鉴收藏家", "解锁并净化10个魔物图鉴", 10)
        )

        val allAchievements = db.achievementDao().getAchievementsForPlayerDirect(playerId).associateBy { it.achievementKey }
        val totalPurifiedCount = db.monsterCodexDao().getCodexForPlayerDirect(playerId).sumOf { it.purifiedCount }
        val uniquePurifiedCount = db.monsterCodexDao().getCodexForPlayerDirect(playerId).count { it.isPurified }
        val wrongWordsPurified = db.monsterCodexDao().getCodexForPlayerDirect(playerId).filter { it.monsterType == "WRONG_WORD" }.sumOf { it.purifiedCount }

        achievementKeys.forEach { (key, info) ->
            val existing = allAchievements[key]
            val (name, desc, target) = info

            if (existing?.isUnlocked == true) return@forEach

            var currentProgress = existing?.progressValue ?: 0
            var isUnlockedNow = false

            when (key) {
                "first_purification" -> {
                    currentProgress = if (totalPurifiedCount >= 1) 1 else 0
                    isUnlockedNow = currentProgress >= target
                }
                "combo_3" -> {
                    currentProgress = maxOf(currentProgress, if (highestCombo >= 3) 3 else 0)
                    isUnlockedNow = currentProgress >= target
                }
                "combo_5" -> {
                    currentProgress = maxOf(currentProgress, if (highestCombo >= 5) 5 else 0)
                    isUnlockedNow = currentProgress >= target
                }
                "error_hunter_3" -> {
                    currentProgress = wrongWordsPurified
                    isUnlockedNow = currentProgress >= target
                }
                "perfect_conquest" -> {
                    currentProgress = if (finalAccuracy >= 1.0) 1 else currentProgress
                    isUnlockedNow = currentProgress >= target
                }
                "codex_collector_10" -> {
                    currentProgress = uniquePurifiedCount
                    isUnlockedNow = currentProgress >= target
                }
            }

            val updatedRecord = AchievementRecord(
                id = existing?.id ?: 0,
                accountId = accountId,
                playerId = playerId,
                achievementKey = key,
                achievementName = name,
                achievementDesc = desc,
                progressValue = currentProgress.coerceAtMost(target),
                targetValue = target,
                isUnlocked = isUnlockedNow,
                unlockedAt = if (isUnlockedNow && existing?.isUnlocked != true) System.currentTimeMillis() else existing?.unlockedAt,
                rewardCoins = when (key) {
                    "first_purification" -> 50
                    "combo_3" -> 30
                    "combo_5" -> 80
                    "error_hunter_3" -> 100
                    "perfect_conquest" -> 100
                    "codex_collector_10" -> 200
                    else -> 50
                }
            )

            db.achievementDao().insertOrUpdateAchievement(updatedRecord)

            if (isUnlockedNow && existing?.isUnlocked != true) {
                unlockedAchievements.add(updatedRecord)
                db.playerProfileDao().addPlayerCoins(playerId, updatedRecord.rewardCoins)
            }
        }

        return BattleProcessResult(
            newMonstersUnlocked = newMonsters,
            newlyUnlockedAchievements = unlockedAchievements,
            drops = drops
        )
    }

    // --- V0.5-I: 冒险商店 & 背包仓库 业务逻辑层 ---

    fun getInventoryForPlayerFlow(playerId: Long): Flow<List<PlayerInventoryItem>> {
        return db.playerInventoryDao().getInventoryForPlayerFlow(playerId)
    }

    suspend fun getInventoryForPlayerDirect(playerId: Long): List<PlayerInventoryItem> {
        return db.playerInventoryDao().getInventoryForPlayerDirect(playerId)
    }

    suspend fun initializeDefaultInventoryForPlayer(accountId: Long, playerId: Long) {
        val defaultItems = listOf(
            Triple("default_black", "BRUSH", true),
            Triple("title_newbie", "TITLE", true),
            Triple("theme_default", "CAMP_THEME", true)
        )
        defaultItems.forEach { (itemId, itemType, isEquipped) ->
            val existing = db.playerInventoryDao().getInventoryItem(playerId, itemId)
            if (existing == null) {
                db.playerInventoryDao().insertOrUpdateInventoryItem(
                    PlayerInventoryItem(
                        accountId = accountId,
                        playerId = playerId,
                        itemId = itemId,
                        itemType = itemType,
                        amount = 1,
                        isOwned = true,
                        isEquipped = isEquipped,
                        obtainedAt = System.currentTimeMillis(),
                        updatedAt = System.currentTimeMillis()
                    )
                )
            }
        }
    }

    suspend fun addLootToInventory(accountId: Long, playerId: Long, lootType: String, amount: Int) {
        val itemId = when (lootType) {
            "WORD_SHARD" -> "WORD_SHARD"
            "BRUSH_SHARD" -> "BRUSH_SHARD"
            "COMBO_SHARD" -> "COMBO_SHARD"
            "JINGHUA" -> "JINGHUA"
            "BOSS_SHARD" -> "BOSS_SHARD"
            else -> return
        }
        val existing = db.playerInventoryDao().getInventoryItem(playerId, itemId)
        if (existing != null) {
            db.playerInventoryDao().insertOrUpdateInventoryItem(
                existing.copy(
                    amount = existing.amount + amount,
                    updatedAt = System.currentTimeMillis()
                )
            )
        } else {
            db.playerInventoryDao().insertOrUpdateInventoryItem(
                PlayerInventoryItem(
                    accountId = accountId,
                    playerId = playerId,
                    itemId = itemId,
                    itemType = "MATERIAL",
                    amount = amount,
                    isOwned = true,
                    isEquipped = false,
                    obtainedAt = System.currentTimeMillis(),
                    updatedAt = System.currentTimeMillis()
                )
            )
        }
    }

    suspend fun purchaseItem(accountId: Long, playerId: Long, itemId: String): Result<String> {
        val itemDef = ItemDefinition.ALL_ITEMS.firstOrNull { it.itemId == itemId }
            ?: return Result.failure(Exception("商品定义不存在"))

        val profile = db.playerProfileDao().getProfileById(playerId)
            ?: return Result.failure(Exception("未找到该角色档案"))

        // Check ownership
        val existing = db.playerInventoryDao().getInventoryItem(playerId, itemId)
        if (existing != null && existing.isOwned) {
            return Result.failure(Exception("你已经拥有该商品啦！"))
        }

        // Check coins
        if (profile.coins < itemDef.priceCoins) {
            return Result.failure(Exception("金币不足，去完成冒险任务吧！"))
        }

        // Check shards/materials
        if (itemDef.priceShardType != null && itemDef.priceShardAmount > 0) {
            val shardItem = db.playerInventoryDao().getInventoryItem(playerId, itemDef.priceShardType)
            val ownedAmount = shardItem?.amount ?: 0
            if (ownedAmount < itemDef.priceShardAmount) {
                val shardName = when (itemDef.priceShardType) {
                    "WORD_SHARD" -> "字灵勋章碎片"
                    "BRUSH_SHARD" -> "画道神笔碎片"
                    "COMBO_SHARD" -> "连击徽记碎片"
                    "JINGHUA" -> "太清净化结晶"
                    else -> itemDef.priceShardType
                }
                return Result.failure(Exception("还缺 ${itemDef.priceShardAmount - ownedAmount} 个 $shardName。"))
            }
        }

        // Check unlock conditions
        if (itemDef.unlockConditionType != null && itemDef.unlockConditionValue != null) {
            val conditionMet = when (itemDef.unlockConditionType) {
                "ACHIEVE" -> {
                    val ach = db.achievementDao().getAchievementByKey(playerId, itemDef.unlockConditionValue)
                    ach?.isUnlocked == true
                }
                "MONSTER" -> {
                    val countRequired = itemDef.unlockConditionValue.toIntOrNull() ?: 0
                    val codexList = db.monsterCodexDao().getCodexForPlayerDirect(playerId)
                    val purifiedWrongWords = codexList.filter { it.monsterType == "WRONG_WORD" && it.isPurified }.size
                    purifiedWrongWords >= countRequired
                }
                "PET_STAGE" -> {
                    val activePet = db.petBindingDao().getActivePetBindingDirect(playerId)
                    val intimacyRequired = itemDef.unlockConditionValue.toIntOrNull() ?: 50
                    activePet != null && (activePet.lifeStage != "EGG" && activePet.lifeStage != "SOUL_SLEEP" || activePet.intimacy >= intimacyRequired)
                }
                else -> true
            }
            if (!conditionMet) {
                val msg = when (itemDef.unlockConditionType) {
                    "ACHIEVE" -> "未满足解锁条件：需先达成指定冒险成就！"
                    "MONSTER" -> "未满足解锁条件：需先净化 ${itemDef.unlockConditionValue} 个错题魔物！"
                    "PET_STAGE" -> "未满足解锁条件：需激活宠物契约且亲密度达到 ${itemDef.unlockConditionValue}！"
                    else -> "解锁条件未满足！"
                }
                return Result.failure(Exception(msg))
            }
        }

        // Deduct Coins
        db.playerProfileDao().addPlayerCoins(playerId, -itemDef.priceCoins)

        // Deduct Shards
        if (itemDef.priceShardType != null && itemDef.priceShardAmount > 0) {
            val shardItem = db.playerInventoryDao().getInventoryItem(playerId, itemDef.priceShardType)
            if (shardItem != null) {
                db.playerInventoryDao().insertOrUpdateInventoryItem(
                    shardItem.copy(
                        amount = maxOf(0, shardItem.amount - itemDef.priceShardAmount),
                        updatedAt = System.currentTimeMillis()
                    )
                )
            }
        }

        // Add item to inventory
        db.playerInventoryDao().insertOrUpdateInventoryItem(
            PlayerInventoryItem(
                accountId = accountId,
                playerId = playerId,
                itemId = itemId,
                itemType = itemDef.itemType,
                amount = 1,
                isOwned = true,
                isEquipped = false,
                obtainedAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )
        )

        return Result.success("购买成功！可在背包中查看并装备。")
    }

    suspend fun equipItem(playerId: Long, itemId: String): Result<String> {
        val itemDef = ItemDefinition.ALL_ITEMS.firstOrNull { it.itemId == itemId }
            ?: return Result.failure(Exception("物品定义不存在"))

        val existing = db.playerInventoryDao().getInventoryItem(playerId, itemId)
        if (existing == null || !existing.isOwned) {
            return Result.failure(Exception("你还没有拥有该物品，无法装备。"))
        }

        // Unequip all of this type
        db.playerInventoryDao().unequipAllOfType(playerId, itemDef.itemType)

        // Equip this item
        db.playerInventoryDao().insertOrUpdateInventoryItem(
            existing.copy(isEquipped = true, updatedAt = System.currentTimeMillis())
        )

        // Synchronize with PlayerProfile / existing brush setups
        if (itemDef.itemType == "BRUSH") {
            val profile = db.playerProfileDao().getProfileById(playerId)
            if (profile != null) {
                val currentUnlocked = profile.unlockedBrushIds.split(",").toMutableSet()
                currentUnlocked.add(itemId)
                db.playerProfileDao().insertOrUpdateProfile(
                    profile.copy(
                        equippedBrushId = itemId,
                        unlockedBrushIds = currentUnlocked.joinToString(","),
                        updatedAt = System.currentTimeMillis()
                    )
                )
            }
        }

        return Result.success("装备成功！")
    }

    suspend fun unequipItem(playerId: Long, itemId: String): Result<String> {
        val existing = db.playerInventoryDao().getInventoryItem(playerId, itemId)
            ?: return Result.failure(Exception("物品不存在"))

        db.playerInventoryDao().insertOrUpdateInventoryItem(
            existing.copy(isEquipped = false, updatedAt = System.currentTimeMillis())
        )

        val itemDef = ItemDefinition.ALL_ITEMS.firstOrNull { it.itemId == itemId }
        if (itemDef?.itemType == "BRUSH") {
            val profile = db.playerProfileDao().getProfileById(playerId)
            if (profile != null) {
                db.playerProfileDao().insertOrUpdateProfile(
                    profile.copy(
                        equippedBrushId = "default_black",
                        updatedAt = System.currentTimeMillis()
                    )
                )
            }
        }

        return Result.success("卸下装备成功！")
    }
}
