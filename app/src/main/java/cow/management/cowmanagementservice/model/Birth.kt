package cow.management.cowmanagementservice.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import java.time.LocalDate

@Entity(
    tableName = "births",
    foreignKeys = [
        ForeignKey(
            entity = Cow::class,
            parentColumns = ["id"],
            childColumns = ["motherId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class Birth(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val date: LocalDate,
    val motherId: Long // The cow that gave birth
)
