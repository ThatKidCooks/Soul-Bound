package site.thatkid.soulBound.hearts

import java.util.UUID

data class SavedHeartData(
    val uuid: UUID,
    val hearts: List<String>
)
