package plugins.routes.schedule

import database.tables.AvailabilityTable
import database.tables.RestrictionsTable
import database.tables.StudentRestrictionsTable
import database.tables.StudentTable
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import model.Restrictions
import model.ScheduleCreateRequest
import model.ScheduleSessionResponse
import model.Student
import model.TimeSlot
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import service.AvailabilityService
import service.ScheduleService


@Suppress("NewApi")
fun Route.scheduleRoutes() {

    // POST /schedule/create
    // Cria horário para o professor com base nas disponibilidades
    post("/schedule/create") {
        val request = call.receive<ScheduleCreateRequest>()
        val teacherId = request.teacherId
        val response = transaction {
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

            val teacherAvailability = AvailabilityTable
                .select { AvailabilityTable.teacherId eq teacherId }
                .map { Triple(
                        it[AvailabilityTable.dayOfWeek],
                        it[AvailabilityTable.startTime],
                        it[AvailabilityTable.endTime]
                ) }

            val teacherSlots = teacherAvailability.flatMap { (dayOfWeek, startTime, endTime) ->
                AvailabilityService.splitIntoSlots(
                    dayOfWeek = dayOfWeek,
                    startTime = startTime,
                    endTime = endTime,
                    slotDurationMinutes = restrictions.sessionDurationMinutes.toLong()
                ).map { slot -> TimeSlot(
                        dayOfWeek = slot.dayOfWeek,
                        startTime = slot.startTime,
                        endTime = slot.endTime
                ) }
            }

            val students = StudentTable
                .select { StudentTable.teacherId eq teacherId }
                .map { Student(
                        id = it[StudentTable.id].value,
                        name = it[StudentTable.name],
                        email = it[StudentTable.email],
                        teacherId = it[StudentTable.teacherId]?.value,
                        maxDailySessions = it[StudentTable.maxDailySessions],
                ) }

            val studentAvailabilities = students.associate { student ->
                val availabilityIntervals = AvailabilityTable
                    .select { AvailabilityTable.studentId eq student.id }
                    .map { Triple(
                            it[AvailabilityTable.dayOfWeek],
                            it[AvailabilityTable.startTime],
                            it[AvailabilityTable.endTime]
                    ) }

                val slots = availabilityIntervals.flatMap { (dayOfWeek, startTime, endTime) ->
                    AvailabilityService.splitIntoSlots(
                        dayOfWeek = dayOfWeek,
                        startTime = startTime,
                        endTime = endTime,
                        slotDurationMinutes = restrictions.sessionDurationMinutes.toLong()
                    ).map { slot -> TimeSlot(
                            dayOfWeek = slot.dayOfWeek,
                            startTime = slot.startTime,
                            endTime = slot.endTime
                    ) }
                }
                student.id to slots
            }

            val studentWeeklyHours = students.associate { student ->
                val weeklyHours = StudentRestrictionsTable
                    .select { StudentRestrictionsTable.studentId eq student.id }
                    .singleOrNull()
                    ?.get(StudentRestrictionsTable.weeklyHours)
                    ?: 3
                student.id to weeklyHours
            }

            ScheduleService.create(
                teacherSlots = teacherSlots,
                students = students,
                studentAvailabilities = studentAvailabilities,
                studentWeeklyHours = studentWeeklyHours,
                restrictions = restrictions
            ).map { session -> ScheduleSessionResponse(
                    dayOfWeek = session.slot.dayOfWeek,
                    startTime = session.slot.startTime.toString(),
                    endTime = session.slot.endTime.toString(),
                    studentIds = session.studentIds
            ) }
        }
        if (response == null) {
            call.respond(
                HttpStatusCode.NotFound,
                mapOf("error" to "Restrições do professor não encontradas")
            )
            return@post
        }
        call.respond(HttpStatusCode.OK, response)
    }




}

