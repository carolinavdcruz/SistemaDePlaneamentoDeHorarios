package database.tables

import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.javatime.time
import org.jetbrains.exposed.sql.javatime.datetime

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

object RestrictionsTable : IntIdTable("restrictions") {
    val teacherId                   = reference("teacher_id", TeacherTable).uniqueIndex()
    val sessionDurationMinutes      = integer("session_duration_minutes").default(60)
    val maxDailyHours               = integer("max_daily_hours").default(3)
    val maxParticipantsPerSession   = integer("max_participants_per_session").default(5)
    val maxSessionsPerStudentPerDay = integer("max_sessions_per_student_per_day").default(1)
}

object SchedulesTable : IntIdTable("schedules") {
    val teacherId  = reference("teacher_id", TeacherTable)
    val createdAt  = datetime("created_at")
    val status     = varchar("status", 20).default("CREATED") // CREATED, ACCEPTED, REJECTED
}

object SessionsTable : IntIdTable("sessions") {
    val scheduleId  = reference("schedule_id", SchedulesTable)
    val timeslotId  = reference("timeslot_id", TimeSlotTable)
    val maxCapacity = integer("max_capacity").default(5)
}

object SessionEnrollmentsTable : IntIdTable("session_enrollments") {
    val sessionId  = reference("session_id", SessionsTable)
    val studentId  = reference("student_id", StudentTable)
    val enrolledAt = datetime("enrolled_at")
}