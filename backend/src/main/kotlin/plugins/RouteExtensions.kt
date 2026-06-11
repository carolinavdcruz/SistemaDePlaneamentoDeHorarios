package plugins

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.response.respond
import model.OwnerType

// Devolve o Int do path param ou responde 400 e devolve null
suspend fun ApplicationCall.intParam(name: String): Int? {
    val value = parameters[name]?.toIntOrNull()
    if (value == null) respond(HttpStatusCode.BadRequest, mapOf("error" to "$name inválido"))
    return value
}

// Devolve (ownerId, ownerType) dos query params ou responde 400 e devolve null
suspend fun ApplicationCall.ownerParams(): Pair<Int, OwnerType>? {
    val ownerId = request.queryParameters["ownerId"]?.toIntOrNull()
        ?: run { respond(HttpStatusCode.BadRequest, mapOf("error" to "ownerId inválido")); return null }
    val ownerTypeParam = request.queryParameters["ownerType"]
        ?: run { respond(HttpStatusCode.BadRequest, mapOf("error" to "ownerType em falta")); return null }
    val ownerType = try { OwnerType.valueOf(ownerTypeParam) }
    catch (e: IllegalArgumentException) {
        respond(HttpStatusCode.BadRequest, mapOf("error" to "ownerType inválido")); return null
    }
    return ownerId to ownerType
}