package com.truthordare.app.ui.library

import android.app.Application
import androidx.lifecycle.*
import com.truthordare.app.data.model.Card
import com.truthordare.app.data.model.CardLibrary
import com.truthordare.app.data.repository.LibraryRepository
import kotlinx.coroutines.launch

class LibraryViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = LibraryRepository(app)
    val libraries = repo.allLibraries

    private val _selectedLibraryId = MutableLiveData<Long>(-1L)
    val selectedLibraryId: LiveData<Long> = _selectedLibraryId

    val cardsForSelected: LiveData<List<Card>> = _selectedLibraryId.switchMap { id ->
        if (id > 0) repo.getCardsForLibrary(id)
        else MutableLiveData(emptyList())
    }

    fun selectLibrary(id: Long) { _selectedLibraryId.value = id }

    fun createLibrary(name: String, emoji: String) = viewModelScope.launch {
        repo.createLibrary(name, emoji)
    }

    fun renameLibrary(library: CardLibrary, newName: String) = viewModelScope.launch {
        repo.updateLibrary(library.copy(name = newName))
    }

    fun deleteLibrary(library: CardLibrary) = viewModelScope.launch {
        repo.deleteLibrary(library)
    }

    fun copyLibrary(id: Long, newName: String) = viewModelScope.launch {
        repo.copyLibrary(id, newName)
    }

    fun setDefaultLibrary(id: Long) = viewModelScope.launch {
        repo.setDefaultLibrary(id)
    }

    fun addCard(card: Card) = viewModelScope.launch { repo.addCard(card) }

    fun updateCard(card: Card) = viewModelScope.launch { repo.updateCard(card) }

    fun deleteCard(card: Card) = viewModelScope.launch { repo.deleteCard(card) }

    fun unlockLibrary(id: Long) = viewModelScope.launch { repo.unlockLibrary(id) }

    fun importFromCsv(csv: String, libraryName: String) = viewModelScope.launch {
        repo.importFromCsv(csv, libraryName)
    }

    suspend fun exportShortCode(libraryId: Long): String = repo.exportLibraryAsShortCode(libraryId)

    fun importFromShortCode(code: String) = viewModelScope.launch {
        repo.importFromShortCode(code)
    }
}
