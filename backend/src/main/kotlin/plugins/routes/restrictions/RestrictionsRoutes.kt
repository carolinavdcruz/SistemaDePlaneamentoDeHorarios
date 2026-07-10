package plugins.routes.restrictions

import database.tables.RestrictionsTable
import database.tables.StudentRestrictionsTable
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.put
import model.RestrictionsRequest
import model.RestrictionsResponse
import model.StudentRestrictionsRequest
import model.StudentRestrictionsResponse
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update

fun Route.restrictionsRoutes() {
    // GET /restrictions/{teacherId}
    // Devolve as restrições de um professor.
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

    // PUT /restrictions/{teacherId}
    // Atualiza ou cria as restrições de um professor.
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

    // GET /studentRestrictions/{studentId}
    // Devolve as restrições de um aluno.
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

    // PUT /studentRestrictions/{studentId}
    // Atualiza ou cria as restrições de um aluno.
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
}