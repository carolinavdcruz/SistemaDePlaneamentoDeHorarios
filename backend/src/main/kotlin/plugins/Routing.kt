package plugins

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.call
import io.ktor.server.response.respond
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import plugins.routes.auth.authRoutes
import plugins.routes.availability.availabilityRoutes
import plugins.routes.lessons.lessonRoutes
import plugins.routes.restrictions.restrictionsRoutes
import plugins.routes.schedule.scheduleRoutes
import plugins.routes.students.studentRoutes
import plugins.routes.teachers.teacherRoutes

@Suppress("NewApi")
fun Application.configureRouting() {
    routing {
        // GET /health  →  confirma que o servidor está vivo
        get("/health") { call.respond(HttpStatusCode.OK, mapOf("status" to "ok")) }

        teacherRoutes()
        studentRoutes()
        availabilityRoutes()
        restrictionsRoutes()
        scheduleRoutes()
        lessonRoutes()
        authRoutes()
    }
}
