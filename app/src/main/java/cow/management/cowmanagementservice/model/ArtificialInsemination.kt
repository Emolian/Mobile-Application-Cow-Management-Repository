package cow.management.cowmanagementservice.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import java.time.LocalDate

@Entity(
    tableName = "artificial_inseminations",
    foreignKeys = [
        ForeignKey(
            entity = Cow::class,
            parentColumns = ["id"],
            childColumns = ["cowId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Cow::class,
            parentColumns = ["id"],
            childColumns = ["sireId"],
            onDelete = ForeignKey.SET_NULL
        )
    ]
)
data class ArtificialInsemination(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val date: LocalDate,
    val cowId: Long,
    val sireId: Long?
)
