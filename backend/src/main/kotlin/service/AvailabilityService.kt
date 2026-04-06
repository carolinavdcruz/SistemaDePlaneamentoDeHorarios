package service



/* import java.time.LocalTime
object AvailabilityService {

    /**
     * Recebe um intervalo (ex: 09:00–11:00) e devolve slots de 1h.
     * Esta é a lógica do Use Case 4.
     */
    fun splitIntoSlots(
        ownerId: Int,
        ownerType: String,
        dayOfWeek: Int,
        startTime: LocalTime,
        endTime: LocalTime,
        slotDurationMinutes: Long = 60L
    ): List<TimeSlotEntity> {
        val slots = mutableListOf<TimeSlotEntity>()
        var current = startTime

        while (current.plusMinutes(slotDurationMinutes) <= endTime) {
            val next = current.plusMinutes(slotDurationMinutes)
            slots.add(
                TimeSlotEntity(
                    dayOfWeek = dayOfWeek,
                    startTime = current,
                    endTime   = next
                )
            )
            current = next
        }
        return slots
    }
}

 */