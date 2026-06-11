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
import plugins.intParam
import service.AvailabilityService
import service.ScheduleService

@Suppress("NewApi")
fun Route.scheduleRoutes() {
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

            val teacherSlots = AvailabilityTable
                .select { AvailabilityTable.teacherId eq teacherId }
                .flatMap { row ->
                    AvailabilityService.splitIntoSlots(
                        dayOfWeek = row[AvailabilityTable.dayOfWeek],
                        startTime = row[AvailabilityTable.startTime],
                        endTime   = row[AvailabilityTable.endTime],
                        slotDurationMinutes = restrictions.sessionDurationMinutes.toLong()
                    ).map { slot ->
                        TimeSlot(
                            dayOfWeek = slot.dayOfWeek,
                            startTime = slot.startTime,
                            endTime   = slot.endTime
                        )}
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
                            endTime   = row[AvailabilityTable.endTime],
                            slotDurationMinutes = restrictions.sessionDurationMinutes.toLong()
                        ).map { slot ->
                            TimeSlot(
                                dayOfWeek = slot.dayOfWeek,
                                startTime = slot.startTime,
                                endTime   = slot.endTime
                            )
                        }
                    }
            }

            val studentWeeklyHours = students.associate { student ->
                student.id to (StudentRestrictionsTable
                    .select { StudentRestrictionsTable.studentId eq student.id }
                    .singleOrNull()
                    ?.get(StudentRestrictionsTable.weeklyHours) ?: 3)
            }

            ScheduleService.create(
                teacherSlots = teacherSlots,
                students = students,
                studentAvailabilities = studentAvailabilities,
                studentWeeklyHours = studentWeeklyHours,
                restrictions = restrictions
            ).map { session ->
                ScheduleSessionResponse(
                    dayOfWeek  = session.slot.dayOfWeek,
                    startTime  = session.slot.startTime.toString(),
                    endTime    = session.slot.endTime.toString(),
                    studentIds = session.studentIds
                )
            }
        }

        if (response == null)
            return@post call.respond(HttpStatusCode.NotFound, mapOf("error" to "Restrições do professor não encontradas"))

        call.respond(HttpStatusCode.OK, response)
    }
}

