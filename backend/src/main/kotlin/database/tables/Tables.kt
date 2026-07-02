package database.tables

import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.javatime.date
import org.jetbrains.exposed.sql.javatime.time
import org.jetbrains.exposed.sql.javatime.timestamp

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

object StudentRestrictionsTable : IntIdTable("student_restrictions") {
    val studentId = reference("student_id", StudentTable).uniqueIndex()
    val weeklyHours = integer("weekly_hours")
}

enum class LessonStatus {
    SCHEDULED,
    CANCELLED
}

object LessonTable : IntIdTable("lesson") {
    val teacherId = reference("teacher_id", TeacherTable)
    val seriesId = varchar("series_id", 100).nullable()
    val date = date("date")
    val startTime = time("start_time")
    val endTime = time("end_time")
    val status = enumerationByName("status", 30, LessonStatus::class)
}

object LessonStudentTable : IntIdTable("lesson_student") {
    val lessonId = reference("lesson_id", LessonTable)
    val studentId = reference("student_id", StudentTable)
    val attended = bool("attended").nullable()
    val attendedAt = timestamp("attended_at").nullable()

    init {
        uniqueIndex(lessonId, studentId)
    }
}