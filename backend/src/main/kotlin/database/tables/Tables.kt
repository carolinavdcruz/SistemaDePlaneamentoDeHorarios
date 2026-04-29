package database.tables

import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.javatime.time

object TeacherTable : IntIdTable("teacher") {
    val name       = varchar("name", 100)
    val email      = varchar("email", 150).uniqueIndex()
    val sessionDurationMinutes      = integer("session_duration_minutes").default(60)
    val maxParticipantsPerSession   = integer("max_participants_per_session").default(5)
}

object StudentTable : IntIdTable("student") {
    val name             = varchar("name", 100)
    val email            = varchar("email", 150).uniqueIndex()
    val maxDailySessions = integer("max_daily_sessions").default(1)
}

object TimeSlotTable : IntIdTable("timeslots") {
    val dayOfWeek = integer("day_of_week")
    val startTime = time("start_time")
    val endTime   = time("end_time")
}

object AvailabilityTable : IntIdTable("availability") {
    val teacherId = reference("teacher_id", TeacherTable).nullable()
    val studentId = reference("student_id", StudentTable).nullable()
    val dayOfWeek = integer("day_of_week")
    val startTime = time("start_time")
    val endTime   = time("end_time")
}