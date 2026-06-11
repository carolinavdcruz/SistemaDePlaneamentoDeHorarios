package plugins.routes.restrictions

import plugins.intParam
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
    get("/restrictions/{teacherId}") {
        val teacherId = call.intParam("teacherId") ?: return@get
        val restrictions = transaction {
            RestrictionsTable.select { RestrictionsTable.teacherId eq teacherId }
                .singleOrNull()?.let {
                    RestrictionsResponse(
                        teacherId = it[RestrictionsTable.teacherId].value,
                        maxDailyHours = it[RestrictionsTable.maxDailyHours],
                        sessionDurationMinutes = it[RestrictionsTable.sessionDurationMinutes],
                        maxParticipantsPerSession = it[RestrictionsTable.maxParticipantsPerSession],
                        maxSessionsPerStudentPerDay = it[RestrictionsTable.maxSessionsPerStudentPerDay]
                    )
                }
        } ?: return@get call.respond(HttpStatusCode.NotFound, mapOf("error" to "Restrições não encontradas"))
        call.respond(HttpStatusCode.OK, restrictions)
    }

    put("/restrictions/{teacherId}") {
        val teacherId = call.intParam("teacherId") ?: return@put
        val req = call.receive<RestrictionsRequest>()
        transaction {
            val updated = RestrictionsTable.update({ RestrictionsTable.teacherId eq teacherId }) {
                it[maxDailyHours] = req.maxDailyHours
                it[sessionDurationMinutes] = req.sessionDurationMinutes
                it[maxParticipantsPerSession] = req.maxParticipantsPerSession
                it[maxSessionsPerStudentPerDay] = req.maxSessionsPerStudentPerDay
            }
            if (updated == 0) RestrictionsTable.insert {
                it[RestrictionsTable.teacherId] = teacherId
                it[maxDailyHours] = req.maxDailyHours
                it[sessionDurationMinutes] = req.sessionDurationMinutes
                it[maxParticipantsPerSession] = req.maxParticipantsPerSession
                it[maxSessionsPerStudentPerDay] = req.maxSessionsPerStudentPerDay
            }
        }
        call.respond(HttpStatusCode.OK, mapOf("message" to "Restrições guardadas com sucesso"))
    }

    get("/studentRestrictions/{studentId}") {
        val studentId = call.intParam("studentId") ?: return@get
        val restrictions = transaction {
            StudentRestrictionsTable.select { StudentRestrictionsTable.studentId eq studentId }
                .singleOrNull()?.let {
                    StudentRestrictionsResponse(
                        studentId = it[StudentRestrictionsTable.studentId].value,
                        weeklyHours = it[StudentRestrictionsTable.weeklyHours]
                    )
                }
        } ?: return@get call.respond(HttpStatusCode.NotFound, mapOf("error" to "Restrições do aluno não encontradas"))
        call.respond(HttpStatusCode.OK, restrictions)
    }

    put("/studentRestrictions/{studentId}") {
        val studentId = call.intParam("studentId") ?: return@put
        val req = call.receive<StudentRestrictionsRequest>()
        transaction {
            val updated = StudentRestrictionsTable.update({ StudentRestrictionsTable.studentId eq studentId }) {
                it[weeklyHours] = req.weeklyHours
            }
            if (updated == 0) StudentRestrictionsTable.insert {
                it[StudentRestrictionsTable.studentId] = studentId
                it[weeklyHours] = req.weeklyHours
            }
        }
        call.respond(HttpStatusCode.OK, mapOf("message" to "Restrições do aluno guardadas com sucesso"))
    }
}