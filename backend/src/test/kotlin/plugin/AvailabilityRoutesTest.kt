package plugin

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.testing.ApplicationTestBuilder
import io.ktor.server.testing.testApplication
import model.AvailabilityRequest
import model.AvailabilityResponse
import model.OwnerType
import model.StudentRequest
import model.StudentResponse
import model.TeacherRequest
import model.TeacherResponse
import plugins.configureRouting
import plugins.configureSerialization
import testsuport.TestDatabase
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AvailabilityRoutesTest {

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
                    name = "Maria",
                    email = "maria@isel.pt",
                    password = "Student123",
                    teacherId = null
                )
            )
        }.body()

    @Test
    fun `post availability cria disponibilidade de professor`() = testApplication {
        val client = jsonClient()
        val teacher = createTeacher(client)

        val response = client.post("/availability") {
            contentType(ContentType.Application.Json)
            setBody(
                AvailabilityRequest(
                    ownerId = teacher.id,
                    ownerType = OwnerType.TEACHER,
                    dayOfWeek = 1,
                    startTime = "09:00",
                    endTime = "11:00"
                )
            )
        }

        assertEquals(HttpStatusCode.Created, response.status)

        val body = response.body<AvailabilityResponse>()
        assertTrue(body.id > 0)
        assertEquals(teacher.id, body.ownerId)
        assertEquals(OwnerType.TEACHER, body.ownerType)
        assertEquals(1, body.dayOfWeek)
    }

    @Test
    fun `get availability devolve disponibilidades do professor`() = testApplication {
        val client = jsonClient()
        val teacher = createTeacher(client)

        client.post("/availability") {
            contentType(ContentType.Application.Json)
            setBody(
                AvailabilityRequest(
                    ownerId = teacher.id,
                    ownerType = OwnerType.TEACHER,
                    dayOfWeek = 2,
                    startTime = "14:00",
                    endTime = "16:00"
                )
            )
        }

        val response = client.get("/availability") {
            parameter("ownerId", teacher.id)
            parameter("ownerType", "TEACHER")
        }

        assertEquals(HttpStatusCode.OK, response.status)

        val body = response.body<List<AvailabilityResponse>>()
        assertEquals(1, body.size)
        assertEquals("14:00", body.first().startTime)
        assertEquals("16:00", body.first().endTime)
    }

    @Test
    fun `get availability devolve disponibilidades do aluno`() = testApplication {
        val client = jsonClient()
        val student = createStudent(client)

        client.post("/availability") {
            contentType(ContentType.Application.Json)
            setBody(
                AvailabilityRequest(
                    ownerId = student.id,
                    ownerType = OwnerType.STUDENT,
                    dayOfWeek = 3,
                    startTime = "10:00",
                    endTime = "12:00"
                )
            )
        }

        val response = client.get("/availability") {
            parameter("ownerId", student.id)
            parameter("ownerType", "STUDENT")
        }

        assertEquals(HttpStatusCode.OK, response.status)

        val body = response.body<List<AvailabilityResponse>>()
        assertEquals(1, body.size)
        assertEquals(OwnerType.STUDENT, body.first().ownerType)
    }

    @Test
    fun `get availability com ownerType invalido devolve 400`() = testApplication {
        val client = jsonClient()

        val response = client.get("/availability") {
            parameter("ownerId", 1)
            parameter("ownerType", "ERRADO")
        }

        assertEquals(HttpStatusCode.BadRequest, response.status)
        assertTrue(response.bodyAsText().contains("ownerType inválido"))
    }

    @Test
    fun `delete availability remove disponibilidades`() = testApplication {
        val client = jsonClient()
        val teacher = createTeacher(client)

        client.post("/availability") {
            contentType(ContentType.Application.Json)
            setBody(
                AvailabilityRequest(
                    ownerId = teacher.id,
                    ownerType = OwnerType.TEACHER,
                    dayOfWeek = 1,
                    startTime = "09:00",
                    endTime = "10:00"
                )
            )
        }

        val deleteResponse = client.delete("/availability") {
            parameter("ownerId", teacher.id)
            parameter("ownerType", "TEACHER")
        }

        assertEquals(HttpStatusCode.OK, deleteResponse.status)

        val getResponse = client.get("/availability") {
            parameter("ownerId", teacher.id)
            parameter("ownerType", "TEACHER")
        }

        val body = getResponse.body<List<AvailabilityResponse>>()
        assertTrue(body.isEmpty())
    }
}