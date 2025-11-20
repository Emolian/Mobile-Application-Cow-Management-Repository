package cow.management.cowmanagementservice.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cow.management.cowmanagementservice.model.*
import cow.management.cowmanagementservice.repository.CowRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

class CowListViewModel(private val cowRepository: CowRepository) : ViewModel() {

    val cows: StateFlow<List<CowWithDetails>> = cowRepository.getAllCowsWithDetails()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _birthsWithCalves = MutableStateFlow<List<BirthWithCalves>>(emptyList())
    val birthsWithCalves: StateFlow<List<BirthWithCalves>> = _birthsWithCalves.asStateFlow()

    private val _potentialCalves = MutableStateFlow<List<Cow>>(emptyList())
    val potentialCalves: StateFlow<List<Cow>> = _potentialCalves.asStateFlow()

    private val _inseminationsBySire = MutableStateFlow<List<InseminationWithCow>>(emptyList())
    val inseminationsBySire: StateFlow<List<InseminationWithCow>> = _inseminationsBySire.asStateFlow()
    
    private val _inseminationsForCow = MutableStateFlow<List<InseminationWithSireEarTag>>(emptyList())
    val inseminationsForCow: StateFlow<List<InseminationWithSireEarTag>> = _inseminationsForCow.asStateFlow()

    private val _errorState = MutableStateFlow<String?>(null)
    val errorState: StateFlow<String?> = _errorState.asStateFlow()

    private val _operationSuccess = MutableStateFlow(false)
    val operationSuccess: StateFlow<Boolean> = _operationSuccess.asStateFlow()

    fun loadBirthsForCow(motherId: Long) {
        viewModelScope.launch {
            cowRepository.getBirthsWithCalves(motherId).collect {
                _birthsWithCalves.value = it
            }
        }
    }

    fun loadPotentialCalves(date: LocalDate) {
        viewModelScope.launch {
            _potentialCalves.value = cowRepository.getCowsByBirthDate(date)
        }
    }

    fun loadInseminationsBySire(sireId: Long) {
        viewModelScope.launch {
            cowRepository.getInseminationsBySire(sireId).collect {
                _inseminationsBySire.value = it
            }
        }
    }

    fun loadInseminationsForCow(cowId: Long) {
        viewModelScope.launch {
            cowRepository.getInseminationsWithSireEarTag(cowId).collect {
                _inseminationsForCow.value = it
            }
        }
    }

    suspend fun getEarTagById(id: Long?): String? {
        if (id == null) return null
        return cowRepository.getCowByEarTag(cows.value.find { it.cow.id == id }?.cow?.earTag ?: "")?.earTag
    }

    fun addCow(
        earTag: String, breed: Breed?, birthDate: String, entryDate: String, exitDate: String, sex: Sex?, category: Category?
    ) {
        viewModelScope.launch {
            try {
                if (breed == null || sex == null || category == null || earTag.isBlank() || birthDate.isBlank() || entryDate.isBlank()) {
                    _errorState.value = "Please fill all required fields."
                    return@launch
                }
                val dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")
                val newCow = Cow(
                    earTag = earTag,
                    breed = breed,
                    birthDate = LocalDate.parse(birthDate, dateFormatter),
                    entryDate = LocalDate.parse(entryDate, dateFormatter),
                    exitDate = if (exitDate.isNotBlank()) LocalDate.parse(exitDate, dateFormatter) else null,
                    sex = sex,
                    category = category
                )
                cowRepository.insertCow(newCow)
                _operationSuccess.value = true
            } catch (e: DateTimeParseException) {
                _errorState.value = "Invalid date format. Please use DD.MM.YYYY."
            } catch (e: Exception) {
                _errorState.value = "An unexpected error occurred: ${e.message}"
            }
        }
    }

    fun updateCow(
        originalCow: Cow, earTag: String, breed: Breed?, birthDate: String, entryDate: String, exitDate: String, sex: Sex?, category: Category?
    ) {
        viewModelScope.launch {
            try {
                if (breed == null || sex == null || category == null || earTag.isBlank() || birthDate.isBlank() || entryDate.isBlank()) {
                    _errorState.value = "Please fill all required fields."
                    return@launch
                }
                val dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")
                val updatedCow = originalCow.copy(
                    earTag = earTag,
                    breed = breed,
                    birthDate = LocalDate.parse(birthDate, dateFormatter),
                    entryDate = LocalDate.parse(entryDate, dateFormatter),
                    exitDate = if (exitDate.isNotBlank()) LocalDate.parse(exitDate, dateFormatter) else null,
                    sex = sex,
                    category = category
                )
                cowRepository.updateCowAndHandleSexChange(updatedCow)
                _operationSuccess.value = true
            } catch (e: DateTimeParseException) {
                _errorState.value = "Invalid date format. Please use DD.MM.YYYY."
            } catch (e: Exception) {
                _errorState.value = "An unexpected error occurred: ${e.message}"
            }
        }
    }

    fun deleteCow(cow: Cow) {
        viewModelScope.launch {
            cowRepository.deleteCow(cow)
        }
    }

    fun addBirth(motherId: Long, date: LocalDate) {
        viewModelScope.launch {
            val births = cowRepository.getBirthsWithCalves(motherId).first()
            if (births.any { it.birth.date == date }) {
                _errorState.value = "A birth on this date has already been recorded for this cow."
            } else {
                cowRepository.insertBirth(Birth(date = date, motherId = motherId))
                _operationSuccess.value = true
            }
        }
    }

    fun deleteBirth(birth: Birth) {
        viewModelScope.launch {
            cowRepository.deleteAndDissociate(birth)
        }
    }

    fun updateBirth(birth: Birth, addedCalves: List<Cow>, removedCalves: List<Cow>) {
        viewModelScope.launch {
            cowRepository.updateBirthAndCalves(birth, addedCalves, removedCalves)
            _operationSuccess.value = true
        }
    }

    fun addInsemination(cowId: Long, date: String, sireEarTag: String?) {
        viewModelScope.launch {
            try {
                val sireId = getAndValidateSireId(sireEarTag)
                if (!sireEarTag.isNullOrBlank() && sireId == null) return@launch 

                cowRepository.insertInsemination(ArtificialInsemination(date = LocalDate.parse(date, DateTimeFormatter.ofPattern("dd.MM.yyyy")), cowId = cowId, sireId = sireId))
                _operationSuccess.value = true
            } catch (e: DateTimeParseException) {
                _errorState.value = "Invalid date format. Please use DD.MM.YYYY."
            }
        }
    }

    fun updateInsemination(insemination: ArtificialInsemination, newDate: String, newSireEarTag: String?) {
        viewModelScope.launch {
             try {
                val sireId = getAndValidateSireId(newSireEarTag)
                if (!newSireEarTag.isNullOrBlank() && sireId == null) return@launch
                
                val updatedInsemination = insemination.copy(
                    date = LocalDate.parse(newDate, DateTimeFormatter.ofPattern("dd.MM.yyyy")),
                    sireId = sireId
                )
                cowRepository.updateInsemination(updatedInsemination)
                _operationSuccess.value = true
            } catch (e: DateTimeParseException) {
                _errorState.value = "Invalid date format. Please use DD.MM.YYYY."
            }
        }
    }

    private suspend fun getAndValidateSireId(sireEarTag: String?): Long? {
        if (sireEarTag.isNullOrBlank()) {
            return null
        }
        val sire = cowRepository.getCowByEarTag(sireEarTag)
        if (sire == null) {
            _errorState.value = "Sire with ear tag '$sireEarTag' not found."
            return null
        }
        if (sire.sex != Sex.MALE) {
            _errorState.value = "Cow '$sireEarTag' is not a male."
            return null
        }
        return sire.id
    }

    fun deleteInsemination(insemination: ArtificialInsemination) {
        viewModelScope.launch {
            cowRepository.deleteInsemination(insemination)
        }
    }

    fun onOperationCompleted() {
        _operationSuccess.value = false
    }

    fun onErrorShown() {
        _errorState.value = null
    }
}