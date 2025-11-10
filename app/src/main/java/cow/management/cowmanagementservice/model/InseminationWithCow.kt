package cow.management.cowmanagementservice.model

import androidx.room.Embedded
import androidx.room.Relation

data class InseminationWithCow(
    @Embedded val insemination: ArtificialInsemination,
    @Relation(
        parentColumn = "cowId",
        entityColumn = "id"
    )
    val cow: Cow // The female cow that was inseminated
)
