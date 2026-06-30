package service

import database.tables.LessonStatus
import database.tables.LessonStudentTable
import database.tables.LessonTable
import model.Lesson
import model.LessonStudent
import model.Session
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.greaterEq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.lessEq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import java.time.Instant
import java.time.LocalDate
import java.util.UUID

object LessonService {

    /**
     * Dado o conjunto de sessões geradas pelo ScheduleService (template semanal,
     * baseado em dayOfWeek) e uma data de início (segunda-feira dessa semana),
     * grava `occurrences` semanas consecutivas em LessonTable/LessonStudentTable.
     * Devolve as aulas criadas.
     */
    fun persistRecurring(
        teacherId: Int,
        sessions: List<Session>,
        startDate: LocalDate,
        occurrences: Int
    ): List<Lesson> {
        val seriesId = if (occurrences > 1) UUID.randomUUID().toString() else null
        val created = mutableListOf<Lesson>()

        transaction {
            for (week in 0 until occurrences) {
                for (session in sessions) {
                    // 1=Segunda ... 7=Domingo -> deslocamento a partir da segunda-feira da semana
                    val date = startDate
                        .plusWeeks(week.toLong())
                        .plusDays((session.slot.dayOfWeek - 1).toLong())

                    val lessonId = LessonTable.insert {
                        it[LessonTable.teacherId] = teacherId
                        it[LessonTable.seriesId] = seriesId
                        it[LessonTable.date] = date
                        it[LessonTable.startTime] = session.slot.startTime
                        it[LessonTable.endTime] = session.slot.endTime
                        it[LessonTable.status] = LessonStatus.SCHEDULED
                    } get LessonTable.id

                    val students = session.studentIds.map { studentId ->
                        LessonStudentTable.insert {
                            it[LessonStudentTable.lessonId] = lessonId.value
                            it[LessonStudentTable.studentId] = studentId
                            it[LessonStudentTable.attended] = null
                        }
                        LessonStudent(studentId = studentId, attended = null, attendedAt = null)
                    }

                    created.add(
                        Lesson(
                            id = lessonId.value,
                            teacherId = teacherId,
                            seriesId = seriesId,
                            date = date,
                            startTime = session.slot.startTime,
                            endTime = session.slot.endTime,
                            status = LessonStatus.SCHEDULED,
                            students = students
                        )
                    )
                }
            }
        }
        return created
    }

    /** Histórico: todas as aulas de um professor num intervalo [from, to] (inclusive). */
    fun getHistory(teacherId: Int, from: LocalDate, to: LocalDate): List<Lesson> = transaction {
        LessonTable
            .select { (LessonTable.teacherId eq teacherId) and (LessonTable.date greaterEq from) and (LessonTable.date lessEq to) }
            .map { it.toLesson() }
    }

    /** Conveniência: histórico da semana (segunda a domingo) que contém `anyDateInWeek`. */
    fun getHistoryForWeek(teacherId: Int, anyDateInWeek: LocalDate): List<Lesson> {
        val monday = anyDateInWeek.minusDays((anyDateInWeek.dayOfWeek.value - 1).toLong())
        val sunday = monday.plusDays(6)
        return getHistory(teacherId, monday, sunday)
    }

    fun markAttendance(lessonId: Int, studentId: Int, attended: Boolean): Boolean = transaction {
        val updated = LessonStudentTable.update({
            (LessonStudentTable.lessonId eq lessonId) and (LessonStudentTable.studentId eq studentId)
        }) {
            it[LessonStudentTable.attended] = attended
            it[LessonStudentTable.attendedAt] = Instant.now()
        }
        updated > 0
    }

    private fun org.jetbrains.exposed.sql.ResultRow.toLesson(): Lesson {
        val lessonId = this[LessonTable.id].value
        val students = LessonStudentTable
            .select { LessonStudentTable.lessonId eq lessonId }
            .map {
                LessonStudent(
                    studentId = it[LessonStudentTable.studentId].value,
                    attended = it[LessonStudentTable.attended],
                    attendedAt = it[LessonStudentTable.attendedAt]
                )
            }
        return Lesson(
            id = lessonId,
            teacherId = this[LessonTable.teacherId].value,
            seriesId = this[LessonTable.seriesId],
            date = this[LessonTable.date],
            startTime = this[LessonTable.startTime],
            endTime = this[LessonTable.endTime],
            status = this[LessonTable.status],
            students = students
        )
    }
}
