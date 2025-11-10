package cow.management.cowmanagementservice.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import cow.management.cowmanagementservice.model.Cow
import cow.management.cowmanagementservice.model.CowWithDetails
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
interface CowDao {

    @Insert
    suspend fun insertCow(cow: Cow): Long

    @Update
    suspend fun updateCow(cow: Cow)

    @Delete
    suspend fun deleteCow(cow: Cow)

    @Transaction
    @Query("SELECT * FROM cows WHERE id = :cowId")
    fun getCowWithDetails(cowId: Long): Flow<CowWithDetails>
    
    @Query("SELECT * FROM cows WHERE id = :cowId")
    suspend fun getCowById(cowId: Long): Cow?

    @Transaction
    @Query("SELECT * FROM cows")
    fun getAllCowsWithDetails(): Flow<List<CowWithDetails>>

    @Query("SELECT * FROM cows WHERE birthDate = :date")
    suspend fun getCowsByBirthDate(date: LocalDate): List<Cow>
}
