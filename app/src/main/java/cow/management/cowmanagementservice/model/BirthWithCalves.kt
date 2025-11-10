package cow.management.cowmanagementservice.model

import androidx.room.Embedded
import androidx.room.Relation

data class BirthWithCalves(
    @Embedded val birth: Birth,
    @Relation(
        parentColumn = "id",
        entityColumn = "birthId"
    )
    val calves: List<Cow>
)
