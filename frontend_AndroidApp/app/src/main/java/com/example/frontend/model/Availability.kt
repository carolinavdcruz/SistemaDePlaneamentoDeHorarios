data class Availability(
    //val id: UUID,
    val ownerName: String,
    val ownerType: OwnerType,
    val dayOfWeek: Int,
    val startTime: LocalTime,
    val endTime: LocalTime
)