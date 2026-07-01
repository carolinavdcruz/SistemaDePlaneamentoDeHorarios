package database.tables

import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.javatime.date
import org.jetbrains.exposed.sql.javatime.datetime
import org.jetbrains.exposed.sql.javatime.time

object TeacherTable : IntIdTable("teacher") {
    val name = varchar("name", 100)
    val email = varchar("email", 150).uniqueIndex()
    val password = varchar("password", 255)

//val sessionDurationMinutes      = integer("session_duration_minutes").default(60)
    //val maxParticipantsPerSession   = integer("max_participants_per_session").default(5)
}

object StudentTable : IntIdTable("student") {
    val name = varchar("name", 100)
    val email = varchar("email", 150).uniqueIndex()
    val password = varchar("password", 255)
    val teacherId = reference("teacher_id", TeacherTable).nullable()
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
    val teacherId = reference("teacher_id", TeacherTable).uniqueIndex()
    val maxDailyHours = integer("max_daily_hours")
    val sessionDurationMinutes = integer("session_duration_minutes")
    val maxParticipantsPerSession = integer("max_participants_per_session")
    val maxSessionsPerStudentPerDay = integer("max_sessions_per_student_per_day")
}

object StudentRestrictionsTable : IntIdTable("studentRestrictions") {
    val studentId = reference("student_id", StudentTable)
    val weeklyHours = integer("weekly_hours")
}

object SchedulePlanTable : IntIdTable("schedule_plan") {
    val teacherId = reference("teacher_id", TeacherTable)
    val title = varchar("title", 150).default("Horario")
    val status = varchar("status", 30).default("DRAFT")
    val weekStart = date("week_start")
    val weekEnd = date("week_end")
    val createdAt = datetime("created_at")
    val acceptedAt = datetime("accepted_at").nullable()
}

object ScheduleSessionTable : IntIdTable("schedule_session") {
    val schedulePlanId = reference("schedule_plan_id", SchedulePlanTable)
    val dayOfWeek = integer("day_of_week")
    val startTime = time("start_time")
    val endTime = time("end_time")
}

object ScheduleSessionStudentTable : IntIdTable("schedule_session_student") {
    val sessionId = reference("session_id", ScheduleSessionTable)
    val studentId = reference("student_id", StudentTable)
}