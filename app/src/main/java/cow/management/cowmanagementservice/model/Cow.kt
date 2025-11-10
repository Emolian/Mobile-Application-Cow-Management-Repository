package cow.management.cowmanagementservice.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import java.time.LocalDate

// Add missing imports
import cow.management.cowmanagementservice.model.Birth
import cow.management.cowmanagementservice.model.Breed
import cow.management.cowmanagementservice.model.Category
import cow.management.cowmanagementservice.model.Sex

@Entity(
    tableName = "cows",
    foreignKeys = [
        ForeignKey(
            entity = Birth::class,
            parentColumns = ["id"],
            childColumns = ["birthId"],
            onDelete = ForeignKey.SET_NULL
        )
    ]
)
data class Cow(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val earTag: String,
    val sex: Sex,
    val breed: Breed,
    val category: Category,
    val birthDate: LocalDate,
    val entryDate: LocalDate,
    val exitDate: LocalDate? = null,
    val motherId: Long? = null,
    val birthId: Long? = null
)
