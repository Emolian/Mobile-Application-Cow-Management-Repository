package cow.management.cowmanagementservice.repository

import androidx.room.Transaction
import cow.management.cowmanagementservice.database.dao.ArtificialInseminationDao
import cow.management.cowmanagementservice.database.dao.BirthDao
import cow.management.cowmanagementservice.database.dao.CowDao
import cow.management.cowmanagementservice.model.ArtificialInsemination
import cow.management.cowmanagementservice.model.Birth
import cow.management.cowmanagementservice.model.BirthWithCalves
import cow.management.cowmanagementservice.model.Cow
import cow.management.cowmanagementservice.model.CowWithDetails
import cow.management.cowmanagementservice.model.InseminationWithCow
import cow.management.cowmanagementservice.model.Sex
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

class CowRepository(
    private val cowDao: CowDao,
    private val birthDao: BirthDao,
    private val artificialInseminationDao: ArtificialInseminationDao
) {

    fun getAllCowsWithDetails(): Flow<List<CowWithDetails>> {
        return cowDao.getAllCowsWithDetails()
    }

    fun getCowWithDetails(cowId: Long): Flow<CowWithDetails> {
        return cowDao.getCowWithDetails(cowId)
    }

    suspend fun getCowsByBirthDate(date: LocalDate): List<Cow> {
        return cowDao.getCowsByBirthDate(date)
    }

    fun getBirthsWithCalves(motherId: Long): Flow<List<BirthWithCalves>> {
        return birthDao.getBirthsWithCalves(motherId)
    }

    fun getInseminationsBySire(sireId: Long): Flow<List<InseminationWithCow>> {
        return artificialInseminationDao.getInseminationsBySire(sireId)
    }

    suspend fun insertCow(cow: Cow) {
        cowDao.insertCow(cow)
    }

    @Transaction
    suspend fun updateCowAndHandleSexChange(updatedCow: Cow) {
        val originalCow = cowDao.getCowById(updatedCow.id)
        if (originalCow != null && originalCow.sex != updatedCow.sex) {
            when (originalCow.sex) {
                Sex.FEMALE -> {
                    birthDao.deleteBirthsByCowId(originalCow.id)
                    artificialInseminationDao.deleteInseminationsByCowId(originalCow.id)
                }
                Sex.MALE -> {
                    // The logic to remove sire from calves is no longer needed.
                }
            }
        }
        cowDao.updateCow(updatedCow)
    }

    suspend fun deleteCow(cow: Cow) {
        cowDao.deleteCow(cow)
    }

    suspend fun insertBirth(birth: Birth) {
        birthDao.insertBirth(birth)
    }

    suspend fun deleteAndDissociate(birth: Birth) {
        birthDao.deleteAndDissociate(birth)
    }

    suspend fun updateBirthAndCalves(birth: Birth, addedCalves: List<Cow>, removedCalves: List<Cow>) {
        birthDao.updateBirthAndCalves(birth, addedCalves, removedCalves)
    }

    suspend fun insertInsemination(insemination: ArtificialInsemination) {
        artificialInseminationDao.insertInsemination(insemination)
    }

    suspend fun updateInsemination(insemination: ArtificialInsemination) {
        artificialInseminationDao.updateInsemination(insemination)
    }

    suspend fun deleteInsemination(insemination: ArtificialInsemination) {
        artificialInseminationDao.deleteInsemination(insemination)
    }
}