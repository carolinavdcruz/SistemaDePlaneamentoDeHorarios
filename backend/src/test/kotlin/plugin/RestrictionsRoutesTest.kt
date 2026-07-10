package plugin

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.testing.ApplicationTestBuilder
import io.ktor.server.testing.testApplication
import model.RestrictionsRequest
import model.RestrictionsResponse
import model.StudentRequest
import model.StudentResponse
import model.StudentRestrictionsRequest
import model.StudentRestrictionsResponse
import model.TeacherRequest
import model.TeacherResponse
import plugins.configureRouting
import plugins.configureSerialization
import testsuport.TestDatabase
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RestrictionsRoutesTest {

    @BeforeTest
    fun setup() {
        TestDatabase.reset()
    }

    private fun ApplicationTestBuilder.jsonClient(): HttpClient {
        application {
            configureSerialization()
            configureRouting()
        }
        return createClient {
            install(ContentNegotiation) {
                json()
            }
        }
    }

    private suspend fun createTeacher(client: HttpClient): TeacherResponse =
        client.post("/teachers") {
            contentType(ContentType.Application.Json)
            setBody(
                TeacherRequest(
                    name = "Prof. Ana",
                    email = "ana@isel.pt",
                    password = "Teacher123"
                )
            )
        }.body()

    private suspend fun createStudent(client: HttpClient): StudentResponse =
        client.post("/students") {
            contentType(ContentType.Application.Json)
            setBody(
                StudentRequest(
                    name = "Aluno A",
                    email = "alunoa@isel.pt",
                    password = "Student123",
                    teacherId = null
                )
            )
        }.body()

    @Test
    fun `get restrictions inexistentes devolve 404`() = testApplication {
        val client = jsonClient()

        val response = client.get("/restrictions/999")

        assertEquals(HttpStatusCode.NotFound, response.status)
        assertTrue(response.bodyAsText().contains("Restrições não encontradas"))
    }

    @Test
    fun `put restrictions cria e get restrictions devolve valores`() = testApplication {
        val client = jsonClient()
        val teacher = createTeacher(client)

        val putResponse = client.put("/restrictions/${teacher.id}") {
            contentType(ContentType.Application.Json)
            setBody(
                RestrictionsRequest(
                    teacherId = teacher.id,
                    maxDailyHours = 8,
                    sessionDurationMinutes = 60,
                    maxParticipantsPerSession = 3,
                    maxSessionsPerStudentPerDay = 1
                )
            )
        }

        assertEquals(HttpStatusCode.OK, putResponse.status)

        val getResponse = client.get("/restrictions/${teacher.id}")
        assertEquals(HttpStatusCode.OK, getResponse.status)

        val body = getResponse.body<RestrictionsResponse>()
        assertEquals(teacher.id, body.teacherId)
        assertEquals(8, body.maxDailyHours)
        assertEquals(60, body.sessionDurationMinutes)
        assertEquals(3, body.maxParticipantsPerSession)
        assertEquals(1, body.maxSessionsPerStudentPerDay)
    }

    @Test
    fun `put restrictions atualiza restricoes existentes`() = testApplication {
        val client = jsonClient()
        val teacher = createTeacher(client)

        client.put("/restrictions/${teacher.id}") {
            contentType(ContentType.Application.Json)
            setBody(
                RestrictionsRequest(
                    teacherId = teacher.id,
                    maxDailyHours = 8,
                    sessionDurationMinutes = 60,
                    maxParticipantsPerSession = 3,
                    maxSessionsPerStudentPerDay = 1
                )
            )
        }

        client.put("/restrictions/${teacher.id}") {
            contentType(ContentType.Application.Json)
            setBody(
                RestrictionsRequest(
                    teacherId = teacher.id,
                    maxDailyHours = 6,
                    sessionDurationMinutes = 45,
                    maxParticipantsPerSession = 2,
                    maxSessionsPerStudentPerDay = 2
                )
            )
        }

        val body = client.get("/restrictions/${teacher.id}").body<RestrictionsResponse>()
        assertEquals(6, body.maxDailyHours)
        assertEquals(45, body.sessionDurationMinutes)
        assertEquals(2, body.maxParticipantsPerSession)
        assertEquals(2, body.maxSessionsPerStudentPerDay)
    }

    @Test
    fun `get studentRestrictions inexistentes devolve 404`() = testApplication {
        val client = jsonClient()

        val response = client.get("/studentRestrictions/999")

        assertEquals(HttpStatusCode.NotFound, response.status)
        assertTrue(response.bodyAsText().contains("Restrições do aluno não encontradas"))
    }

    @Test
    fun `put studentRestrictions cria e get studentRestrictions devolve weeklyHours`() = testApplication {
        val client = jsonClient()
        val student = createStudent(client)

        val putResponse = client.put("/studentRestrictions/${student.id}") {
            contentType(ContentType.Application.Json)
            setBody(
                StudentRestrictionsRequest(
                    studentId = student.id,
                    weeklyHours = 4
                )
            )
        }

        assertEquals(HttpStatusCode.OK, putResponse.status)

        val getResponse = client.get("/studentRestrictions/${student.id}")
        assertEquals(HttpStatusCode.OK, getResponse.status)

        val body = getResponse.body<StudentRestrictionsResponse>()
        assertEquals(student.id, body.studentId)
        assertEquals(4, body.weeklyHours)
    }

    @Test
    fun `put studentRestrictions atualiza valor existente`() = testApplication {
        val client = jsonClient()
        val student = createStudent(client)

        client.put("/studentRestrictions/${student.id}") {
            contentType(ContentType.Application.Json)
            setBody(
                StudentRestrictionsRequest(
                    studentId = student.id,
                    weeklyHours = 3
                )
            )
        }

        client.put("/studentRestrictions/${student.id}") {
            contentType(ContentType.Application.Json)
            setBody(
                StudentRestrictionsRequest(
                    studentId = student.id,
                    weeklyHours = 5
                )
            )
        }

        val body = client.get("/studentRestrictions/${student.id}")
            .body<StudentRestrictionsResponse>()

        assertEquals(5, body.weeklyHours)
    }
}