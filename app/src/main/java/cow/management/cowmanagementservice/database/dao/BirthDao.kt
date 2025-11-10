package cow.management.cowmanagementservice.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import cow.management.cowmanagementservice.model.Birth
import cow.management.cowmanagementservice.model.BirthWithCalves
import cow.management.cowmanagementservice.model.Cow
import kotlinx.coroutines.flow.Flow

@Dao
interface BirthDao {

    @Insert
    suspend fun insertBirth(birth: Birth): Long

    @Update
    suspend fun updateBirth(birth: Birth)

    @Query("DELETE FROM births WHERE motherId = :cowId")
    suspend fun deleteBirthsByCowId(cowId: Long)

    @Transaction
    @Query("SELECT * FROM births WHERE motherId = :motherId")
    fun getBirthsWithCalves(motherId: Long): Flow<List<BirthWithCalves>>

    @Transaction
    suspend fun deleteAndDissociate(birth: Birth) {
        val calves = getCalvesOfBirth(birth.id)
        calves.forEach { 
            updateCow(it.copy(birthId = null))
        }
        deleteBirth(birth)
    }

    @Transaction
    suspend fun updateBirthAndCalves(birth: Birth, addedCalves: List<Cow>, removedCalves: List<Cow>) {
        updateBirth(birth)
        addedCalves.forEach { 
            updateCow(it.copy(birthId = birth.id))
        }
        removedCalves.forEach {
            updateCow(it.copy(birthId = null))
        }
    }

    @Query("SELECT * FROM cows WHERE birthId = :birthId")
    suspend fun getCalvesOfBirth(birthId: Long): List<Cow>

    @Delete
    suspend fun deleteBirth(birth: Birth)

    @Update
    suspend fun updateCow(cow: Cow)
}