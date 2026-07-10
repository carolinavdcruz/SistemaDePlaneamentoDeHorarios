package service

import database.tables.LessonStatus
import database.tables.LessonStudentTable
import database.tables.LessonTable
import model.Lesson
import model.LessonStudent
import model.Session
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.greater
import org.jetbrains.exposed.sql.SqlExpressionBuilder.greaterEq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.inList
import org.jetbrains.exposed.sql.SqlExpressionBuilder.less
import org.jetbrains.exposed.sql.SqlExpressionBuilder.lessEq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.neq
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

                    val alreadyExists = LessonTable.select {
                        (LessonTable.teacherId eq teacherId) and
                                (LessonTable.date eq date) and
                                (LessonTable.startTime eq session.slot.startTime) and
                                (LessonTable.endTime eq session.slot.endTime) and
                                (LessonTable.status eq LessonStatus.SCHEDULED)
                    }.singleOrNull()

                    if (alreadyExists != null) {
                        continue
                    }

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

    /** Devolve uma única aula pelo id, ou null se não existir. */
    fun getById(lessonId: Int): Lesson? = transaction {
        LessonTable.select { LessonTable.id eq lessonId }
            .singleOrNull()
            ?.toLesson()
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

    /**
     * Todas as aulas (de qualquer professor) em que este aluno participa,
     * num intervalo [from, to] (inclusive). Usado no ecrã principal do aluno.
     */
    fun getHistoryForStudent(studentId: Int, from: LocalDate, to: LocalDate): List<Lesson> = transaction {
        val lessonIds = LessonStudentTable
            .select { LessonStudentTable.studentId eq studentId }
            .map { it[LessonStudentTable.lessonId].value }
            .distinct()

        if (lessonIds.isEmpty()) return@transaction emptyList()

        LessonTable
            .select {
                (LessonTable.id inList lessonIds) and
                        (LessonTable.date greaterEq from) and
                        (LessonTable.date lessEq to)
            }
            .map { it.toLesson() }
    }

    /** Conveniência: aulas do aluno na semana (segunda a domingo) que contém `anyDateInWeek`. */
    fun getWeekForStudent(studentId: Int, anyDateInWeek: LocalDate): List<Lesson> {
        val monday = anyDateInWeek.minusDays((anyDateInWeek.dayOfWeek.value - 1).toLong())
        val sunday = monday.plusDays(6)
        return getHistoryForStudent(studentId, monday, sunday)
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

    /**
     * Cancela TODAS as ocorrências futuras/agendadas de uma série (recorrência).
     * Não apaga as linhas (mantém o histórico), só marca status = CANCELLED.
     * Devolve quantas aulas foram afetadas.
     */
    fun cancelSeries(seriesId: String): Int = transaction {
        LessonTable.update({
            (LessonTable.seriesId eq seriesId) and (LessonTable.status eq LessonStatus.SCHEDULED)
        }) {
            it[LessonTable.status] = LessonStatus.CANCELLED
        }
    }

    /** Cancela uma única aula (não mexe nas restantes da série, se existir). */
    fun cancelLesson(lessonId: Int): Boolean = transaction {
        LessonTable.update({ LessonTable.id eq lessonId }) {
            it[LessonTable.status] = LessonStatus.CANCELLED
        } > 0
    }

    sealed class UpdateLessonResult {
        data class Success(val lesson: Lesson) : UpdateLessonResult()
        object NotFound : UpdateLessonResult()
        data class Conflict(val conflictingLessonId: Int) : UpdateLessonResult()
    }

    /**
     * Edita uma ocorrência isolada (data/hora). Ao editar, a aula é "destacada"
     * da série (seriesId passa a null) para que ações em massa sobre a série
     * (ex. cancelSeries) deixem de a afetar.
     *
     * Antes de gravar, valida que o professor não fica com duas aulas a
     * sobrepor-se no mesmo dia/hora (exclui a própria aula e aulas já
     * CANCELLED da verificação).
     */
    fun updateLesson(
        lessonId: Int,
        date: LocalDate?,
        startTime: java.time.LocalTime?,
        endTime: java.time.LocalTime?
    ): UpdateLessonResult = transaction {
        val current = LessonTable.select { LessonTable.id eq lessonId }.singleOrNull()
            ?: return@transaction UpdateLessonResult.NotFound

        val newDate = date ?: current[LessonTable.date]
        val newStart = startTime ?: current[LessonTable.startTime]
        val newEnd = endTime ?: current[LessonTable.endTime]
        val teacherId = current[LessonTable.teacherId].value

        val conflict = LessonTable.select {
            (LessonTable.teacherId eq teacherId) and
                    (LessonTable.id neq lessonId) and
                    (LessonTable.date eq newDate) and
                    (LessonTable.status neq LessonStatus.CANCELLED) and
                    (LessonTable.startTime less newEnd) and
                    (LessonTable.endTime greater newStart)
        }.firstOrNull()

        if (conflict != null) {
            return@transaction UpdateLessonResult.Conflict(conflict[LessonTable.id].value)
        }

        LessonTable.update({ LessonTable.id eq lessonId }) {
            it[LessonTable.date] = newDate
            it[LessonTable.startTime] = newStart
            it[LessonTable.endTime] = newEnd
            if (date != null || startTime != null || endTime != null) {
                it[LessonTable.seriesId] = null
            }
        }

        val updated = LessonTable.select { LessonTable.id eq lessonId }.single().toLesson()
        UpdateLessonResult.Success(updated)
    }

    data class AttendanceSummary(
        val studentId: Int,
        val totalLessons: Int,
        val attended: Int,
        val missed: Int,
        val pending: Int,
        val attendanceRate: Double
    )

    /** Resumo de presenças de um aluno (em todas as aulas, com qualquer professor). */
    fun getAttendanceSummary(studentId: Int): AttendanceSummary = transaction {
        val rows = LessonStudentTable
            .select { LessonStudentTable.studentId eq studentId }
            .toList()

        val total = rows.size
        val attended = rows.count { it[LessonStudentTable.attended] == true }
        val missed = rows.count { it[LessonStudentTable.attended] == false }
        val pending = rows.count { it[LessonStudentTable.attended] == null }
        val marked = attended + missed
        val rate = if (marked > 0) attended.toDouble() / marked else 0.0

        AttendanceSummary(
            studentId = studentId,
            totalLessons = total,
            attended = attended,
            missed = missed,
            pending = pending,
            attendanceRate = rate
        )
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