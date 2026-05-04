package plugins

import database.tables.AvailabilityTable
import database.tables.StudentTable
import database.tables.TeacherTable
import database.tables.TimeSlotTable
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.call
import io.ktor.server.request.receive
import io.ktor.server.request.receiveText
import io.ktor.server.response.respond
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.routing
import model.AvailabilityRequest
import model.AvailabilityResponse
import model.StudentResponse
import model.TeacherResponse
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import service.AvailabilityService
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
            val start   = LocalTime.parse(request.startTime)
            val end     = LocalTime.parse(request.endTime)

            // Valida ownerType
            if (request.ownerType != "TEACHER" && request.ownerType != "STUDENT") {
                call.respond(HttpStatusCode.BadRequest, "ownerType deve ser TEACHER ou STUDENT")
                return@post
            }

            // Fatia o intervalo em slots uniformes
            val slots = AvailabilityService.splitIntoSlots(
                dayOfWeek  = request.dayOfWeek,
                startTime  = start,
                endTime    = end
            )

            val saved = transaction {
                slots.map { slot ->
                    // 1. Insere o timeslot
                    val slotId = TimeSlotTable.insert {
                        it[TimeSlotTable.dayOfWeek]  = slot.dayOfWeek
                        it[TimeSlotTable.startTime]  = slot.startTime
                        it[TimeSlotTable.endTime]    = slot.endTime
                    } get TimeSlotTable.id

                    // 2. Regista a disponibilidade ligada ao teacher ou student
                    AvailabilityTable.insert {
                        it[AvailabilityTable.dayOfWeek]  = slot.dayOfWeek
                        it[AvailabilityTable.startTime]  = slot.startTime
                        it[AvailabilityTable.endTime]    = slot.endTime
                        if (request.ownerType == "TEACHER") {
                            it[AvailabilityTable.teacherId] = request.ownerId
                        } else {
                            it[AvailabilityTable.studentId] = request.ownerId
                        }
                    }

                    AvailabilityResponse(
                        id        = slotId.value,
                        ownerId   = request.ownerId,
                        ownerType = request.ownerType,
                        dayOfWeek = slot.dayOfWeek,
                        startTime = slot.startTime.toString(),
                        endTime   = slot.endTime.toString()
                    )
                }
            }

            call.respond(HttpStatusCode.OK, saved)
        }

        // GET /availability?ownerType=TEACHER&ownerId=1
        // Devolve todas as disponibilidades de um professor ou aluno.
        get("/availability") {
            val ownerType = call.request.queryParameters["ownerType"]
                ?: return@get call.respond(HttpStatusCode.BadRequest, "Parâmetro ownerType em falta")
            val ownerId = call.request.queryParameters["ownerId"]?.toIntOrNull()
                ?: return@get call.respond(HttpStatusCode.BadRequest, "Parâmetro ownerId em falta ou inválido")

            val result = transaction {
                val query = if (ownerType == "TEACHER") {
                    AvailabilityTable.select { AvailabilityTable.teacherId eq ownerId }
                } else {
                    AvailabilityTable.select { AvailabilityTable.studentId eq ownerId }
                }

                query.map {
                    AvailabilityResponse(
                        id        = it[AvailabilityTable.id].value,
                        ownerId   = ownerId,
                        ownerType = ownerType,
                        dayOfWeek = it[AvailabilityTable.dayOfWeek],
                        startTime = it[AvailabilityTable.startTime].toString(),
                        endTime   = it[AvailabilityTable.endTime].toString()
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
            val duration     = body["sessionDurationMinutes"]?.toIntOrNull() ?: 60
            val maxParticipants = body["maxParticipantsPerSession"]?.toIntOrNull() ?: 5

            transaction {
                TeacherTable.insert {
                    it[TeacherTable.name]                      = name
                    it[TeacherTable.email]                     = email
                    it[TeacherTable.sessionDurationMinutes]    = duration
                    it[TeacherTable.maxParticipantsPerSession] = maxParticipants
                }
            }

            call.respond(HttpStatusCode.Created, mapOf("message" to "Professor criado com sucesso"))
        }

        // POST /students
        // Regista um novo aluno.
        // Body JSON esperado:
        // { "name": "Ana Costa", "email": "ana@alunos.isel.pt" }
        post("/students") {
            val body  = call.receive<Map<String, String>>()
            val name  = body["name"]  ?: return@post call.respond(HttpStatusCode.BadRequest, "name em falta")
            val email = body["email"] ?: return@post call.respond(HttpStatusCode.BadRequest, "email em falta")

            transaction {
                StudentTable.insert {
                    it[StudentTable.name]  = name
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

        post("/schedule/create") {
            TODO()
        }
    }
}