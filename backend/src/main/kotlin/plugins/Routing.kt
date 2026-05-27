package plugins

import database.tables.AvailabilityTable
import database.tables.RestrictionsTable
import database.tables.SchedulesTable
import database.tables.SessionEnrollmentsTable
import database.tables.SessionsTable
import database.tables.StudentTable
import database.tables.TeacherTable
import database.tables.TimeSlotTable
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.call
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.put
import io.ktor.server.routing.routing
import model.AvailabilityRequest
import model.AvailabilityResponse
import model.Restrictions
import model.SaveScheduleRequest
import model.ScheduleResponse
import model.Session
import model.SessionResponse
import model.Student
import model.StudentResponse
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

        // POST /availability/setup
        // Recebe um intervalo (ex: 09:00–11:00)
        // guarda cada slot na tabela timeslots e regista a disponibilidade
        // na tabela availability, devolvendo a lista de slots criados.
        // Body JSON esperado:
        // {
        //   "ownerType": "TEACHER",   <- "TEACHER" ou "STUDENT"
        //   "ownerId": 1,
        //   "dayOfWeek": 2,           <- 1=Segunda ... 7=Domingo
        //   "startTime": "09:00",
        //   "endTime": "11:00"
        // }
        post("/availability/setup") {
            val request = call.receive<AvailabilityRequest>()
            println(">>> ownerType: ${request.ownerType}")
            println(">>> ownerId: ${request.ownerId}")
            val start = LocalTime.parse(request.startTime)
            val end = LocalTime.parse(request.endTime)

            // Valida ownerType
            if (request.ownerType != "TEACHER" && request.ownerType != "STUDENT") {
                call.respond(HttpStatusCode.BadRequest, "ownerType deve ser TEACHER ou STUDENT")
                return@post
            }

            // Fatia o intervalo em slots uniformes
            val slots = AvailabilityService.splitIntoSlots(
                dayOfWeek = request.dayOfWeek,
                startTime = start,
                endTime = end
            )

            val saved = transaction {
                slots.map { slot ->
                    // 1. Insere o timeslot
                    val slotId = TimeSlotTable.insert {
                        it[TimeSlotTable.dayOfWeek] = slot.dayOfWeek
                        it[TimeSlotTable.startTime] = slot.startTime
                        it[TimeSlotTable.endTime] = slot.endTime
                    } get TimeSlotTable.id

                    // 2. Regista a disponibilidade ligada ao teacher ou student
                    AvailabilityTable.insert {
                        it[AvailabilityTable.dayOfWeek] = slot.dayOfWeek
                        it[AvailabilityTable.startTime] = slot.startTime
                        it[AvailabilityTable.endTime] = slot.endTime
                        if (request.ownerType == "TEACHER") {
                            it[AvailabilityTable.teacherId] = request.ownerId
                        } else {
                            it[AvailabilityTable.studentId] = request.ownerId
                        }
                    }

                    AvailabilityResponse(
                        id = slotId.value,
                        ownerId = request.ownerId,
                        ownerType = request.ownerType,
                        dayOfWeek = slot.dayOfWeek,
                        startTime = slot.startTime.toString(),
                        endTime = slot.endTime.toString()
                    )
                }
            }

            call.respond(HttpStatusCode.OK, saved)
        }

        delete("/availability") {
            val ownerType = call.request.queryParameters["ownerType"]
                ?: return@delete call.respond(HttpStatusCode.BadRequest, "ownerType em falta")
            val ownerId = call.request.queryParameters["ownerId"]?.toIntOrNull()
                ?: return@delete call.respond(HttpStatusCode.BadRequest, "ownerId em falta")

            transaction {
                if (ownerType == "TEACHER") {
                    AvailabilityTable.deleteWhere { AvailabilityTable.teacherId eq ownerId }
                } else {
                    AvailabilityTable.deleteWhere { AvailabilityTable.studentId eq ownerId }
                }
            }
            call.respond(HttpStatusCode.OK, mapOf("message" to "Disponibilidades apagadas"))
        }

        // GET /availability?ownerType=TEACHER&ownerId=1
        // Devolve todas as disponibilidades de um professor ou aluno.
        get("/availability") {
            val ownerType = call.request.queryParameters["ownerType"]
                ?: return@get call.respond(
                    HttpStatusCode.BadRequest,
                    "Parâmetro ownerType em falta"
                )
            val ownerId = call.request.queryParameters["ownerId"]?.toIntOrNull()
                ?: return@get call.respond(
                    HttpStatusCode.BadRequest,
                    "Parâmetro ownerId em falta ou inválido"
                )

            val result = transaction {
                val query = if (ownerType == "TEACHER") {
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
            call.respond(HttpStatusCode.OK, result as List<AvailabilityResponse>)
        }

        // POST /teachers
        // Regista um novo professor.
        // Body JSON esperado:
        // {
        //   "name": "João Silva",
        //   "email": "joao@isel.pt",
        //   "sessionDurationMinutes": 60,
        //   "maxParticipantsPerSession": 5
        // }
        post("/teachers") {
            val body = call.receive<Map<String, String>>()
            val name  = body["name"]  ?: return@post call.respond(HttpStatusCode.BadRequest, "name em falta")
            val email = body["email"] ?: return@post call.respond(HttpStatusCode.BadRequest, "email em falta")
            val duration       = body["sessionDurationMinutes"]?.toIntOrNull() ?: 60
            val maxParticipants = body["maxParticipantsPerSession"]?.toIntOrNull() ?: 5

            val newId = transaction {
                TeacherTable.insert {
                    it[TeacherTable.name]                      = name
                    it[TeacherTable.email]                     = email
                    it[TeacherTable.sessionDurationMinutes]    = duration
                    it[TeacherTable.maxParticipantsPerSession] = maxParticipants
                } get TeacherTable.id
            }

            call.respond(HttpStatusCode.Created, mapOf("id" to newId.value.toString()))
        }

        // POST /students
        // Regista um novo aluno.
        // Body JSON esperado:
        // { "name": "Ana Costa", "email": "ana@alunos.isel.pt" }
        post("/students") {
            val body = call.receive<Map<String, String>>()
            val name =
                body["name"] ?: return@post call.respond(HttpStatusCode.BadRequest, "name em falta")
            val email = body["email"] ?: return@post call.respond(
                HttpStatusCode.BadRequest,
                "email em falta"
            )

            transaction {
                StudentTable.insert {
                    it[StudentTable.name] = name
                    it[StudentTable.email] = email
                }
            }

            call.respond(HttpStatusCode.Created, mapOf("message" to "Aluno criado com sucesso"))
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
            call.respond(HttpStatusCode.OK, teachers as List<TeacherResponse>)
        }

        // GET /students
        // Lista todos os alunos.
        get("/students") {
            val students = transaction {
                StudentTable.selectAll().map {
                    StudentResponse(
                        id = it[StudentTable.id].value,
                        name = it[StudentTable.name],
                        email = it[StudentTable.email]
                    )
                }
            }
            call.respond(HttpStatusCode.OK, students as List<StudentResponse>)
        }

        // GET /students/by-teacher/{teacherId}
        get("/students/by-teacher/{teacherId}") {
            val teacherId = call.parameters["teacherId"]?.toIntOrNull()
                ?: return@get call.respond(HttpStatusCode.BadRequest, "teacherId inválido")

            val students = transaction {
                StudentTable
                    .select { StudentTable.teacherId eq teacherId }
                    .map {
                        StudentResponse(
                            id = it[StudentTable.id].value,
                            name = it[StudentTable.name],
                            email = it[StudentTable.email],
                            teacherId = teacherId
                        )
                    }
            }
            call.respond(HttpStatusCode.OK, students)
        }

        // GET /restrictions/{teacherId}
        get("/restrictions/{teacherId}") {
            val teacherId = call.parameters["teacherId"]?.toIntOrNull()
                ?: return@get call.respond(HttpStatusCode.BadRequest, "teacherId inválido")

            val result = transaction {
                RestrictionsTable
                    .select { RestrictionsTable.teacherId eq teacherId }
                    .map {
                        mapOf(
                            "teacherId" to it[RestrictionsTable.teacherId].value.toString(),
                            "sessionDurationMinutes" to it[RestrictionsTable.sessionDurationMinutes].toString(),
                            "maxDailyHours" to it[RestrictionsTable.maxDailyHours].toString(),
                            "maxParticipantsPerSession" to it[RestrictionsTable.maxParticipantsPerSession].toString(),
                            "maxSessionsPerStudentPerDay" to it[RestrictionsTable.maxSessionsPerStudentPerDay].toString()
                        )
                    }.firstOrNull()
            }

            if (result == null) {
                call.respond(HttpStatusCode.NotFound, "Restrições não encontradas")
            } else {
                call.respond(HttpStatusCode.OK, result)
            }
        }

        // PUT /restrictions/{teacherId}
        put("/restrictions/{teacherId}") {
            val teacherId = call.parameters["teacherId"]?.toIntOrNull()
                ?: return@put call.respond(HttpStatusCode.BadRequest, "teacherId inválido")

            val body = call.receive<Map<String, Int>>()

            transaction {
                val exists = RestrictionsTable
                    .select { RestrictionsTable.teacherId eq teacherId }
                    .count() > 0

                if (exists) {
                    RestrictionsTable.update({ RestrictionsTable.teacherId eq teacherId }) {
                        it[sessionDurationMinutes] = body["sessionDurationMinutes"] ?: 60
                        it[maxDailyHours] = body["maxDailyHours"] ?: 3
                        it[maxParticipantsPerSession] = body["maxParticipantsPerSession"] ?: 5
                        it[maxSessionsPerStudentPerDay] = body["maxSessionsPerStudentPerDay"] ?: 1
                    }
                } else {
                    RestrictionsTable.insert {
                        it[RestrictionsTable.teacherId] = teacherId
                        it[sessionDurationMinutes] = body["sessionDurationMinutes"] ?: 60
                        it[maxDailyHours] = body["maxDailyHours"] ?: 3
                        it[maxParticipantsPerSession] = body["maxParticipantsPerSession"] ?: 5
                        it[maxSessionsPerStudentPerDay] = body["maxSessionsPerStudentPerDay"] ?: 1
                    }
                }
            }

            call.respond(HttpStatusCode.OK, mapOf("message" to "Restrições guardadas"))
        }

        // POST /schedule/create
        // Cria horário para o professor com base nas disponibilidades
        post("/schedule/create") {
            val body = call.receive<Map<String, Int>>()
            val teacherId = body["teacherId"]
                ?: return@post call.respond(HttpStatusCode.BadRequest, "teacherId em falta")

            val restrictions = Restrictions(
                sessionDurationMinutes = body["sessionDurationMinutes"] ?: 60,
                maxDailyHours = body["maxDailyHours"] ?: 3,
                maxParticipantsPerSession = body["maxParticipantsPerSession"] ?: 5,
                maxSessionsPerStudentPerDay = body["maxSessionsPerStudentPerDay"] ?: 1
            )

            val result = transaction {

                // Vai buscar slots do professor
                val teacherSlots = AvailabilityTable
                    .select { AvailabilityTable.teacherId eq teacherId }
                    .map {
                        TimeSlot(
                            id = it[AvailabilityTable.id].value,
                            dayOfWeek = it[AvailabilityTable.dayOfWeek],
                            startTime = it[AvailabilityTable.startTime],
                            endTime = it[AvailabilityTable.endTime]
                        )
                    }

                if (teacherSlots.isEmpty()) {
                    return@transaction emptyList<Session>()
                }

                // Vai buscar todos os alunos
                val students = StudentTable.selectAll().map {
                    Student(
                        id = it[StudentTable.id].value,
                        name = it[StudentTable.name],
                        email = it[StudentTable.email],
                        maxDailySessions = it[StudentTable.maxDailySessions]
                    )
                }

                if (students.isEmpty()) {
                    return@transaction emptyList<Session>()
                }

                // Disponibilidades de cada aluno
                val studentAvailabilities = students.associate { student ->
                    student.id to AvailabilityTable
                        .select { AvailabilityTable.studentId eq student.id }
                        .map {
                            TimeSlot(
                                id = it[AvailabilityTable.id].value,
                                dayOfWeek = it[AvailabilityTable.dayOfWeek],
                                startTime = it[AvailabilityTable.startTime],
                                endTime = it[AvailabilityTable.endTime]
                            )
                        }
                }

                // Algoritmo
                ScheduleService.create(
                    teacherId = teacherId,
                    teacherSlots = teacherSlots,
                    students = students,
                    studentAvailabilities = studentAvailabilities,
                    restrictions = restrictions
                )
            }

            // Converte para resposta JSON
            val response = result.map { session ->
                mapOf(
                    "dayOfWeek" to session.slot.dayOfWeek.toString(),
                    "startTime" to session.slot.startTime.toString(),
                    "endTime" to session.slot.endTime.toString(),
                    "studentIds" to session.studentIds.joinToString(",")
                )
            }
            call.respond(HttpStatusCode.OK, response)
        }

        // POST /schedule/save
        // Guarda o horário gerado para o professor poder aceitar ou rejeitar
        post("/schedule/save") {
            val request = call.receive<SaveScheduleRequest>()

            val scheduleId = transaction {
                // 1. Cria o schedule
                val schedId = SchedulesTable.insert {
                    it[teacherId] = request.teacherId
                    it[createdAt] = java.time.LocalDateTime.now()
                    it[status] = "CREATED"
                } get SchedulesTable.id

                // 2. Para cada sessão, cria o timeslot, a session e os enrollments
                request.sessions.forEach { sessionReq ->
                    val slotId = TimeSlotTable.insert {
                        it[dayOfWeek] = sessionReq.dayOfWeek
                        it[startTime] = java.time.LocalTime.parse(sessionReq.startTime)
                        it[endTime] = java.time.LocalTime.parse(sessionReq.endTime)
                    } get TimeSlotTable.id

                    val sessionId = SessionsTable.insert {
                        it[scheduleId] = schedId
                        it[timeslotId] = slotId
                        it[maxCapacity] = sessionReq.studentIds.size
                    } get SessionsTable.id

                    sessionReq.studentIds.forEach { studentId ->
                        SessionEnrollmentsTable.insert {
                            it[SessionEnrollmentsTable.sessionId] = sessionId
                            it[SessionEnrollmentsTable.studentId] = studentId
                            it[enrolledAt] = java.time.LocalDateTime.now()
                        }
                    }
                }

                schedId.value
            }

            call.respond(HttpStatusCode.Created, mapOf("scheduleId" to scheduleId.toString()))
        }

        // PUT /schedule/{id}/accept
        // Professor aceita o horário
        put("/schedule/{id}/accept") {
            val scheduleId = call.parameters["id"]?.toIntOrNull()
                ?: return@put call.respond(HttpStatusCode.BadRequest, "scheduleId inválido")

            transaction {
                SchedulesTable.update({ SchedulesTable.id eq scheduleId }) {
                    it[status] = "ACCEPTED"
                }
            }

            call.respond(HttpStatusCode.OK, mapOf("message" to "Horário aceite"))
        }

        // PUT /schedule/{id}/reject
        // Professor rejeita o horário
        put("/schedule/{id}/reject") {
            val scheduleId = call.parameters["id"]?.toIntOrNull()
                ?: return@put call.respond(HttpStatusCode.BadRequest, "scheduleId inválido")

            transaction {
                SchedulesTable.update({ SchedulesTable.id eq scheduleId }) {
                    it[status] = "REJECTED"
                }
            }

            call.respond(HttpStatusCode.OK, mapOf("message" to "Horário rejeitado"))
        }

        // GET /schedule/{id}
        // Busca o horário com as sessões e alunos
        get("/schedule/{id}") {
            val scheduleId = call.parameters["id"]?.toIntOrNull()
                ?: return@get call.respond(HttpStatusCode.BadRequest, "scheduleId inválido")

            val response = transaction {
                val schedule = SchedulesTable
                    .select { SchedulesTable.id eq scheduleId }
                    .firstOrNull()
                    ?: return@transaction null

                val sessions = SessionsTable
                    .select { SessionsTable.scheduleId eq scheduleId }
                    .map { sessionRow ->
                        val slot = TimeSlotTable
                            .select { TimeSlotTable.id eq sessionRow[SessionsTable.timeslotId] }
                            .first()

                        val studentIds = SessionEnrollmentsTable
                            .select { SessionEnrollmentsTable.sessionId eq sessionRow[SessionsTable.id] }
                            .map { it[SessionEnrollmentsTable.studentId].value }

                        SessionResponse(
                            sessionId = sessionRow[SessionsTable.id].value,
                            dayOfWeek = slot[TimeSlotTable.dayOfWeek],
                            startTime = slot[TimeSlotTable.startTime].toString(),
                            endTime = slot[TimeSlotTable.endTime].toString(),
                            studentIds = studentIds
                        )
                    }

                ScheduleResponse(
                    scheduleId = scheduleId,
                    status = schedule[SchedulesTable.status],
                    sessions = sessions
                )
            }

            if (response == null) {
                call.respond(HttpStatusCode.NotFound, "Horário não encontrado")
            } else {
                call.respond(HttpStatusCode.OK, response)
            }
        }
    }
}