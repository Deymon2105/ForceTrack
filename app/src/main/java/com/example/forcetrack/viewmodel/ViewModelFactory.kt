package com.example.forcetrack.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.forcetrack.database.repository.ForceTrackRepository
import com.example.forcetrack.network.sync.SyncService

class ViewModelFactory(
    private val repository: ForceTrackRepository,
    private val sessionManager: com.example.forcetrack.SessionManager,
    private val syncService: SyncService? = null
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(AuthViewModel::class.java) -> AuthViewModel(repository, sessionManager) as T
            modelClass.isAssignableFrom(BloquesViewModel::class.java) -> BloquesViewModel(repository) as T
            modelClass.isAssignableFrom(SplitViewModel::class.java) -> SplitViewModel(repository) as T
            modelClass.isAssignableFrom(RutinaViewModel::class.java) -> RutinaViewModel(repository) as T
            modelClass.isAssignableFrom(TrainingLogViewModel::class.java) -> TrainingLogViewModel(repository) as T
            modelClass.isAssignableFrom(EjerciciosViewModel::class.java) -> EjerciciosViewModel(repository) as T
            modelClass.isAssignableFrom(SyncViewModel::class.java) -> {
                requireNotNull(syncService) { "SyncService es requerido para SyncViewModel" }
                SyncViewModel(syncService) as T
            }
            else -> throw IllegalArgumentException("Clase de ViewModel Desconocida: ${modelClass.name}")
        }
    }
}
