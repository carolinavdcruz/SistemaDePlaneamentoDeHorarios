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
    post("/availability") {
        val req = call.receive<AvailabilityRequest>()
        val savedId = transaction {
            AvailabilityTable.insert {
                it[dayOfWeek] = req.dayOfWeek
                it[startTime] = parseTime(req.startTime)
                it[endTime]   = parseTime(req.endTime)
                if (req.ownerType == OwnerType.TEACHER) it[teacherId] = req.ownerId
                else it[studentId] = req.ownerId
            } get AvailabilityTable.id
        }
        call.respond(HttpStatusCode.Created, AvailabilityResponse(
            id = savedId.value, ownerId = req.ownerId, ownerType = req.ownerType,
            dayOfWeek = req.dayOfWeek, startTime = req.startTime, endTime = req.endTime
        ))
    }

    get("/availability") {
        val (ownerId, ownerType) = call.ownerParams() ?: return@get
        val result = transaction {
            val query = if (ownerType == OwnerType.TEACHER)
                AvailabilityTable.select { AvailabilityTable.teacherId eq ownerId }
            else
                AvailabilityTable.select { AvailabilityTable.studentId eq ownerId }
            query.map {
                AvailabilityResponse(
                    id = it[AvailabilityTable.id].value, ownerId = ownerId, ownerType = ownerType,
                    dayOfWeek = it[AvailabilityTable.dayOfWeek],
                    startTime = it[AvailabilityTable.startTime].toString(),
                    endTime   = it[AvailabilityTable.endTime].toString()
                )
            }
        }
        call.respond(HttpStatusCode.OK, result)
    }

    delete("/availability") {
        val (ownerId, ownerType) = call.ownerParams() ?: return@delete
        transaction {
            if (ownerType == OwnerType.TEACHER) AvailabilityTable.deleteWhere { teacherId eq ownerId }
            else AvailabilityTable.deleteWhere { studentId eq ownerId }
        }
        call.respond(HttpStatusCode.OK, mapOf("message" to "Disponibilidade removida com sucesso"))
    }
}

fun parseTime(value: String): LocalTime {
    val (hour, minute) = value.split(":").map { it.toInt() }
    return LocalTime.of(hour, minute)
}