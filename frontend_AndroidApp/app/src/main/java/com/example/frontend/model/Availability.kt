data class Availability(
    val id: UUID,
    val ownerId: UUID,
    val ownerType: OwnerType,
    val dayOfWeek: Int,
    val startTime: LocalTime,
    val endTime: LocalTime
)