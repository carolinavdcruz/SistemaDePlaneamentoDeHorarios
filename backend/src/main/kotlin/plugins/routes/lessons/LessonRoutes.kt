package plugins.routes.lessons

import database.tables.AvailabilityTable
import database.tables.LessonStatus
import database.tables.LessonStudentTable
import database.tables.LessonTable
import database.tables.RestrictionsTable
import database.tables.StudentRestrictionsTable
import database.tables.StudentTable
import database.tables.TeacherTable
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.patch
import io.ktor.server.routing.post
import model.AttendanceSummaryResponse
import model.CancelSeriesResponse
import model.GenerateLessonsRequest
import model.LessonConflictResponse
import model.LessonResponse
import model.LessonStudentResponse
import model.MarkAttendanceRequest
import model.NotifyStudentsRequest
import model.NotifyStudentsResponse
import model.RecurrenceType
import model.Restrictions
import model.UpdateLessonRequest
import model.Student
import model.StudentResponse
import model.TimeSlot
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import plugins.intParam
import service.AvailabilityService
import service.EmailService
import service.LessonService
import service.ScheduleService
import java.time.LocalDate
import java.time.LocalTime

@Suppress("NewApi")
fun Route.lessonRoutes() {

    post("/lessons/generate") {

        val request = call.receive<GenerateLessonsRequest>()

        val teacherId = request.teacherId

        // O professor pode indicar qualquer dia; ajustamos automaticamente para a
        // segunda-feira da semana em que esse dia cai, já que é a partir daí que
        // o deslocamento de cada sessão (dayOfWeek - 1 dias) é calculado.
        val requestedDate = LocalDate.parse(request.startDate)
        val startDate = requestedDate.minusDays((requestedDate.dayOfWeek.value - 1).toLong())

        val occurrences =
            if (request.recurrence == RecurrenceType.NONE) 1 else request.occurrences.coerceAtLeast(1)

        val sessions = transaction {
            val restrictionsRow = RestrictionsTable
                .select { RestrictionsTable.teacherId eq teacherId }
                .singleOrNull() ?: return@transaction null

            val restrictions = Restrictions(
                teacherId = teacherId,
                maxDailyHours = restrictionsRow[RestrictionsTable.maxDailyHours],
                sessionDurationMinutes = restrictionsRow[RestrictionsTable.sessionDurationMinutes],
                maxParticipantsPerSession = restrictionsRow[RestrictionsTable.maxParticipantsPerSession],
                maxSessionsPerStudentPerDay = restrictionsRow[RestrictionsTable.maxSessionsPerStudentPerDay]
            )

            val teacherSlots = AvailabilityTable
                .select { AvailabilityTable.teacherId eq teacherId }
                .flatMap { row ->
                    AvailabilityService.splitIntoSlots(
                        dayOfWeek = row[AvailabilityTable.dayOfWeek],
                        startTime = row[AvailabilityTable.startTime],
                        endTime = row[AvailabilityTable.endTime],
                        slotDurationMinutes = restrictions.sessionDurationMinutes.toLong()
                    ).map { slot ->
                        TimeSlot(
                            dayOfWeek = slot.dayOfWeek,
                            startTime = slot.startTime,
                            endTime = slot.endTime
                        )
                    }
                }

            val students = StudentTable
                .select { StudentTable.teacherId eq teacherId }
                .map {
                    Student(
                        id = it[StudentTable.id].value,
                        name = it[StudentTable.name],
                        email = it[StudentTable.email],
                        teacherId = it[StudentTable.teacherId]?.value,
                        maxDailySessions = it[StudentTable.maxDailySessions]
                    )
                }

            val studentAvailabilities = students.associate { student ->
                student.id to AvailabilityTable
                    .select { AvailabilityTable.studentId eq student.id }
                    .flatMap { row ->
                        AvailabilityService.splitIntoSlots(
                            dayOfWeek = row[AvailabilityTable.dayOfWeek],
                            startTime = row[AvailabilityTable.startTime],
                            endTime = row[AvailabilityTable.endTime],
                            slotDurationMinutes = restrictions.sessionDurationMinutes.toLong()
                        ).map { slot ->
                            TimeSlot(
                                dayOfWeek = slot.dayOfWeek,
                                startTime = slot.startTime,
                                endTime = slot.endTime
                            )
                        }
                    }
            }

            val studentWeeklyHours = students.associate { student ->
                student.id to (
                        StudentRestrictionsTable
                            .select { StudentRestrictionsTable.studentId eq student.id }
                            .singleOrNull()
                            ?.get(StudentRestrictionsTable.weeklyHours) ?: 3
                        )
            }

            ScheduleService.create(
                teacherSlots = teacherSlots,
                students = students,
                studentAvailabilities = studentAvailabilities,
                studentWeeklyHours = studentWeeklyHours,
                restrictions = restrictions
            )
        }

        if (sessions == null) {
            call.respond(
                HttpStatusCode.NotFound,
                mapOf("error" to "Restrições do professor não encontradas")
            )
            return@post
        }

        val created = LessonService.persistRecurring(
            teacherId = teacherId,
            sessions = sessions,
            startDate = startDate,
            occurrences = occurrences,
            notBefore = requestedDate
        )

        call.respond(
            HttpStatusCode.OK,
            created.map { lesson ->
                LessonResponse(
                    id = lesson.id,
                    teacherId = lesson.teacherId,
                    seriesId = lesson.seriesId,
                    date = lesson.date.toString(),
                    startTime = lesson.startTime.toString(),
                    endTime = lesson.endTime.toString(),
                    status = lesson.status.name,
                    students = lesson.students.map {
                        LessonStudentResponse(
                            studentId = it.studentId,
                            attended = it.attended,
                            attendedAt = it.attendedAt?.toString()
                        )
                    }
                )
            }
        )
    }

    get("/lessons/{lessonId}") {
        val lessonId = call.intParam("lessonId") ?: return@get
        val lesson = LessonService.getById(lessonId)
            ?: return@get call.respond(HttpStatusCode.NotFound, mapOf("error" to "Aula não encontrada"))

        call.respond(
            HttpStatusCode.OK,
            LessonResponse(
                id = lesson.id,
                teacherId = lesson.teacherId,
                seriesId = lesson.seriesId,
                date = lesson.date.toString(),
                startTime = lesson.startTime.toString(),
                endTime = lesson.endTime.toString(),
                status = lesson.status.name,
                students = lesson.students.map {
                    LessonStudentResponse(
                        studentId = it.studentId,
                        attended = it.attended,
                        attendedAt = it.attendedAt?.toString()
                    )
                }
            )
        )
    }

    get("/lessons/history") {
        val teacherId = call.request.queryParameters["teacherId"]?.toIntOrNull()
            ?: return@get call.respond(HttpStatusCode.BadRequest, mapOf("error" to "teacherId inválido"))
        val from = call.request.queryParameters["from"]?.let(LocalDate::parse)
            ?: return@get call.respond(HttpStatusCode.BadRequest, mapOf("error" to "from inválido"))
        val to = call.request.queryParameters["to"]?.let(LocalDate::parse)
            ?: return@get call.respond(HttpStatusCode.BadRequest, mapOf("error" to "to inválido"))

        val lessons = LessonService.getHistory(teacherId, from, to)
        call.respond(
            HttpStatusCode.OK,
            lessons.map { lesson ->
                LessonResponse(
                    id = lesson.id,
                    teacherId = lesson.teacherId,
                    seriesId = lesson.seriesId,
                    date = lesson.date.toString(),
                    startTime = lesson.startTime.toString(),
                    endTime = lesson.endTime.toString(),
                    status = lesson.status.name,
                    students = lesson.students.map {
                        LessonStudentResponse(
                            studentId = it.studentId,
                            attended = it.attended,
                            attendedAt = it.attendedAt?.toString()
                        )
                    }
                )
            }
        )
    }

    get("/lessons/week") {
        val teacherId = call.request.queryParameters["teacherId"]?.toIntOrNull()
            ?: return@get call.respond(HttpStatusCode.BadRequest, mapOf("error" to "teacherId inválido"))
        val date = call.request.queryParameters["date"]?.let(LocalDate::parse)
            ?: return@get call.respond(HttpStatusCode.BadRequest, mapOf("error" to "date inválida"))

        val lessons = LessonService.getHistoryForWeek(teacherId, date)
        call.respond(
            HttpStatusCode.OK,
            lessons.map { lesson ->
                LessonResponse(
                    id = lesson.id,
                    teacherId = lesson.teacherId,
                    seriesId = lesson.seriesId,
                    date = lesson.date.toString(),
                    startTime = lesson.startTime.toString(),
                    endTime = lesson.endTime.toString(),
                    status = lesson.status.name,
                    students = lesson.students.map {
                        LessonStudentResponse(
                            studentId = it.studentId,
                            attended = it.attended,
                            attendedAt = it.attendedAt?.toString()
                        )
                    }
                )
            }
        )
    }

    patch("/lessons/{lessonId}/students/{studentId}/attendance") {
        val lessonId = call.intParam("lessonId") ?: return@patch
        val studentId = call.intParam("studentId") ?: return@patch
        val request = call.receive<MarkAttendanceRequest>()

        val updated = LessonService.markAttendance(lessonId, studentId, request.attended)
        if (!updated) {
            call.respond(HttpStatusCode.NotFound, mapOf("error" to "Registo de presença não encontrado"))
            return@patch
        }

        call.respond(HttpStatusCode.OK, mapOf("message" to "Presença atualizada com sucesso"))
    }

    get("/lessons/students/{studentId}/attendance-summary") {
        val studentId = call.intParam("studentId") ?: return@get
        val summary = LessonService.getAttendanceSummary(studentId)

        call.respond(
            HttpStatusCode.OK,
            AttendanceSummaryResponse(
                studentId = summary.studentId,
                totalLessons = summary.totalLessons,
                attended = summary.attended,
                missed = summary.missed,
                pending = summary.pending,
                attendanceRate = summary.attendanceRate
            )
        )
    }

    patch("/lessons/{lessonId}/cancel") {
        val lessonId = call.intParam("lessonId") ?: return@patch

        val lesson = LessonService.getById(lessonId)
            ?: return@patch call.respond(HttpStatusCode.NotFound, mapOf("error" to "Aula não encontrada"))

        val cancelled = LessonService.cancelLesson(lessonId)

        if (!cancelled) {
            call.respond(HttpStatusCode.NotFound, mapOf("error" to "Aula não encontrada"))
            return@patch
        }

        val teacherName = transaction {
            TeacherTable
                .select { TeacherTable.id eq lesson.teacherId }
                .singleOrNull()
                ?.get(TeacherTable.name)
        } ?: "Professor"

        val students = transaction {
            StudentTable
                .select { StudentTable.id inList lesson.students.map { it.studentId } }
                .map {
                    StudentResponse(
                        id = it[StudentTable.id].value,
                        name = it[StudentTable.name],
                        email = it[StudentTable.email],
                        teacherId = it[StudentTable.teacherId]?.value
                    )
                }
        }

        students.forEach { student ->
            EmailService.notifyLessonCancelled(
                studentEmail = student.email,
                studentName = student.name,
                teacherName = teacherName,
                date = lesson.date,
                startTime = lesson.startTime,
                endTime = lesson.endTime
            )
        }

        call.respond(HttpStatusCode.OK, mapOf("message" to "Aula cancelada com sucesso"))
    }

    delete("/lessons/series/{seriesId}") {
        val seriesId = call.parameters["seriesId"]
            ?: return@delete call.respond(
                HttpStatusCode.BadRequest,
                mapOf("error" to "seriesId em falta")
            )

        val seriesLessons = transaction {
            LessonTable
                .select {
                    (LessonTable.seriesId eq seriesId) and
                            (LessonTable.status eq LessonStatus.SCHEDULED)
                }
                .toList()
        }

        if (seriesLessons.isEmpty()) {
            call.respond(HttpStatusCode.OK, CancelSeriesResponse(cancelledCount = 0))
            return@delete
        }

        val teacherId = seriesLessons.first()[LessonTable.teacherId].value

        val teacherName = transaction {
            TeacherTable
                .select { TeacherTable.id eq teacherId }
                .singleOrNull()
                ?.get(TeacherTable.name)
        } ?: "Professor"

        val lessonIds = seriesLessons.map { it[LessonTable.id].value }

        val affectedStudentIds = transaction {
            LessonStudentTable
                .select { LessonStudentTable.lessonId inList lessonIds }
                .map { it[LessonStudentTable.studentId].value }
                .distinct()
        }

        val affectedStudents = transaction {
            if (affectedStudentIds.isEmpty()) {
                emptyList()
            } else {
                StudentTable
                    .select { StudentTable.id inList affectedStudentIds }
                    .map {
                        StudentResponse(
                            id = it[StudentTable.id].value,
                            name = it[StudentTable.name],
                            email = it[StudentTable.email],
                            teacherId = it[StudentTable.teacherId]?.value
                        )
                    }
            }
        }

        val count = LessonService.cancelSeries(seriesId)

        affectedStudents.forEach { student ->
            EmailService.notifySeriesCancelled(
                studentEmail = student.email,
                studentName = student.name,
                teacherName = teacherName,
                affectedCount = count
            )
        }

        call.respond(HttpStatusCode.OK, CancelSeriesResponse(cancelledCount = count))
    }

    patch("/lessons/{lessonId}") {

        val lessonId = call.intParam("lessonId") ?: return@patch

        val request = call.receive<UpdateLessonRequest>()

        val previousLesson = LessonService.getById(lessonId)
            ?: return@patch call.respond(HttpStatusCode.NotFound, mapOf("error" to "Aula não encontrada"))

        val result = LessonService.updateLesson(
            lessonId = lessonId,
            date = request.date?.let(LocalDate::parse),
            startTime = request.startTime?.let(LocalTime::parse),
            endTime = request.endTime?.let(LocalTime::parse)
        )

        when (result) {
            is LessonService.UpdateLessonResult.NotFound -> {
                call.respond(HttpStatusCode.NotFound, mapOf("error" to "Aula não encontrada"))
            }

            is LessonService.UpdateLessonResult.Conflict -> {
                call.respond(
                    HttpStatusCode.Conflict,
                    LessonConflictResponse(
                        error = "Conflito com outra aula do professor",
                        conflictingLessonId = result.conflictingLessonId
                    )
                )
            }

            is LessonService.UpdateLessonResult.Success -> {

                val lesson = result.lesson

                val teacherName = transaction {
                    TeacherTable
                        .select { TeacherTable.id eq lesson.teacherId }
                        .singleOrNull()
                        ?.get(TeacherTable.name)
                } ?: "Professor"

                val students = transaction {
                    StudentTable
                        .select { StudentTable.id inList lesson.students.map { it.studentId } }
                        .map {
                            StudentResponse(
                                id = it[StudentTable.id].value,
                                name = it[StudentTable.name],
                                email = it[StudentTable.email],
                                teacherId = it[StudentTable.teacherId]?.value
                            )
                        }
                }

                students.forEach { student ->
                    EmailService.notifyLessonRescheduled(
                        studentEmail = student.email,
                        studentName = student.name,
                        teacherName = teacherName,
                        oldDate = previousLesson.date,
                        oldStart = previousLesson.startTime,
                        oldEnd = previousLesson.endTime,
                        newDate = lesson.date,
                        newStart = lesson.startTime,
                        newEnd = lesson.endTime
                    )
                }

                call.respond(
                    HttpStatusCode.OK,
                    LessonResponse(
                        id = lesson.id,
                        teacherId = lesson.teacherId,
                        seriesId = lesson.seriesId,
                        date = lesson.date.toString(),
                        startTime = lesson.startTime.toString(),
                        endTime = lesson.endTime.toString(),
                        status = lesson.status.name,
                        students = lesson.students.map {
                            LessonStudentResponse(
                                studentId = it.studentId,
                                attended = it.attended,
                                attendedAt = it.attendedAt?.toString()
                            )
                        }
                    )
                )
            }
        }
    }

    post("/teachers/{teacherId}/notify") {
        val teacherId = call.intParam("teacherId") ?: return@post
        val request = call.receive<NotifyStudentsRequest>()

        val teacherName = transaction {
            TeacherTable
                .select { TeacherTable.id eq teacherId }
                .singleOrNull()
                ?.get(TeacherTable.name)
        } ?: return@post call.respond(HttpStatusCode.NotFound, mapOf("error" to "Professor não encontrado"))

        val students = transaction {
            val baseQuery = StudentTable.select { StudentTable.teacherId eq teacherId }
            val rows = if (request.studentIds.isNullOrEmpty()) {
                baseQuery.toList()
            } else {
                baseQuery.filter { it[StudentTable.id].value in request.studentIds }
            }

            rows.map {
                StudentResponse(
                    id = it[StudentTable.id].value,
                    name = it[StudentTable.name],
                    email = it[StudentTable.email],
                    teacherId = it[StudentTable.teacherId]?.value
                )
            }
        }

        students.forEach { student ->
            EmailService.notifyCustom(
                studentEmail = student.email,
                studentName = student.name,
                teacherName = teacherName,
                subject = request.subject,
                message = request.message
            )
        }

        call.respond(HttpStatusCode.OK, NotifyStudentsResponse(sentTo = students.size))
    }

    get("/lessons/student/week") {
        val studentId = call.request.queryParameters["studentId"]?.toIntOrNull()
            ?: return@get call.respond(HttpStatusCode.BadRequest, mapOf("error" to "studentId inválido"))
        val date = call.request.queryParameters["date"]?.let(LocalDate::parse)
            ?: return@get call.respond(HttpStatusCode.BadRequest, mapOf("error" to "date inválida"))

        val lessons = LessonService.getWeekForStudent(studentId, date)
        call.respond(
            HttpStatusCode.OK,
            lessons.map { lesson ->
                LessonResponse(
                    id = lesson.id,
                    teacherId = lesson.teacherId,
                    seriesId = lesson.seriesId,
                    date = lesson.date.toString(),
                    startTime = lesson.startTime.toString(),
                    endTime = lesson.endTime.toString(),
                    status = lesson.status.name,
                    students = lesson.students.map {
                        LessonStudentResponse(
                            studentId = it.studentId,
                            attended = it.attended,
                            attendedAt = it.attendedAt?.toString()
                        )
                    }
                )
            }
        )
    }

    get("/lessons/student/history") {
        val studentId = call.request.queryParameters["studentId"]?.toIntOrNull()
            ?: return@get call.respond(HttpStatusCode.BadRequest, mapOf("error" to "studentId inválido"))
        val from = call.request.queryParameters["from"]?.let(LocalDate::parse)
            ?: return@get call.respond(HttpStatusCode.BadRequest, mapOf("error" to "from inválido"))
        val to = call.request.queryParameters["to"]?.let(LocalDate::parse)
            ?: return@get call.respond(HttpStatusCode.BadRequest, mapOf("error" to "to inválido"))

        val lessons = LessonService.getHistoryForStudent(studentId, from, to)
        call.respond(
            HttpStatusCode.OK,
            lessons.map { lesson ->
                LessonResponse(
                    id = lesson.id,
                    teacherId = lesson.teacherId,
                    seriesId = lesson.seriesId,
                    date = lesson.date.toString(),
                    startTime = lesson.startTime.toString(),
                    endTime = lesson.endTime.toString(),
                    status = lesson.status.name,
                    students = lesson.students.map {
                        LessonStudentResponse(
                            studentId = it.studentId,
                            attended = it.attended,
                            attendedAt = it.attendedAt?.toString()
                        )
                    }
                )
            }
        )
    }
}