package cow.management.cowmanagementservice.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import cow.management.cowmanagementservice.repository.CowRepository

/**
 * Factory for creating ViewModels that have a CowRepository dependency.
 */
class ViewModelFactory(private val cowRepository: CowRepository) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CowListViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CowListViewModel(cowRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}