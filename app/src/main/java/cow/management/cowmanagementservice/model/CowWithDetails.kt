package cow.management.cowmanagementservice.model

import androidx.room.Embedded
import androidx.room.Relation

data class CowWithDetails(
    @Embedded val cow: Cow,
    @Relation(
        parentColumn = "id",
        entityColumn = "cowId"
    )
    val inseminations: List<ArtificialInsemination>,
    @Relation(
        parentColumn = "id",
        entityColumn = "motherId"
    )
    val births: List<Birth>
)
