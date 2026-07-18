package plugin

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.testing.ApplicationTestBuilder
import io.ktor.server.testing.testApplication
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import model.AvailabilityRequest
import model.GenerateLessonsRequest
import model.LessonResponse
import model.OwnerType
import model.RecurrenceType
import model.RestrictionsRequest
import model.StudentRequest
import model.StudentResponse
import model.TeacherRequest
import model.TeacherResponse
import model.UpdateLessonRequest
import plugins.configureRouting
import plugins.configureSerialization
import testsuport.TestDatabase
import kotlin.system.measureTimeMillis
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Testes de CARGA e CONCORRÊNCIA ao nível da API HTTP.
 *
 * Diferença face aos restantes testes de rotas (ex: LessonRoutesTest): aqueles validam
 * comportamento funcional com pedidos sequenciais; estes disparam MUITOS pedidos em
 * SIMULTÂNEO (via coroutines) contra o mesmo servidor de teste, para verificar:
 *   1) Throughput/latência sob um volume de pedidos concorrentes (teste de carga).
 *   2) Se dados concorrentes a escrever a mesma coisa geram condições de corrida —
 *      nomeadamente, se é possível criar aulas sobrepostas quando dois pedidos
 *      "colidem" no tempo (teste de concorrência/race condition).
 *
 * Corre sobre a mesma base H2 em memória usada nos outros testes (TestDatabase),
 * dentro de um único `testApplication { }` — o que já testa concorrência real de
 * pedidos HTTP ao mesmo motor Netty de teste, incluindo o acesso à BD via Exposed.
 *
 * Como correr: `./gradlew test --tests "plugin.LoadAndConcurrencyRoutesTest"`
 *
 * Nota: se o projeto ainda não tiver `kotlinx-coroutines-core` explícito nas
 * dependências de teste, normalmente já vem transitivamente com o Ktor server.
 * Se o Gradle acusar "unresolved reference: kotlinx", acrescentar em build.gradle.kts:
 *   testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.0")
 */
class LoadAndConcurrencyRoutesTest {

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
            install(ContentNegotiation) { json() }
        }
    }

    private suspend fun createTeacher(client: HttpClient, email: String): TeacherResponse =
        client.post("/teachers") {
            contentType(ContentType.Application.Json)
            setBody(TeacherRequest(name = "Prof. Carga", email = email, password = "Teacher123"))
        }.body()

    private suspend fun createStudent(client: HttpClient, name: String, email: String, teacherId: Int): StudentResponse =
        client.post("/students") {
            contentType(ContentType.Application.Json)
            setBody(StudentRequest(name = name, email = email, password = "Student123", teacherId = teacherId))
        }.body()

    private suspend fun saveRestrictions(client: HttpClient, teacherId: Int) {
        client.put("/restrictions/$teacherId") {
            contentType(ContentType.Application.Json)
            setBody(
                RestrictionsRequest(
                    teacherId = teacherId,
                    maxDailyHours = 12,
                    sessionDurationMinutes = 60,
                    maxParticipantsPerSession = 5,
                    maxSessionsPerStudentPerDay = 3
                )
            )
        }
    }

    private suspend fun addAvailability(
        client: HttpClient, ownerId: Int, ownerType: OwnerType,
        dayOfWeek: Int, startTime: String, endTime: String
    ) {
        client.post("/availability") {
            contentType(ContentType.Application.Json)
            setBody(AvailabilityRequest(ownerId, ownerType, dayOfWeek, startTime, endTime))
        }
    }

    // ------------------------------------------------------------------
    // 1) TESTE DE CARGA: N registos de aluno em simultâneo para o mesmo professor
    // ------------------------------------------------------------------
    @Test
    fun `50 registos de aluno em simultaneo - todos sao aceites e nenhum se perde`() = testApplication {
        val client = jsonClient()
        val teacher = createTeacher(client, "carga.professor@isel.pt")

        val totalAlunos = 50
        val elapsedMs = measureTimeMillis {
            coroutineScope {
                val resultados = (1..totalAlunos).map { i ->
                    async {
                        client.post("/students") {
                            contentType(ContentType.Application.Json)
                            setBody(
                                StudentRequest(
                                    name = "Aluno Carga $i",
                                    email = "aluno.carga.$i@isel.pt",
                                    password = "Student123",
                                    teacherId = teacher.id
                                )
                            )
                        }
                    }
                }.awaitAll()

                resultados.forEach { response ->
                    assertEquals(HttpStatusCode.Created, response.status, "Um registo concorrente falhou inesperadamente")
                }
            }
        }

        println("[LOAD] $totalAlunos registos de aluno concorrentes em ${elapsedMs}ms (média ${elapsedMs / totalAlunos.toDouble()}ms/pedido)")

        // Confirma que TODOS os alunos ficaram efetivamente persistidos (nenhum se perdeu
        // por causa de acesso concorrente à BD H2 dentro do mesmo testApplication)
        val alunos = client.get("/students/by-teacher/${teacher.id}").body<List<StudentResponse>>()
        assertEquals(totalAlunos, alunos.size, "Perderam-se registos sob escrita concorrente")

        assertTrue(elapsedMs < 15000, "Carga de $totalAlunos registos concorrentes demorou demasiado: ${elapsedMs}ms")
    }

    // ------------------------------------------------------------------
    // 2) TESTE DE CARGA: geração de horário para um professor com muitos alunos
    // ------------------------------------------------------------------
    @Test
    fun `gerar aulas para professor com 30 alunos concorrentemente disponiveis`() = testApplication {
        val client = jsonClient()
        val teacher = createTeacher(client, "carga.horario@isel.pt")
        saveRestrictions(client, teacher.id)

        addAvailability(client, teacher.id, OwnerType.TEACHER, 1, "08:00", "18:00")

        val totalAlunos = 30
        coroutineScope {
            (1..totalAlunos).map { i ->
                async {
                    val student = createStudent(client, "Aluno $i", "aluno.h.$i@isel.pt", teacher.id)
                    addAvailability(client, student.id, OwnerType.STUDENT, 1, "08:00", "18:00")
                }
            }.awaitAll()
        }

        var response: List<LessonResponse> = emptyList()
        val elapsedMs = measureTimeMillis {
            response = client.post("/lessons/generate") {
                contentType(ContentType.Application.Json)
                setBody(
                    GenerateLessonsRequest(
                        teacherId = teacher.id,
                        startDate = "2026-01-05",
                        recurrence = RecurrenceType.NONE,
                        occurrences = 1
                    )
                )
            }.body()
        }

        println("[LOAD] Geração de horário com $totalAlunos alunos disponíveis em ${elapsedMs}ms -> ${response.size} aulas criadas")
        assertTrue(elapsedMs < 5000, "Geração de horário demorou demasiado: ${elapsedMs}ms")
        assertTrue(response.isNotEmpty(), "Deveria ter gerado pelo menos uma aula")
    }

    // ------------------------------------------------------------------
    // 3) TESTE DE CONCORRÊNCIA / RACE CONDITION: duas remarcações simultâneas
    //    para o MESMO horário — no máximo uma pode ter sucesso.
    // ------------------------------------------------------------------
    @Test
    fun `duas remarcacoes simultaneas para o mesmo horario - no maximo uma tem sucesso`() = testApplication {
        val client = jsonClient()
        val teacher = createTeacher(client, "concorrencia@isel.pt")
        saveRestrictions(client, teacher.id)

        addAvailability(client, teacher.id, OwnerType.TEACHER, 1, "09:00", "12:00")

        val alunoA = createStudent(client, "Aluno A", "aluno.a@isel.pt", teacher.id)
        val alunoB = createStudent(client, "Aluno B", "aluno.b@isel.pt", teacher.id)
        addAvailability(client, alunoA.id, OwnerType.STUDENT, 1, "09:00", "12:00")
        addAvailability(client, alunoB.id, OwnerType.STUDENT, 1, "09:00", "12:00")

        val lessons = client.post("/lessons/generate") {
            contentType(ContentType.Application.Json)
            setBody(
                GenerateLessonsRequest(
                    teacherId = teacher.id,
                    startDate = "2026-01-05",
                    recurrence = RecurrenceType.NONE,
                    occurrences = 1
                )
            )
        }.body<List<LessonResponse>>()

        // Duas aulas distintas às 09:00 e 10:00 (por ex.) — vamos tentar remarcar AMBAS
        // para o MESMO slot 11:00-12:00 ao mesmo tempo, simulando dois pedidos concorrentes
        // (ex: dois separadores do browser, ou um duplo-clique acidental do professor).
        val lessonX = lessons[0]
        val lessonY = lessons[1]

        val (responseX, responseY) = coroutineScope {
            val reqX = async {
                client.patch("/lessons/${lessonX.id}") {
                    contentType(ContentType.Application.Json)
                    setBody(UpdateLessonRequest(date = "2026-01-05", startTime = "11:00", endTime = "12:00"))
                }
            }
            val reqY = async {
                client.patch("/lessons/${lessonY.id}") {
                    contentType(ContentType.Application.Json)
                    setBody(UpdateLessonRequest(date = "2026-01-05", startTime = "11:00", endTime = "12:00"))
                }
            }
            reqX.await() to reqY.await()
        }

        val statuses = listOf(responseX.status, responseY.status)
        val sucessos = statuses.count { it == HttpStatusCode.OK }
        val conflitos = statuses.count { it == HttpStatusCode.Conflict }

        println("[CONCORRENCIA] Remarcações simultâneas para o mesmo slot -> status: $statuses")

        // Este é o teste mais importante: prova (ou desmente) se a validação de conflito
        // do backend é robusta também sob concorrência real, e não só quando os pedidos
        // chegam em sequência (que é o que o LessonRoutesTest original já cobre).
        assertTrue(
            sucessos <= 1,
            "PROBLEMA DE CONCORRÊNCIA: mais do que uma remarcação para o mesmo horário teve sucesso ($statuses) — " +
                    "isto indica uma condição de corrida na validação de conflitos do backend."
        )
        assertEquals(2, sucessos + conflitos, "Esperava-se sempre OK ou Conflict, nunca outro estado inesperado: $statuses")
    }
}