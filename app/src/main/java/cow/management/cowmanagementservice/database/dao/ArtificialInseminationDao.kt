package cow.management.cowmanagementservice.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import cow.management.cowmanagementservice.model.ArtificialInsemination
import cow.management.cowmanagementservice.model.InseminationWithCow
import kotlinx.coroutines.flow.Flow

@Dao
interface ArtificialInseminationDao {

    @Insert
    suspend fun insertInsemination(insemination: ArtificialInsemination): Long

    @Update
    suspend fun updateInsemination(insemination: ArtificialInsemination)

    @Delete
    suspend fun deleteInsemination(insemination: ArtificialInsemination)

    @Query("DELETE FROM artificial_inseminations WHERE cowId = :cowId")
    suspend fun deleteInseminationsByCowId(cowId: Long)

    @Transaction
    @Query("SELECT * FROM artificial_inseminations WHERE sireId = :sireId")
    fun getInseminationsBySire(sireId: Long): Flow<List<InseminationWithCow>>
}
