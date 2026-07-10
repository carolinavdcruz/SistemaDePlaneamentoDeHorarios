package service

import model.Restrictions
import model.Student
import model.TimeSlot
import java.time.LocalTime
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Testes do algoritmo de geração de horário (ScheduleService.create) — função pura,
 * sem base de dados. Cobre os casos extremos identificados no plano do projeto:
 * conflitos de disponibilidade, limites diários/semanais, e prioridade de alunos.
 */
class ScheduleServiceTest {

    private val defaultRestrictions = Restrictions()

    private fun student(id: Int, maxDailySessions: Int = 1) =
        Student(id = id, name = "Aluno $id", email = "aluno$id@isel.pt", maxDailySessions = maxDailySessions)

    private fun slot(dayOfWeek: Int, start: LocalTime, end: LocalTime) =
        TimeSlot(dayOfWeek = dayOfWeek, startTime = start, endTime = end)

    @Test
    fun `professor 18h-20h e aluno so disponivel 19h-20h - so a interseçao vira aula`() {
        // Caso do plano original: professor disponível 18-20 (2 slots de 1h: 18-19 e 19-20),
        // aluno só disponível 19-20. Resultado esperado: só a sessão 19-20 é criada.
        val slot1819 = slot(1, LocalTime.of(18, 0), LocalTime.of(19, 0))
        val slot1920 = slot(1, LocalTime.of(19, 0), LocalTime.of(20, 0))

        val aluno = student(1)
        val sessions = ScheduleService.create(
            teacherSlots = listOf(slot1819, slot1920),
            students = listOf(aluno),
            studentAvailabilities = mapOf(1 to listOf(slot1920)),
            studentWeeklyHours = mapOf(1 to 3),
            restrictions = defaultRestrictions
        )

        assertEquals(1, sessions.size, "Só devia ser criada 1 sessão (a interseção)")
        assertEquals(slot1920, sessions[0].slot)
        assertEquals(listOf(1), sessions[0].studentIds)
    }

    @Test
    fun `aluno sem disponibilidade no slot do professor nao entra em nenhuma sessao`() {
        val slotProfessor = slot(1, LocalTime.of(9, 0), LocalTime.of(10, 0))
        val slotAluno = slot(1, LocalTime.of(14, 0), LocalTime.of(15, 0))

        val sessions = ScheduleService.create(
            teacherSlots = listOf(slotProfessor),
            students = listOf(student(1)),
            studentAvailabilities = mapOf(1 to listOf(slotAluno)),
            studentWeeklyHours = mapOf(1 to 3),
            restrictions = defaultRestrictions
        )

        assertTrue(sessions.isEmpty(), "Sem sobreposição de horário, não devia haver sessões")
    }

    @Test
    fun `maxParticipantsPerSession limita quantos alunos entram na mesma sessao`() {
        val slotComum = slot(1, LocalTime.of(9, 0), LocalTime.of(10, 0))
        val restrictions = defaultRestrictions.copy(maxParticipantsPerSession = 2)

        val sessions = ScheduleService.create(
            teacherSlots = listOf(slotComum),
            students = listOf(student(1), student(2), student(3)),
            studentAvailabilities = mapOf(
                1 to listOf(slotComum),
                2 to listOf(slotComum),
                3 to listOf(slotComum)
            ),
            studentWeeklyHours = mapOf(1 to 3, 2 to 3, 3 to 3),
            restrictions = restrictions
        )

        assertEquals(1, sessions.size)
        assertEquals(2, sessions[0].studentIds.size, "Só 2 alunos deviam entrar (limite da sessão)")
    }

    @Test
    fun `aluno com menos disponibilidade tem prioridade quando a sessao tem lugares limitados`() {
        val slotComum = slot(1, LocalTime.of(9, 0), LocalTime.of(10, 0))
        val slotExtra = slot(2, LocalTime.of(9, 0), LocalTime.of(10, 0))
        val restrictions = defaultRestrictions.copy(maxParticipantsPerSession = 1)

        // Aluno 1 só tem este slot disponível (menos flexível).
        // Aluno 2 tem este slot + outro (mais flexível), por isso deve ceder o lugar.
        val sessions = ScheduleService.create(
            teacherSlots = listOf(slotComum),
            students = listOf(student(2), student(1)), // ordem de input propositadamente invertida
            studentAvailabilities = mapOf(
                1 to listOf(slotComum),
                2 to listOf(slotComum, slotExtra)
            ),
            studentWeeklyHours = mapOf(1 to 3, 2 to 3),
            restrictions = restrictions
        )

        assertEquals(1, sessions.size)
        assertEquals(listOf(1), sessions[0].studentIds, "O aluno com menos disponibilidade devia ter prioridade")
    }

    @Test
    fun `aluno nao pode exceder o maximo de sessoes por dia`() {
        val slotManha = slot(1, LocalTime.of(9, 0), LocalTime.of(10, 0))
        val slotTarde = slot(1, LocalTime.of(15, 0), LocalTime.of(16, 0))
        // maxSessionsPerStudentPerDay=1 (default) e maxDailyHours suficientemente alto
        // para não ser o professor a bloquear o segundo slot.
        val restrictions = defaultRestrictions.copy(maxDailyHours = 10)

        val sessions = ScheduleService.create(
            teacherSlots = listOf(slotManha, slotTarde),
            students = listOf(student(1)),
            studentAvailabilities = mapOf(1 to listOf(slotManha, slotTarde)),
            studentWeeklyHours = mapOf(1 to 10),
            restrictions = restrictions
        )

        assertEquals(1, sessions.size, "O aluno só devia entrar numa sessão nesse dia")
        assertEquals(slotManha, sessions[0].slot)
    }

    @Test
    fun `aluno nao pode exceder o limite semanal de horas`() {
        val slotDia1 = slot(1, LocalTime.of(9, 0), LocalTime.of(10, 0))
        val slotDia2 = slot(2, LocalTime.of(9, 0), LocalTime.of(10, 0))
        val restrictions = defaultRestrictions.copy(maxDailyHours = 10)

        val sessions = ScheduleService.create(
            teacherSlots = listOf(slotDia1, slotDia2),
            students = listOf(student(1)),
            studentAvailabilities = mapOf(1 to listOf(slotDia1, slotDia2)),
            studentWeeklyHours = mapOf(1 to 1), // só 1h/semana = 60min = 1 sessão
            restrictions = restrictions
        )

        assertEquals(1, sessions.size, "O aluno só tem horas semanais para 1 sessão")
        assertEquals(slotDia1, sessions[0].slot)
    }

    @Test
    fun `professor nao pode exceder o maximo de horas diarias mesmo com alunos disponiveis`() {
        val slot1 = slot(1, LocalTime.of(9, 0), LocalTime.of(10, 0))
        val slot2 = slot(1, LocalTime.of(10, 0), LocalTime.of(11, 0))
        // maxDailyHours = 1h → só cabe 1 sessão de 60min nesse dia, mesmo que o aluno
        // esteja disponível e dentro do seu próprio limite semanal/diário.
        val restrictions = defaultRestrictions.copy(maxDailyHours = 1, maxSessionsPerStudentPerDay = 5)

        val sessions = ScheduleService.create(
            teacherSlots = listOf(slot1, slot2),
            students = listOf(student(1, maxDailySessions = 5)),
            studentAvailabilities = mapOf(1 to listOf(slot1, slot2)),
            studentWeeklyHours = mapOf(1 to 10),
            restrictions = restrictions
        )

        assertEquals(1, sessions.size, "O professor só devia conseguir dar 1 sessão nesse dia")
        assertEquals(slot1, sessions[0].slot)
    }

    @Test
    fun `sem alunos disponiveis nenhuma sessao e criada`() {
        val sessions = ScheduleService.create(
            teacherSlots = listOf(slot(1, LocalTime.of(9, 0), LocalTime.of(10, 0))),
            students = emptyList(),
            studentAvailabilities = emptyMap(),
            studentWeeklyHours = emptyMap(),
            restrictions = defaultRestrictions
        )

        assertTrue(sessions.isEmpty())
    }
}
