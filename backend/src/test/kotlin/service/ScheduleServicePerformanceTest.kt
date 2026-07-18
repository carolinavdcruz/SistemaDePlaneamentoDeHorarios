package service

import model.Restrictions
import model.Student
import model.TimeSlot
import java.time.LocalTime
import kotlin.system.measureTimeMillis
import kotlin.test.Test
import kotlin.test.assertTrue

/**
 * Testes de PERFORMANCE/STRESS do algoritmo de criação de horário (ScheduleService.create).
 *
 * Ao contrário do ScheduleServiceTest (que valida correção com casos pequenos e concretos),
 * este ficheiro valida COMPORTAMENTO SOB VOLUME: quanto tempo demora o algoritmo com centenas
 * de alunos e dezenas de slots, e se as restrições continuam a ser respeitadas nesse cenário.
 *
 * Não usa base de dados — o ScheduleService.create é uma função pura, o que torna estes
 * testes rápidos e determinísticos, ideais para correr em qualquer máquina/CI sem setup.
 *
 * Como correr: `./gradlew test --tests "service.ScheduleServicePerformanceTest"`
 */
class ScheduleServicePerformanceTest {

    private val restrictions = Restrictions(
        maxDailyHours = 8,
        sessionDurationMinutes = 60,
        maxParticipantsPerSession = 4,
        maxSessionsPerStudentPerDay = 2
    )

    /** Gera `count` slots de 1h consecutivas, repartidos por `daysOfWeek` dias. */
    private fun generateTeacherSlots(count: Int, daysOfWeek: List<Int> = listOf(1, 2, 3, 4, 5)): List<TimeSlot> {
        val slots = mutableListOf<TimeSlot>()
        var day = 0
        var hour = 8
        repeat(count) {
            val d = daysOfWeek[day % daysOfWeek.size]
            slots.add(TimeSlot(dayOfWeek = d, startTime = LocalTime.of(hour, 0), endTime = LocalTime.of(hour + 1, 0)))
            hour++
            if (hour >= 18) {
                hour = 8
                day++
            }
        }
        return slots
    }

    /** Gera `count` alunos, cada um disponível num subconjunto aleatório-determinístico dos slots do professor. */
    private fun generateStudentsWithAvailability(
        count: Int,
        teacherSlots: List<TimeSlot>
    ): Triple<List<Student>, Map<Int, List<TimeSlot>>, Map<Int, Int>> {
        val students = (1..count).map { id -> Student(id = id, name = "Aluno $id", email = "aluno$id@isel.pt", maxDailySessions = 2) }

        // Cada aluno fica disponível em ~40% dos slots do professor (determinístico, sem Random,
        // para o teste ser sempre repetível da mesma forma).
        val availabilities = students.associate { student ->
            val available = teacherSlots.filterIndexed { index, _ -> (index + student.id) % 5 < 2 }
            student.id to available
        }

        val weeklyHours = students.associate { it.id to 4 }

        return Triple(students, availabilities, weeklyHours)
    }

    @Test
    fun `200 alunos e 40 slots semanais - algoritmo termina rapidamente e respeita restricoes`() {
        val teacherSlots = generateTeacherSlots(count = 40)
        val (students, availabilities, weeklyHours) = generateStudentsWithAvailability(200, teacherSlots)

        var sessions: List<model.Session> = emptyList()
        val elapsedMs = measureTimeMillis {
            sessions = ScheduleService.create(
                teacherSlots = teacherSlots,
                students = students,
                studentAvailabilities = availabilities,
                studentWeeklyHours = weeklyHours,
                restrictions = restrictions
            )
        }

        println("[PERF] 200 alunos / 40 slots -> ${sessions.size} sessões geradas em ${elapsedMs}ms")

        // Limite generoso: com este volume, o algoritmo (O(slots * alunos) no essencial)
        // deve terminar em bem menos de 1 segundo numa máquina normal. Se isto falhar,
        // é sinal de uma regressão de performance grave (ex: alguém introduziu uma
        // operação O(n^2) ou uma query dentro do loop).
        assertTrue(elapsedMs < 2000, "Algoritmo demorou $elapsedMs ms para 200 alunos — esperado < 2000ms")

        // Garantias de correção que têm de continuar válidas independentemente do volume:
        sessions.forEach { session ->
            assertTrue(
                session.studentIds.size <= restrictions.maxParticipantsPerSession,
                "Sessão ${session.slot} excedeu o máximo de participantes"
            )
        }

        // Nenhum aluno pode aparecer duas vezes na mesma sessão
        sessions.forEach { session ->
            assertTrue(session.studentIds.toSet().size == session.studentIds.size, "Aluno duplicado numa sessão")
        }
    }

    @Test
    fun `1000 alunos e 60 slots - caso extremo de escala, so mede tempo e nao rebenta`() {
        val teacherSlots = generateTeacherSlots(count = 60)
        val (students, availabilities, weeklyHours) = generateStudentsWithAvailability(1000, teacherSlots)

        var sessions: List<model.Session> = emptyList()
        val elapsedMs = measureTimeMillis {
            sessions = ScheduleService.create(
                teacherSlots = teacherSlots,
                students = students,
                studentAvailabilities = availabilities,
                studentWeeklyHours = weeklyHours,
                restrictions = restrictions
            )
        }

        println("[PERF] 1000 alunos / 60 slots -> ${sessions.size} sessões geradas em ${elapsedMs}ms")

        // Este cenário é bem acima do que um centro de estudos real teria (é um "teste de
        // stress", não um cenário realista) — serve para confirmar que o algoritmo escala de
        // forma previsível (aprox. linear no nº de slots * alunos) e não tem nenhum
        // comportamento explosivo escondido.
        assertTrue(elapsedMs < 5000, "Algoritmo demorou $elapsedMs ms para 1000 alunos — esperado < 5000ms")
    }

    @Test
    fun `slots vazios ou lista de alunos vazia - performance nao degrada com inputs vazios`() {
        val elapsedMs = measureTimeMillis {
            repeat(1000) {
                ScheduleService.create(
                    teacherSlots = emptyList(),
                    students = emptyList(),
                    studentAvailabilities = emptyMap(),
                    studentWeeklyHours = emptyMap(),
                    restrictions = restrictions
                )
            }
        }
        println("[PERF] 1000x execução com inputs vazios em ${elapsedMs}ms")
        assertTrue(elapsedMs < 500, "Overhead inesperado mesmo com inputs vazios: ${elapsedMs}ms")
    }
}