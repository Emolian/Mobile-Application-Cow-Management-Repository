package cow.management.cowmanagementservice

import android.app.Application
import cow.management.cowmanagementservice.database.AppDatabase
import cow.management.cowmanagementservice.repository.CowRepository

/**
 * Custom Application class to provide a single instance of the CowRepository.
 */
class CowManagementApplication : Application() {

    // Using lazy initialization to create the database and repository only when needed.
    private val database by lazy { AppDatabase.getInstance(this) }
    val repository by lazy { CowRepository(database.cowDao(), database.birthDao(), database.artificialInseminationDao()) }
}