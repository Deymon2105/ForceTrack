package com.example.forcetrack.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.forcetrack.database.repository.ForceTrackRepository

class ViewModelFactory(private val repository: ForceTrackRepository) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(AuthViewModel::class.java) -> AuthViewModel(repository) as T
            modelClass.isAssignableFrom(BloquesViewModel::class.java) -> BloquesViewModel(repository) as T
            modelClass.isAssignableFrom(SplitViewModel::class.java) -> SplitViewModel(repository) as T // Añadido
            modelClass.isAssignableFrom(RutinaViewModel::class.java) -> RutinaViewModel(repository) as T
            modelClass.isAssignableFrom(TrainingLogViewModel::class.java) -> TrainingLogViewModel(repository) as T
            modelClass.isAssignableFrom(EjerciciosViewModel::class.java) -> EjerciciosViewModel(repository) as T
            else -> throw IllegalArgumentException("Clase de ViewModel Desconocida: ${modelClass.name}")
        }
    }
}
