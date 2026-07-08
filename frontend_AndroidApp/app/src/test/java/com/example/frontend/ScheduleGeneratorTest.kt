package com.example.frontend

import com.example.frontend.data.local.entity.StudentEntity
import com.example.frontend.data.model.Restrictions
import com.example.frontend.data.model.TimeSlot
import com.example.frontend.data.model.scheduling.ScheduleGenerator
import org.junit.Assert.*
import org.junit.Test
import java.time.LocalTime

/*class ScheduleGeneratorTest {

    private val generator = ScheduleGenerator()

    private val restrictions = Restrictions(
        teacherId                  = 1,
        maxDailyHours              = 8,
        sessionDurationMinutes     = 60,
        maxParticipantsPerSession  = 3,
        maxSessionsPerStudentPerDay = 1
    )

    private fun slot(day: Int, hour: Int) = TimeSlot(
        dayOfWeek = day,
        startTime = LocalTime.of(hour, 0),
        endTime   = LocalTime.of(hour + 1, 0)
    )

    private fun student(id: Int, maxDaily: Int = 1) = StudentEntity(
        id                = id,
        name              = "Aluno $id",
        email             = "aluno$id@test.com",
        maxDailySessions  = maxDaily
    )

    @Test
    fun `aluno disponivel e atribuido ao slot do professor`() {
        val teacherSlots = listOf(slot(1, 9))
        val students     = listOf(student(1))
        val avail        = mapOf(1 to listOf(slot(1, 9)))

        val result = generator.create(1, teacherSlots, students, avail, restrictions)

        assertEquals(1, result.size)
        assertTrue(1 in result[0].studentIds)
    }

    @Test
    fun `aluno sem disponibilidade nao e atribuido`() {
        val teacherSlots = listOf(slot(1, 9))
        val students     = listOf(student(1))
        val avail        = mapOf(1 to listOf(slot(2, 9))) // dia diferente

        val result = generator.create(1, teacherSlots, students, avail, restrictions)

        assertEquals(0, result.size)
    }

    @Test
    fun `nao excede maxParticipantsPerSession`() {
        val r = restrictions.copy(maxParticipantsPerSession = 2)
        val teacherSlots = listOf(slot(1, 9))
        val students     = listOf(student(1), student(2), student(3))
        val avail        = mapOf(
            1 to listOf(slot(1, 9)),
            2 to listOf(slot(1, 9)),
            3 to listOf(slot(1, 9))
        )

        val result = generator.create(1, teacherSlots, students, avail, r)

        assertEquals(1, result.size)
        assertEquals(2, result[0].studentIds.size)
    }

    @Test
    fun `nao excede maxDailySessions por aluno`() {
        val teacherSlots = listOf(slot(1, 9), slot(1, 10))
        val students     = listOf(student(1, maxDaily = 1))
        val avail        = mapOf(1 to listOf(slot(1, 9), slot(1, 10)))

        val result = generator.create(1, teacherSlots, students, avail, restrictions)

        val totalAssignments = result.sumOf { it.studentIds.size }
        assertEquals(1, totalAssignments) // só 1 sessão no dia, apesar de 2 slots
    }

    @Test
    fun `aluno com menos disponibilidade e atribuido primeiro`() {
        val r = restrictions.copy(maxParticipantsPerSession = 1)
        val teacherSlots = listOf(slot(1, 9))

        // aluno 1 tem 3 slots (menos restrito), aluno 2 tem 1 (mais restrito)
        val students = listOf(student(1), student(2))
        val avail = mapOf(
            1 to listOf(slot(1, 9), slot(2, 9), slot(3, 9)),
            2 to listOf(slot(1, 9))
        )

        val result = generator.create(1, teacherSlots, students, avail, r)

        assertEquals(1, result[0].studentIds.size)
        assertEquals(2, result[0].studentIds[0]) // aluno 2 (mais restrito) vem primeiro
    }

    @Test
    fun `sem alunos devolve lista vazia`() {
        val result = generator.create(1, listOf(slot(1, 9)), emptyList(), emptyMap(), restrictions)
        assertEquals(0, result.size)
    }

    @Test
    fun `sem slots do professor devolve lista vazia`() {
        val students = listOf(student(1))
        val avail    = mapOf(1 to listOf(slot(1, 9)))
        val result   = generator.create(1, emptyList(), students, avail, restrictions)
        assertEquals(0, result.size)
    }
}

 */