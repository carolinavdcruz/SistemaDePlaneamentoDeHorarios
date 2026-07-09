package plugins.routes.availability

import database.tables.AvailabilityTable
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import model.AvailabilityRequest
import model.AvailabilityResponse
import model.OwnerType
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import plugins.ownerParams
import java.time.LocalTime

fun Route.availabilityRoutes() {
    // POST /availability
    // Recebe um intervalo (ex: 09:00–11:00) guarda cada slot na tabela timeslots
    // e regista a disponibilidade na tabela availability, devolvendo a lista de slots criados.
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

                if (request.ownerType == OwnerType.TEACHER) { it[teacherId] = request.ownerId
                } else { it[studentId] = request.ownerId }
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
}
