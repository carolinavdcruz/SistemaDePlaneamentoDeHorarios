data class User(
    val id: UUID,
    val name: String,
    val email: String,
    val maxDailyHours: Int,
    val sessionDurationMinutes: Int,
    val maxParticipantsPerSession: Int
)