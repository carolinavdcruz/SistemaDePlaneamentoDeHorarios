package plugins

import io.ktor.server.application.call
import io.ktor.server.response.respond
import io.ktor.server.routing.get
import io.ktor.server.routing.routing

/*
@Suppress("NewApi")
fun Application.configureRouting() {
    routing {
        get("/health") { call.respond(HttpStatusCode.OK, mapOf("status" to "ok")) }
        teacherRoutes()
        studentRoutes()
        availabilityRoutes()
        restrictionsRoutes()
        scheduleRoutes()
        authRoutes()
    }
}

 */

import database.tables.AvailabilityTable
import database.tables.LessonStudentTable
import database.tables.LessonTable
import database.tables.RestrictionsTable
import database.tables.StudentRestrictionsTable
import database.tables.StudentTable
import database.tables.TeacherTable
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.request.receive
import io.ktor.server.routing.post
import io.ktor.server.routing.delete
import io.ktor.server.routing.patch
import io.ktor.server.routing.put
import model.AssignTeacherRequest
import model.AvailabilityRequest
import model.AvailabilityResponse
import model.GenerateLessonsRequest
import model.LessonResponse
import model.LessonStudentResponse
import model.LoginRequest
import model.LoginResponse
import model.MarkAttendanceRequest
import model.NotifyStudentsRequest
import model.NotifyStudentsResponse
import model.OwnerType
import model.UpdateLessonRequest
import model.CancelSeriesResponse
import model.LessonConflictResponse
import model.AttendanceSummaryResponse
import model.RecurrenceType
import model.Restrictions
import model.RestrictionsRequest
import model.RestrictionsResponse
import model.ScheduleCreateRequest
import model.ScheduleSessionResponse
import model.Student
import model.StudentRequest
import model.StudentResponse
import model.StudentRestrictionsRequest
import model.StudentRestrictionsResponse
import model.TeacherRequest
import model.TeacherResponse
import model.TimeSlot
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.inList
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import org.mindrot.jbcrypt.BCrypt
import service.AvailabilityService
import service.EmailService
import service.LessonService
import service.ScheduleService
import java.time.LocalDate
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
                    it[password] = BCrypt.hashpw(request.password, BCrypt.gensalt())
                } get TeacherTable.id

                TeacherResponse(
                    id = id.value,
                    name = request.name,
                    email = request.email,
                    //password = request.password
                )
            }
            call.respond(HttpStatusCode.Created, created)
        }

        // GET /teachers
        // Lista todos os professores.
        get("/teachers") {
            val teachers = transaction {
                TeacherTable.selectAll().map {
                    TeacherResponse(
                        id = it[TeacherTable.id].value,
                        name = it[TeacherTable.name],
                        email = it[TeacherTable.email],
                        //password = it[TeacherTable.password]
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
                    it[StudentTable.name] = request.name
                    it[StudentTable.email] = request.email
                    it[StudentTable.password] = BCrypt.hashpw(request.password, BCrypt.gensalt())
                    it[StudentTable.teacherId] = request.teacherId
                } get StudentTable.id

                StudentResponse(
                    id = id.value,
                    name = request.name,
                    email = request.email,
                    //password = request.password,
                    teacherId = request.teacherId,
                )
            }
            call.respond(HttpStatusCode.Created, created)
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
                        //password = it[StudentTable.password],
                        teacherId = it[StudentTable.teacherId]?.value,
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
                            teacherId = it[StudentTable.teacherId]?.value,
                            //password = it[StudentTable.password]
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
                ?: return@get call.respond(
                    HttpStatusCode.BadRequest,
                    mapOf("error" to "ownerId inválido")
                )

            val ownerTypeParam = call.request.queryParameters["ownerType"]
                ?: return@get call.respond(
                    HttpStatusCode.BadRequest,
                    mapOf("error" to "ownerType em falta")
                )

            val ownerType = try {
                OwnerType.valueOf(ownerTypeParam)
            } catch (e: IllegalArgumentException) {
                return@get call.respond(
                    HttpStatusCode.BadRequest,
                    mapOf("error" to "ownerType inválido")
                )
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
                ?: return@delete call.respond(
                    HttpStatusCode.BadRequest,
                    mapOf("error" to "ownerId inválido")
                )

            val ownerTypeParam = call.request.queryParameters["ownerType"]
                ?: return@delete call.respond(
                    HttpStatusCode.BadRequest,
                    mapOf("error" to "ownerType em falta")
                )

            val ownerType = try {
                OwnerType.valueOf(ownerTypeParam)
            } catch (e: IllegalArgumentException) {
                return@delete call.respond(
                    HttpStatusCode.BadRequest,
                    mapOf("error" to "ownerType inválido")
                )
            }

            transaction {
                if (ownerType == OwnerType.TEACHER) {
                    AvailabilityTable.deleteWhere { teacherId eq ownerId }
                } else {
                    AvailabilityTable.deleteWhere { studentId eq ownerId }
                }
            }

            call.respond(
                HttpStatusCode.OK,
                mapOf("message" to "Disponibilidade removida com sucesso")
            )
        }

        get("/restrictions/{teacherId}") {
            val teacherId = call.parameters["teacherId"]?.toIntOrNull()
                ?: return@get call.respond(
                    HttpStatusCode.BadRequest,
                    mapOf("error" to "teacherId inválido")
                )

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
                call.respond(
                    HttpStatusCode.NotFound,
                    mapOf("error" to "Restrições não encontradas")
                )
                return@get
            }

            call.respond(HttpStatusCode.OK, restrictions)
        }

        put("/restrictions/{teacherId}") {
            val teacherId = call.parameters["teacherId"]?.toIntOrNull()
                ?: return@put call.respond(
                    HttpStatusCode.BadRequest,
                    mapOf("error" to "teacherId inválido")
                )

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

        get("/studentRestrictions/{studentId}") {
            val studentId = call.parameters["studentId"]?.toIntOrNull()
                ?: return@get call.respond(
                    HttpStatusCode.BadRequest,
                    mapOf("error" to "studentId inválido")
                )

            val restrictions = transaction {
                StudentRestrictionsTable
                    .select { StudentRestrictionsTable.studentId eq studentId }
                    .singleOrNull()
                    ?.let {
                        StudentRestrictionsResponse(
                            studentId = it[StudentRestrictionsTable.studentId].value,
                            weeklyHours = it[StudentRestrictionsTable.weeklyHours]
                        )
                    }
            }

            if (restrictions == null) {
                call.respond(
                    HttpStatusCode.NotFound,
                    mapOf("error" to "Restrições do aluno não encontradas")
                )
                return@get
            }

            call.respond(HttpStatusCode.OK, restrictions)
        }

        put("/studentRestrictions/{studentId}") {
            val studentId = call.parameters["studentId"]?.toIntOrNull()
                ?: return@put call.respond(
                    HttpStatusCode.BadRequest,
                    mapOf("error" to "studentId inválido")
                )

            val request = call.receive<StudentRestrictionsRequest>()

            val updatedRows = transaction {
                StudentRestrictionsTable.update({ StudentRestrictionsTable.studentId eq studentId }) {
                    it[weeklyHours] = request.weeklyHours
                }
            }

            if (updatedRows == 0) {
                transaction {
                    StudentRestrictionsTable.insert {
                        it[StudentRestrictionsTable.studentId] = studentId
                        it[weeklyHours] = request.weeklyHours
                    }
                }
            }

            call.respond(
                HttpStatusCode.OK,
                mapOf("message" to "Restrições do aluno guardadas com sucesso")
            )
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
                            maxDailySessions = it[StudentTable.maxDailySessions],
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
                call.respond(
                    HttpStatusCode.NotFound,
                    mapOf("error" to "Restrições do professor não encontradas")
                )
                return@post
            }

            call.respond(HttpStatusCode.OK, response)
        }

        // POST /lessons/generate
        // Gera e PERSISTE aulas concretas (com data) a partir das disponibilidades/restrições
        // do professor, opcionalmente repetindo a mesma semana N vezes (recorrência).
        // Body JSON esperado:
        // {
        //   "teacherId": 1,
        //   "startDate": "2026-09-07",   <- segunda-feira da semana de início
        //   "recurrence": "WEEKLY",      <- "NONE" ou "WEEKLY"
        //   "occurrences": 4             <- nº de semanas a gerar
        // }
        post("/lessons/generate") {
            val request = call.receive<GenerateLessonsRequest>()
            val teacherId = request.teacherId
            val startDate = LocalDate.parse(request.startDate)
            val occurrences = if (request.recurrence == RecurrenceType.NONE) 1 else request.occurrences.coerceAtLeast(1)
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
                            ).map { slot -> TimeSlot(
                                dayOfWeek = slot.dayOfWeek,
                                startTime = slot.startTime,
                                endTime = slot.endTime
                            ) }
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
                )
            }

            if (sessions == null) {
                call.respond(HttpStatusCode.NotFound, mapOf("error" to "Restrições do professor não encontradas"))
                return@post
            }

            val lessons = LessonService.persistRecurring(
                teacherId = teacherId,
                sessions = sessions,
                startDate = startDate,
                occurrences = occurrences
            )

            call.respond(HttpStatusCode.Created, lessons.map { it.toResponse() })
        }

        // GET /lessons/history?teacherId=1&from=2026-09-01&to=2026-09-30
        // Devolve as aulas de um professor num intervalo de datas.
        get("/lessons/history") {
            val teacherId = call.request.queryParameters["teacherId"]?.toIntOrNull()
                ?: return@get call.respond(HttpStatusCode.BadRequest, mapOf("error" to "teacherId inválido"))
            val from = call.request.queryParameters["from"]?.let { LocalDate.parse(it) }
                ?: return@get call.respond(HttpStatusCode.BadRequest, mapOf("error" to "from inválido"))
            val to = call.request.queryParameters["to"]?.let { LocalDate.parse(it) }
                ?: return@get call.respond(HttpStatusCode.BadRequest, mapOf("error" to "to inválido"))

            val lessons = LessonService.getHistory(teacherId, from, to)
            call.respond(HttpStatusCode.OK, lessons.map { it.toResponse() })
        }

        // GET /lessons/week?teacherId=1&date=2026-09-10
        // Devolve o horário (todas as aulas) da semana (segunda a domingo) que contém `date`.
        get("/lessons/week") {
            val teacherId = call.request.queryParameters["teacherId"]?.toIntOrNull()
                ?: return@get call.respond(HttpStatusCode.BadRequest, mapOf("error" to "teacherId inválido"))
            val date = call.request.queryParameters["date"]?.let { LocalDate.parse(it) }
                ?: return@get call.respond(HttpStatusCode.BadRequest, mapOf("error" to "date inválido"))

            val lessons = LessonService.getHistoryForWeek(teacherId, date)
            call.respond(HttpStatusCode.OK, lessons.map { it.toResponse() })
        }

        // GET /lessons/student/week?studentId=1&date=2026-09-10
        // Horário do ALUNO (qualquer professor) para a semana que contém `date`.
        // É esta rota que alimenta o ecrã principal do aluno.
        get("/lessons/student/week") {
            val studentId = call.request.queryParameters["studentId"]?.toIntOrNull()
                ?: return@get call.respond(HttpStatusCode.BadRequest, mapOf("error" to "studentId inválido"))
            val date = call.request.queryParameters["date"]?.let { LocalDate.parse(it) }
                ?: return@get call.respond(HttpStatusCode.BadRequest, mapOf("error" to "date inválido"))

            val lessons = LessonService.getWeekForStudent(studentId, date)
            call.respond(HttpStatusCode.OK, lessons.map { it.toResponse() })
        }

        // GET /lessons/student/history?studentId=1&from=...&to=...
        get("/lessons/student/history") {
            val studentId = call.request.queryParameters["studentId"]?.toIntOrNull()
                ?: return@get call.respond(HttpStatusCode.BadRequest, mapOf("error" to "studentId inválido"))
            val from = call.request.queryParameters["from"]?.let { LocalDate.parse(it) }
                ?: return@get call.respond(HttpStatusCode.BadRequest, mapOf("error" to "from inválido"))
            val to = call.request.queryParameters["to"]?.let { LocalDate.parse(it) }
                ?: return@get call.respond(HttpStatusCode.BadRequest, mapOf("error" to "to inválido"))

            val lessons = LessonService.getHistoryForStudent(studentId, from, to)
            call.respond(HttpStatusCode.OK, lessons.map { it.toResponse() })
        }

        // PATCH /lessons/{lessonId}/students/{studentId}/attendance
        // Marca presença/falta de um aluno numa aula concreta.
        // Body JSON esperado: { "attended": true }
        patch("/lessons/{lessonId}/students/{studentId}/attendance") {
            val lessonId = call.parameters["lessonId"]?.toIntOrNull()
                ?: return@patch call.respond(HttpStatusCode.BadRequest, mapOf("error" to "lessonId inválido"))
            val studentId = call.parameters["studentId"]?.toIntOrNull()
                ?: return@patch call.respond(HttpStatusCode.BadRequest, mapOf("error" to "studentId inválido"))
            val request = call.receive<MarkAttendanceRequest>()

            val updated = LessonService.markAttendance(lessonId, studentId, request.attended)
            if (!updated) {
                call.respond(HttpStatusCode.NotFound, mapOf("error" to "Aluno não encontrado nesta aula"))
                return@patch
            }
            call.respond(HttpStatusCode.OK, mapOf("message" to "Presença atualizada com sucesso"))
        }

        // GET /lessons/students/{studentId}/attendance-summary
        // Resumo de presenças do aluno (todas as aulas, qualquer professor).
        get("/lessons/students/{studentId}/attendance-summary") {
            val studentId = call.parameters["studentId"]?.toIntOrNull()
                ?: return@get call.respond(HttpStatusCode.BadRequest, mapOf("error" to "studentId inválido"))

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

        // GET /lessons/{lessonId}
        // Devolve uma única aula pelo id.
        get("/lessons/{lessonId}") {
            val lessonId = call.parameters["lessonId"]?.toIntOrNull()
                ?: return@get call.respond(HttpStatusCode.BadRequest, mapOf("error" to "lessonId inválido"))

            val lesson = LessonService.getById(lessonId)
            if (lesson == null) {
                call.respond(HttpStatusCode.NotFound, mapOf("error" to "Aula não encontrada"))
                return@get
            }
            call.respond(HttpStatusCode.OK, lesson.toResponse())
        }

        // DELETE /lessons/series/{seriesId}
        // Cancela todas as ocorrências SCHEDULED de uma série de recorrência.
        // Não apaga histórico, só marca status = CANCELLED.
        delete("/lessons/series/{seriesId}") {
            val seriesId = call.parameters["seriesId"]
                ?: return@delete call.respond(HttpStatusCode.BadRequest, mapOf("error" to "seriesId inválido"))

            // Recolhe professor + alunos afetados ANTES de cancelar (para poder notificar depois).
            val affected = transaction {
                val lessonRows = LessonTable.select { LessonTable.seriesId eq seriesId }.toList()
                val teacherId = lessonRows.firstOrNull()?.get(LessonTable.teacherId)?.value
                val lessonIds = lessonRows.map { it[LessonTable.id].value }
                val studentIds = if (lessonIds.isEmpty()) emptyList() else {
                    LessonStudentTable
                        .select { LessonStudentTable.lessonId inList lessonIds }
                        .map { it[LessonStudentTable.studentId].value }
                        .distinct()
                }
                teacherId?.let { it to studentIds }
            }

            val cancelledCount = LessonService.cancelSeries(seriesId)

            if (affected != null && cancelledCount > 0) {
                val (teacherId, studentIds) = affected
                notifyStudentIds(teacherId, studentIds) { studentEmail, studentName, teacherName ->
                    EmailService.notifySeriesCancelled(
                        studentEmail = studentEmail,
                        studentName = studentName,
                        teacherName = teacherName,
                        affectedCount = cancelledCount
                    )
                }
            }

            call.respond(HttpStatusCode.OK, CancelSeriesResponse(cancelledCount))
        }

        // PATCH /lessons/{lessonId}/cancel
        // Cancela uma única aula (não mexe nas restantes da série, se houver).
        patch("/lessons/{lessonId}/cancel") {
            val lessonId = call.parameters["lessonId"]?.toIntOrNull()
                ?: return@patch call.respond(HttpStatusCode.BadRequest, mapOf("error" to "lessonId inválido"))

            // Guarda os dados da aula ANTES de cancelar, para poder notificar os alunos.
            val lessonBeforeCancel = LessonService.getById(lessonId)

            val cancelled = LessonService.cancelLesson(lessonId)
            if (!cancelled) {
                call.respond(HttpStatusCode.NotFound, mapOf("error" to "Aula não encontrada"))
                return@patch
            }

            if (lessonBeforeCancel != null) {
                notifyStudentsOfLesson(lessonBeforeCancel) { studentEmail, studentName, teacherName ->
                    EmailService.notifyLessonCancelled(
                        studentEmail = studentEmail,
                        studentName = studentName,
                        teacherName = teacherName,
                        date = lessonBeforeCancel.date,
                        startTime = lessonBeforeCancel.startTime,
                        endTime = lessonBeforeCancel.endTime
                    )
                }
            }

            call.respond(HttpStatusCode.OK, mapOf("message" to "Aula cancelada com sucesso"))
        }

        // PATCH /lessons/{lessonId}
        // Edita data/hora de UMA ocorrência isolada (não afeta o resto da série).
        // Ao editar, a aula deixa de pertencer à série (seriesId -> null).
        // Body JSON esperado (todos os campos opcionais):
        // { "date": "2026-09-14", "startTime": "10:00", "endTime": "11:00" }
        patch("/lessons/{lessonId}") {
            val lessonId = call.parameters["lessonId"]?.toIntOrNull()
                ?: return@patch call.respond(HttpStatusCode.BadRequest, mapOf("error" to "lessonId inválido"))
            val request = call.receive<UpdateLessonRequest>()

            // Guarda o estado anterior para poder comparar e notificar em caso de remarcação.
            val lessonBeforeUpdate = LessonService.getById(lessonId)

            val result = LessonService.updateLesson(
                lessonId = lessonId,
                date = request.date?.let { LocalDate.parse(it) },
                startTime = request.startTime?.let { LocalTime.parse(it) },
                endTime = request.endTime?.let { LocalTime.parse(it) }
            )

            when (result) {
                is LessonService.UpdateLessonResult.NotFound ->
                    call.respond(HttpStatusCode.NotFound, mapOf("error" to "Aula não encontrada"))
                is LessonService.UpdateLessonResult.Conflict ->
                    call.respond(
                        HttpStatusCode.Conflict,
                        LessonConflictResponse(
                            error = "O professor já tem outra aula nesse horário",
                            conflictingLessonId = result.conflictingLessonId
                        )
                    )
                is LessonService.UpdateLessonResult.Success -> {
                    val updatedLesson = result.lesson
                    val actuallyChanged = lessonBeforeUpdate != null && (
                            lessonBeforeUpdate.date != updatedLesson.date ||
                                    lessonBeforeUpdate.startTime != updatedLesson.startTime ||
                                    lessonBeforeUpdate.endTime != updatedLesson.endTime
                            )

                    if (actuallyChanged && lessonBeforeUpdate != null) {
                        notifyStudentsOfLesson(updatedLesson) { studentEmail, studentName, teacherName ->
                            EmailService.notifyLessonRescheduled(
                                studentEmail = studentEmail,
                                studentName = studentName,
                                teacherName = teacherName,
                                oldDate = lessonBeforeUpdate.date,
                                oldStart = lessonBeforeUpdate.startTime,
                                oldEnd = lessonBeforeUpdate.endTime,
                                newDate = updatedLesson.date,
                                newStart = updatedLesson.startTime,
                                newEnd = updatedLesson.endTime
                            )
                        }
                    }

                    call.respond(HttpStatusCode.OK, updatedLesson.toResponse())
                }
            }
        }

        // POST /teachers/{teacherId}/notify
        // Envia um email/aviso livre, escrito pelo professor, a um conjunto de alunos
        // (ou a todos os alunos do professor, se studentIds vier vazio/null).
        // Body JSON esperado:
        // {
        //   "studentIds": [1, 2],       <- opcional; omitir/vazio = todos os alunos do professor
        //   "subject": "Aviso importante",
        //   "message": "A aula de amanhã vai ter início 15 minutos mais tarde."
        // }
        post("/teachers/{teacherId}/notify") {
            val teacherId = call.parameters["teacherId"]?.toIntOrNull()
                ?: return@post call.respond(HttpStatusCode.BadRequest, mapOf("error" to "teacherId inválido"))
            val request = call.receive<NotifyStudentsRequest>()

            val teacherName = transaction {
                TeacherTable.select { TeacherTable.id eq teacherId }.singleOrNull()?.get(TeacherTable.name)
            } ?: return@post call.respond(HttpStatusCode.NotFound, mapOf("error" to "Professor não encontrado"))

            val students = transaction {
                val query = if (request.studentIds.isNullOrEmpty()) {
                    StudentTable.select { StudentTable.teacherId eq teacherId }
                } else {
                    StudentTable.select {
                        (StudentTable.teacherId eq teacherId) and (StudentTable.id inList request.studentIds)
                    }
                }
                query.map { it[StudentTable.name] to it[StudentTable.email] }
            }

            students.forEach { (studentName, studentEmail) ->
                EmailService.notifyCustom(
                    studentEmail = studentEmail,
                    studentName = studentName,
                    teacherName = teacherName,
                    subject = request.subject,
                    message = request.message
                )
            }

            call.respond(HttpStatusCode.OK, NotifyStudentsResponse(sentTo = students.size))
        }

        post("/login") {
            val request = call.receive<LoginRequest>()

            val result = transaction {
                val student = StudentTable
                    .select { StudentTable.email eq request.email }
                    .singleOrNull()

                if (student != null && BCrypt.checkpw(request.password, student[StudentTable.password])) {
                    return@transaction LoginResponse(
                        userId = student[StudentTable.id].value,
                        ownerType = OwnerType.STUDENT
                    )
                }

                val teacher = TeacherTable
                    .select { TeacherTable.email eq request.email }
                    .singleOrNull()

                if (teacher != null && BCrypt.checkpw(request.password, teacher[TeacherTable.password])) {
                    return@transaction LoginResponse(
                        userId = teacher[TeacherTable.id].value,
                        ownerType = OwnerType.TEACHER
                    )
                }

                null
            }

            if (result == null) {
                call.respond(
                    HttpStatusCode.Unauthorized,
                    mapOf("error" to "Email ou password incorretos")
                )
            } else {
                call.respond(HttpStatusCode.OK, result)
            }
        }

    }
}

/**
 * Vai buscar o nome do professor e o nome/email de cada aluno da aula,
 * e invoca [action] para cada aluno (studentEmail, studentName, teacherName).
 * Usado para disparar notificações por email após cancelar/remarcar uma aula.
 */
private fun notifyStudentsOfLesson(
    lesson: model.Lesson,
    action: (studentEmail: String, studentName: String, teacherName: String) -> Unit
) {
    notifyStudentIds(lesson.teacherId, lesson.students.map { it.studentId }, action)
}

/** Igual a [notifyStudentsOfLesson], mas recebendo diretamente a lista de ids de alunos. */
private fun notifyStudentIds(
    teacherId: Int,
    studentIds: List<Int>,
    action: (studentEmail: String, studentName: String, teacherName: String) -> Unit
) {
    if (studentIds.isEmpty()) return

    val (teacherName, students) = transaction {
        val name = TeacherTable
            .select { TeacherTable.id eq teacherId }
            .singleOrNull()
            ?.get(TeacherTable.name)
            ?: "O seu professor"

        val studentsInfo = StudentTable
            .select { StudentTable.id inList studentIds }
            .map { it[StudentTable.email] to it[StudentTable.name] }

        name to studentsInfo
    }

    students.forEach { (email, name) ->
        action(email, name, teacherName)
    }
}

private fun model.Lesson.toResponse() = LessonResponse(
    id = id,
    teacherId = teacherId,
    seriesId = seriesId,
    date = date.toString(),
    startTime = startTime.toString(),
    endTime = endTime.toString(),
    status = status.name,
    students = students.map {
        LessonStudentResponse(
            studentId = it.studentId,
            attended = it.attended,
            attendedAt = it.attendedAt?.toString()
        )
    }
)
