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

object StudentRestrictionsTable : IntIdTable("studentRestrictions") {
    val studentId = reference("student_id", StudentTable)
    val weeklyHours = integer("weekly_hours")
}

// status de uma aula concreta (com data)
enum class LessonStatus { SCHEDULED, CANCELLED, COMPLETED }

object LessonTable : IntIdTable("lesson") {
    val teacherId = reference("teacher_id", TeacherTable)
    // agrupa todas as ocorrências geradas pela mesma recorrência (null = aula avulsa)
    val seriesId = varchar("series_id", 36).nullable()
    val date = date("date")
    val startTime = time("start_time")
    val endTime = time("end_time")
    val status = enumerationByName("status", 20, LessonStatus::class).default(LessonStatus.SCHEDULED)
}

object LessonStudentTable : IntIdTable("lesson_student") {
    val lessonId = reference("lesson_id", LessonTable)
    val studentId = reference("student_id", StudentTable)
    // null = ainda não marcado, true = presente, false = faltou
    val attended = bool("attended").nullable()
    val attendedAt = timestamp("attended_at").nullable()
}