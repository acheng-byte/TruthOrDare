package com.truthordare.app.ui.history

import android.app.Application
import androidx.lifecycle.*
import com.truthordare.app.data.model.DrawnCardRecord
import com.truthordare.app.data.model.GameSession
import com.truthordare.app.data.repository.SessionRepository
import kotlinx.coroutines.launch

class HistoryViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = SessionRepository(app)
    val savedSessions = repo.savedSessions

    private val _searchQuery = MutableLiveData("")
    val filteredSessions: LiveData<List<GameSession>> = _searchQuery.switchMap { q ->
        if (q.isBlank()) repo.savedSessions else repo.searchSessions(q)
    }

    fun search(query: String) { _searchQuery.value = query }

    fun deleteSession(session: GameSession) = viewModelScope.launch { repo.deleteSession(session) }

    fun clearAllHistory() = viewModelScope.launch { repo.clearAllHistory() }

    suspend fun getRecords(sessionId: Long): List<DrawnCardRecord> = repo.getRecords(sessionId)
}
