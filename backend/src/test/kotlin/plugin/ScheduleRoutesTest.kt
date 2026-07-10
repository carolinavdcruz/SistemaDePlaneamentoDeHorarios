package plugin

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
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
import model.AvailabilityRequest
import model.OwnerType
import model.RestrictionsRequest
import model.ScheduleCreateRequest
import model.ScheduleSessionResponse
import model.StudentRequest
import model.StudentResponse
import model.StudentRestrictionsRequest
import model.TeacherRequest
import model.TeacherResponse
import plugins.configureRouting
import plugins.configureSerialization
import testsuport.TestDatabase
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ScheduleRoutesTest {

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

    private suspend fun createStudent(
        client: HttpClient,
        teacherId: Int,
        name: String,
        email: String
    ): StudentResponse =
        client.post("/students") {
            contentType(ContentType.Application.Json)
            setBody(
                StudentRequest(
                    name = name,
                    email = email,
                    password = "Student123",
                    teacherId = teacherId
                )
            )
        }.body()

    @Test
    fun `schedule create sem restricoes devolve 404`() = testApplication {
        val client = jsonClient()
        val teacher = createTeacher(client)

        val response = client.post("/schedule/create") {
            contentType(ContentType.Application.Json)
            setBody(ScheduleCreateRequest(teacherId = teacher.id))
        }

        assertEquals(HttpStatusCode.NotFound, response.status)
        assertTrue(response.bodyAsText().contains("Restrições do professor não encontradas"))
    }

    @Test
    fun `schedule create gera sessoes com disponibilidade coincidente`() = testApplication {
        val client = jsonClient()
        val teacher = createTeacher(client)
        val student = createStudent(client, teacher.id, "Aluno A", "alunoa@isel.pt")

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

        client.put("/studentRestrictions/${student.id}") {
            contentType(ContentType.Application.Json)
            setBody(
                StudentRestrictionsRequest(
                    studentId = student.id,
                    weeklyHours = 2
                )
            )
        }

        client.post("/availability") {
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

        client.post("/availability") {
            contentType(ContentType.Application.Json)
            setBody(
                AvailabilityRequest(
                    ownerId = student.id,
                    ownerType = OwnerType.STUDENT,
                    dayOfWeek = 1,
                    startTime = "09:00",
                    endTime = "10:00"
                )
            )
        }

        val response = client.post("/schedule/create") {
            contentType(ContentType.Application.Json)
            setBody(ScheduleCreateRequest(teacherId = teacher.id))
        }

        assertEquals(HttpStatusCode.OK, response.status)

        val body = response.body<List<ScheduleSessionResponse>>()
        assertEquals(1, body.size)
        assertEquals(1, body.first().dayOfWeek)
        assertEquals("09:00", body.first().startTime)
        assertEquals("10:00", body.first().endTime)
        assertEquals(listOf(student.id), body.first().studentIds)
    }

    @Test
    fun `schedule create respeita limite semanal do aluno`() = testApplication {
        val client = jsonClient()
        val teacher = createTeacher(client)
        val student = createStudent(client, teacher.id, "Aluno A", "alunoa@isel.pt")

        client.put("/restrictions/${teacher.id}") {
            contentType(ContentType.Application.Json)
            setBody(
                RestrictionsRequest(
                    teacherId = teacher.id,
                    maxDailyHours = 8,
                    sessionDurationMinutes = 60,
                    maxParticipantsPerSession = 3,
                    maxSessionsPerStudentPerDay = 2
                )
            )
        }

        client.put("/studentRestrictions/${student.id}") {
            contentType(ContentType.Application.Json)
            setBody(
                StudentRestrictionsRequest(
                    studentId = student.id,
                    weeklyHours = 1
                )
            )
        }

        client.post("/availability") {
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

        client.post("/availability") {
            contentType(ContentType.Application.Json)
            setBody(
                AvailabilityRequest(
                    ownerId = student.id,
                    ownerType = OwnerType.STUDENT,
                    dayOfWeek = 1,
                    startTime = "09:00",
                    endTime = "11:00"
                )
            )
        }

        val response = client.post("/schedule/create") {
            contentType(ContentType.Application.Json)
            setBody(ScheduleCreateRequest(teacherId = teacher.id))
        }

        assertEquals(HttpStatusCode.OK, response.status)

        val body = response.body<List<ScheduleSessionResponse>>()
        assertEquals(1, body.size)
    }
}