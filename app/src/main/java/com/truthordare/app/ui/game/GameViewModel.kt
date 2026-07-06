package com.truthordare.app.ui.game

import android.app.Application
import androidx.lifecycle.*
import com.truthordare.app.data.model.*
import com.truthordare.app.data.repository.LibraryRepository
import com.truthordare.app.data.repository.SessionRepository
import kotlinx.coroutines.launch

enum class GameMode { TURN_BASED, TARGETED }

class GameViewModel(app: Application) : AndroidViewModel(app) {
    private val libraryRepo = LibraryRepository(app)
    private val sessionRepo = SessionRepository(app)

    val libraries = libraryRepo.allLibraries

    private val _selectedLibraryId = MutableLiveData<Long>(-1L)
    val selectedLibraryId: LiveData<Long> = _selectedLibraryId

    private val _maxLevel = MutableLiveData(3)
    val maxLevel: LiveData<Int> = _maxLevel

    private val _selectedTag = MutableLiveData(CardTag.ALL)
    val selectedTag: LiveData<CardTag> = _selectedTag

    private val _gameMode = MutableLiveData(GameMode.TURN_BASED)
    val gameMode: LiveData<GameMode> = _gameMode

    private val _currentCard = MutableLiveData<Card?>()
    val currentCard: LiveData<Card?> = _currentCard

    private val _isFlipped = MutableLiveData(false)
    val isFlipped: LiveData<Boolean> = _isFlipped

    private val _drawnRecords = MutableLiveData<MutableList<DrawnCardRecord>>(mutableListOf())
    val drawnRecords: LiveData<MutableList<DrawnCardRecord>> = _drawnRecords

    private var currentSessionId: Long = -1L
    private var playerNames = listOf("玩家A", "玩家B")
    private var turnIndex = 0

    fun selectLibrary(id: Long) {
        _selectedLibraryId.value = id
        startNewSession(id)
    }

    private fun startNewSession(libraryId: Long) {
        viewModelScope.launch {
            val libs = libraries.value ?: return@launch
            val lib = libs.find { it.id == libraryId } ?: return@launch
            currentSessionId = sessionRepo.startSession(libraryId, lib.name)
            _drawnRecords.value = mutableListOf()
            turnIndex = 0
        }
    }

    fun setMaxLevel(level: Int) { _maxLevel.value = level.coerceIn(1, 5) }

    fun setTag(tag: CardTag) { _selectedTag.value = tag }

    fun toggleGameMode() {
        _gameMode.value = if (_gameMode.value == GameMode.TURN_BASED) GameMode.TARGETED else GameMode.TURN_BASED
    }

    fun draw() {
        val libId = _selectedLibraryId.value ?: return
        if (libId < 0) return
        val level = _maxLevel.value ?: 3
        val tag = _selectedTag.value ?: CardTag.ALL
        viewModelScope.launch {
            val card = libraryRepo.drawCard(libId, level, tag.name)
            _currentCard.value = card
            _isFlipped.value = false
            card?.let {
                val player = if (_gameMode.value == GameMode.TURN_BASED)
                    playerNames.getOrElse(turnIndex % playerNames.size) { "玩家${turnIndex + 1}" }
                else ""
                turnIndex++
                val record = DrawnCardRecord(
                    content = it.content,
                    type = it.type.label,
                    level = it.level,
                    playerName = player
                )
                val list = _drawnRecords.value ?: mutableListOf()
                list.add(0, record)
                _drawnRecords.value = list
            }
        }
    }

    fun flipCard() { _isFlipped.value = true }

    fun setPlayers(names: List<String>) { playerNames = names }

    fun saveCurrentSession(memo: String) {
        if (currentSessionId < 0) return
        viewModelScope.launch {
            sessionRepo.saveSession(currentSessionId, memo, _drawnRecords.value ?: emptyList())
        }
    }

    fun clearCurrentSession() {
        _drawnRecords.value = mutableListOf()
        _currentCard.value = null
        _isFlipped.value = false
        turnIndex = 0
    }

    init {
        viewModelScope.launch {
            val defaultId = libraryRepo.getDefaultLibraryId()
            if (defaultId != null) _selectedLibraryId.value = defaultId
        }
    }
}
