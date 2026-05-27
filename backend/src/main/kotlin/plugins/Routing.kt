package plugins

import database.tables.AvailabilityTable
import database.tables.RestrictionsTable
import database.tables.StudentTable
import database.tables.TeacherTable
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.call
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.delete
import io.ktor.server.routing.put
import io.ktor.server.routing.routing
import model.AssignTeacherRequest
import model.AvailabilityRequest
import model.AvailabilityResponse
import model.OwnerType
import model.Restrictions
import model.RestrictionsRequest
import model.RestrictionsResponse
import model.ScheduleCreateRequest
import model.ScheduleSessionResponse
import model.Session
import model.Student
import model.StudentRequest
import model.StudentResponse
import model.TeacherRequest
import model.TeacherResponse
import model.TimeSlot
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import service.AvailabilityService
import service.ScheduleService
import java.time.LocalTime

@Suppress("NewApi")
fun Application.configureRouting() {

    routing {
        // GET /health  →  confirma que o servidor está vivo
        get("/health") {
            call.respond(HttpStatusCode.OK, mapOf("status" to "ok"))
        }

        // POST /teachers
        // Regista um novo professor.
        // Body JSON esperado:
        // {
        //   "name": "João Silva",
        //   "email": "joao@isel.pt",
        // }
        post("/teachers") {
            val request = call.receive<TeacherRequest>()
            val created = transaction {
                val id = TeacherTable.insert {
                    it[name] = request.name
                    it[email] = request.email
                } get TeacherTable.id
                TeacherResponse(
                    id = id.value,
                    name = request.name,
                    email = request.email

                )
            }
            call.respond(HttpStatusCode.Created, mapOf("message" to "Professor criado com sucesso"))
        }

        // GET /teachers
        // Lista todos os professores.
        get("/teachers") {
            val teachers = transaction {
                TeacherTable.selectAll().map {
                    TeacherResponse(
                        id = it[TeacherTable.id].value,
                        name = it[TeacherTable.name],
                        email = it[TeacherTable.email]
                    )
                }
            }
            call.respond(HttpStatusCode.OK, teachers)
        }

        // POST /students
        // Regista um novo aluno.
        // Body JSON esperado:
        // {
        // "name": "Ana Costa",
        // "email": "ana@alunos.isel.pt"
        // }
        post("/students") {
            val request = call.receive<StudentRequest>()
            val created = transaction {
                val id = StudentTable.insert {
                    it[name] = request.name
                    it[email] = request.email
                } get StudentTable.id
                StudentResponse(
                    id = id.value,
                    name = request.name,
                    email = request.email,
                    teacherId = null
                )
            }
            call.respond(HttpStatusCode.Created, mapOf("message" to "Aluno criado com sucesso"))
        }

        // GET /students
        // Lista todos os alunos.
        get("/students") {
            val students = transaction {
                StudentTable.selectAll().map {
                    StudentResponse(
                        id = it[StudentTable.id].value,
                        name = it[StudentTable.name],
                        email = it[StudentTable.email],
                        teacherId = it[StudentTable.teacherId]?.value
                    )
                }
            }
            call.respond(HttpStatusCode.OK, students)
        }

        post("/students/assign-teacher") {
            val request = call.receive<AssignTeacherRequest>()
            val updateRowStudent = transaction {
                StudentTable.update({ StudentTable.id eq request.studentId }) {
                    it[teacherId] = request.teacherId
                }
            }
            if (updateRowStudent == 0) {
                call.respond(HttpStatusCode.NotFound, mapOf("error" to "Aluno não encontrado"))
            }
            call.respond(HttpStatusCode.OK, mapOf("message" to "Professor associado com sucesso"))
        }

        post("/students/unassign-teacher/{studentId}") {
            val studentId = call.parameters["studentId"]?.toIntOrNull()
                ?: return@post call.respond(
                    HttpStatusCode.BadRequest,
                    mapOf("error" to "studentId inválido")
                )

            val updatedRowsStudent = transaction {
                StudentTable.update({ StudentTable.id eq studentId }) {
                    it[teacherId] = null
                }
            }

            if (updatedRowsStudent == 0) {
                call.respond(HttpStatusCode.NotFound, mapOf("error" to "Aluno não encontrado"))
                return@post
            }

            call.respond(
                HttpStatusCode.OK,
                mapOf("message" to "Professor desassociado com sucesso")
            )
        }

        get("/students/by-teacher/{teacherId}") {
            val teacherId = call.parameters["teacherId"]?.toIntOrNull()
                ?: return@get call.respond(
                    HttpStatusCode.BadRequest,
                    mapOf("error" to "teacherId inválido")
                )

            val students = transaction {
                StudentTable
                    .select { StudentTable.teacherId eq teacherId }
                    .map {
                        StudentResponse(
                            id = it[StudentTable.id].value,
                            name = it[StudentTable.name],
                            email = it[StudentTable.email],
                            teacherId = it[StudentTable.teacherId]?.value
                        )
                    }
            }

            call.respond(HttpStatusCode.OK, students)
        }


        // POST /availability
        // Recebe um intervalo (ex: 09:00–11:00)
        // guarda cada slot na tabela timeslots e regista a disponibilidade
        // na tabela availability, devolvendo a lista de slots criados.
        // Body JSON esperado:
        // {
        //   "id": 1,
        //   "ownerId": 1,
        //   "ownerType": "TEACHER",   <- "TEACHER" ou "STUDENT"
        //   "dayOfWeek": 2,           <- 1=Segunda ... 7=Domingo
        //   "startTime": "09:00",
        //   "endTime": "11:00"
        // }
        post("/availability") {
            val request = call.receive<AvailabilityRequest>()
            val idSavedAvailability = transaction {
                AvailabilityTable.insert {
                    it[dayOfWeek] = request.dayOfWeek
                    it[startTime] = LocalTime.parse(request.startTime)
                    it[endTime] = LocalTime.parse(request.endTime)

                    if (request.ownerType == OwnerType.TEACHER) {
                        it[teacherId] = request.ownerId
                    } else {
                        it[studentId] = request.ownerId
                    }
                } get AvailabilityTable.id
            }
            val response = AvailabilityResponse(
                id = idSavedAvailability.value,
                ownerId = request.ownerId,
                ownerType = request.ownerType,
                dayOfWeek = request.dayOfWeek,
                startTime = request.startTime,
                endTime = request.endTime
            )
            call.respond(HttpStatusCode.Created, response)
        }

        // GET /availability?ownerType=TEACHER&ownerId=1
        // Devolve todas as disponibilidades de um professor ou aluno.
        get("/availability") {
            val ownerId = call.request.queryParameters["ownerId"]?.toIntOrNull()
                ?: return@get call.respond(HttpStatusCode.BadRequest, mapOf("error" to "ownerId inválido"))

            val ownerTypeParam = call.request.queryParameters["ownerType"]
                ?: return@get call.respond(HttpStatusCode.BadRequest, mapOf("error" to "ownerType em falta"))

            val ownerType = try {
                OwnerType.valueOf(ownerTypeParam)
            } catch (e: IllegalArgumentException) {
                return@get call.respond(HttpStatusCode.BadRequest, mapOf("error" to "ownerType inválido"))
            }

            val result = transaction {
                val query = if (ownerType == OwnerType.TEACHER) {
                    AvailabilityTable.select { AvailabilityTable.teacherId eq ownerId }
                } else {
                    AvailabilityTable.select { AvailabilityTable.studentId eq ownerId }
                }

                query.map {
                    AvailabilityResponse(
                        id = it[AvailabilityTable.id].value,
                        ownerId = ownerId,
                        ownerType = ownerType,
                        dayOfWeek = it[AvailabilityTable.dayOfWeek],
                        startTime = it[AvailabilityTable.startTime].toString(),
                        endTime = it[AvailabilityTable.endTime].toString()
                    )
                }
            }

            call.respond(HttpStatusCode.OK, result)
        }

        // DELETE /availability?ownerId=1&ownerType=TEACHER
        // Remove todas as disponibilidades de um professor ou aluno.
        delete("/availability") {
            val ownerId = call.request.queryParameters["ownerId"]?.toIntOrNull()
                ?: return@delete call.respond(HttpStatusCode.BadRequest, mapOf("error" to "ownerId inválido"))

            val ownerTypeParam = call.request.queryParameters["ownerType"]
                ?: return@delete call.respond(HttpStatusCode.BadRequest, mapOf("error" to "ownerType em falta"))

            val ownerType = try {
                OwnerType.valueOf(ownerTypeParam)
            } catch (e: IllegalArgumentException) {
                return@delete call.respond(HttpStatusCode.BadRequest, mapOf("error" to "ownerType inválido"))
            }

            transaction {
                if (ownerType == OwnerType.TEACHER) {
                    AvailabilityTable.deleteWhere { teacherId eq ownerId }
                } else {
                    AvailabilityTable.deleteWhere { studentId eq ownerId }
                }
            }

            call.respond(HttpStatusCode.OK, mapOf("message" to "Disponibilidade removida com sucesso"))
        }

        get("/restrictions/{teacherId}") {
            val teacherId = call.parameters["teacherId"]?.toIntOrNull()
                ?: return@get call.respond(HttpStatusCode.BadRequest, mapOf("error" to "teacherId inválido"))

            val restrictions = transaction {
                RestrictionsTable
                    .select { RestrictionsTable.teacherId eq teacherId }
                    .singleOrNull()
                    ?.let {
                        RestrictionsResponse(
                            teacherId = it[RestrictionsTable.teacherId].value,
                            maxDailyHours = it[RestrictionsTable.maxDailyHours],
                            sessionDurationMinutes = it[RestrictionsTable.sessionDurationMinutes],
                            maxParticipantsPerSession = it[RestrictionsTable.maxParticipantsPerSession],
                            maxSessionsPerStudentPerDay = it[RestrictionsTable.maxSessionsPerStudentPerDay]
                        )
                    }
            }

            if (restrictions == null) {
                call.respond(HttpStatusCode.NotFound, mapOf("error" to "Restrições não encontradas"))
                return@get
            }

            call.respond(HttpStatusCode.OK, restrictions)
        }

        put("/restrictions/{teacherId}") {
            val teacherId = call.parameters["teacherId"]?.toIntOrNull()
                ?: return@put call.respond(HttpStatusCode.BadRequest, mapOf("error" to "teacherId inválido"))

            val request = call.receive<RestrictionsRequest>()

            val updatedRows = transaction {
                RestrictionsTable.update({ RestrictionsTable.teacherId eq teacherId }) {
                    it[maxDailyHours] = request.maxDailyHours
                    it[sessionDurationMinutes] = request.sessionDurationMinutes
                    it[maxParticipantsPerSession] = request.maxParticipantsPerSession
                    it[maxSessionsPerStudentPerDay] = request.maxSessionsPerStudentPerDay
                }
            }

            if (updatedRows == 0) {
                transaction {
                    RestrictionsTable.insert {
                        it[RestrictionsTable.teacherId] = teacherId
                        it[maxDailyHours] = request.maxDailyHours
                        it[sessionDurationMinutes] = request.sessionDurationMinutes
                        it[maxParticipantsPerSession] = request.maxParticipantsPerSession
                        it[maxSessionsPerStudentPerDay] = request.maxSessionsPerStudentPerDay
                    }
                }
            }

            call.respond(HttpStatusCode.OK, mapOf("message" to "Restrições guardadas com sucesso"))
        }

        // POST /schedule/create
        // Cria horário para o professor com base nas disponibilidades
        post("/schedule/create") {
            val request = call.receive<ScheduleCreateRequest>()
            val teacherId = request.teacherId

            val response = transaction {
                val restrictionsRow = RestrictionsTable
                    .select { RestrictionsTable.teacherId eq teacherId }
                    .singleOrNull()
                    ?: return@transaction null

                val restrictions = Restrictions(
                    teacherId = teacherId,
                    maxDailyHours = restrictionsRow[RestrictionsTable.maxDailyHours],
                    sessionDurationMinutes = restrictionsRow[RestrictionsTable.sessionDurationMinutes],
                    maxParticipantsPerSession = restrictionsRow[RestrictionsTable.maxParticipantsPerSession],
                    maxSessionsPerStudentPerDay = restrictionsRow[RestrictionsTable.maxSessionsPerStudentPerDay]
                )

                val teacherAvailability = AvailabilityTable
                    .select { AvailabilityTable.teacherId eq teacherId }
                    .map {
                        Triple(
                            it[AvailabilityTable.dayOfWeek],
                            it[AvailabilityTable.startTime],
                            it[AvailabilityTable.endTime]
                        )
                    }

                val teacherSlots = teacherAvailability.flatMap { (dayOfWeek, startTime, endTime) ->
                    AvailabilityService.splitIntoSlots(
                        dayOfWeek = dayOfWeek,
                        startTime = startTime,
                        endTime = endTime,
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
                    val availabilityIntervals = AvailabilityTable
                        .select { AvailabilityTable.studentId eq student.id }
                        .map {
                            Triple(
                                it[AvailabilityTable.dayOfWeek],
                                it[AvailabilityTable.startTime],
                                it[AvailabilityTable.endTime]
                            )
                        }

                    val slots = availabilityIntervals.flatMap { (dayOfWeek, startTime, endTime) ->
                        AvailabilityService.splitIntoSlots(
                            dayOfWeek = dayOfWeek,
                            startTime = startTime,
                            endTime = endTime,
                            slotDurationMinutes = restrictions.sessionDurationMinutes.toLong()
                        ).map { slot ->
                            TimeSlot(
                                dayOfWeek = slot.dayOfWeek,
                                startTime = slot.startTime,
                                endTime = slot.endTime
                            )
                        }
                    }

                    student.id to slots
                }

                ScheduleService.create(
                    teacherId = teacherId,
                    teacherSlots = teacherSlots,
                    students = students,
                    studentAvailabilities = studentAvailabilities,
                    restrictions = restrictions
                ).map { session ->
                    ScheduleSessionResponse(
                        dayOfWeek = session.slot.dayOfWeek,
                        startTime = session.slot.startTime.toString(),
                        endTime = session.slot.endTime.toString(),
                        studentIds = session.studentIds
                    )
                }
            }

            if (response == null) {
                call.respond(HttpStatusCode.NotFound, mapOf("error" to "Restrições do professor não encontradas"))
                return@post
            }

            call.respond(HttpStatusCode.OK, response)
        }
    }
}