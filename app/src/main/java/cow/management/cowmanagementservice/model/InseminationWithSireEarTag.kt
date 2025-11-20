package cow.management.cowmanagementservice.model

import androidx.room.Embedded

data class InseminationWithSireEarTag(
    @Embedded val insemination: ArtificialInsemination,
    val sireEarTag: String?
)
