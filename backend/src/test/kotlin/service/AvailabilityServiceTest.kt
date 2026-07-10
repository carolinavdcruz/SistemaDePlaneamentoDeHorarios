package service

import java.time.LocalTime
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Testes de AvailabilityService.splitIntoSlots — função pura, sem base de dados.
 * Espelha os testes já existentes no frontend (TimeSlotProcessorTest.kt) para o
 * equivalente do lado do backend.
 */
class AvailabilityServiceTest {

    @Test
    fun `09h-11h gera exatamente 2 slots de 1h`() {
        val slots = AvailabilityService.splitIntoSlots(
            dayOfWeek = 1,
            startTime = LocalTime.of(9, 0),
            endTime = LocalTime.of(11, 0),
            slotDurationMinutes = 60L
        )
        assertEquals(2, slots.size)
    }

    @Test
    fun `primeiro slot começa às 09h e termina às 10h`() {
        val slots = AvailabilityService.splitIntoSlots(
            dayOfWeek = 1,
            startTime = LocalTime.of(9, 0),
            endTime = LocalTime.of(11, 0),
            slotDurationMinutes = 60L
        )
        assertEquals(LocalTime.of(9, 0), slots[0].startTime)
        assertEquals(LocalTime.of(10, 0), slots[0].endTime)
    }

    @Test
    fun `todos os slots herdam o dia da semana correto`() {
        val slots = AvailabilityService.splitIntoSlots(
            dayOfWeek = 3,
            startTime = LocalTime.of(9, 0),
            endTime = LocalTime.of(11, 0),
            slotDurationMinutes = 60L
        )
        assertTrue(slots.all { it.dayOfWeek == 3 })
    }

    @Test
    fun `intervalo de 8h gera 8 slots de 1h`() {
        val slots = AvailabilityService.splitIntoSlots(
            dayOfWeek = 2,
            startTime = LocalTime.of(9, 0),
            endTime = LocalTime.of(17, 0),
            slotDurationMinutes = 60L
        )
        assertEquals(8, slots.size)
    }

    @Test
    fun `caso extremo - intervalo nao alinhado descarta o resto incompleto`() {
        // 09:00-10:30 com slots de 60min: só cabe 1 slot completo (09-10);
        // os 30min finais (10:00-10:30) não chegam para um slot inteiro e são descartados.
        // Isto é importante saber: se o professor definir disponibilidade "torta"
        // (não alinhada a horas certas), perde-se esse pedaço de tempo.
        val slots = AvailabilityService.splitIntoSlots(
            dayOfWeek = 1,
            startTime = LocalTime.of(9, 0),
            endTime = LocalTime.of(10, 30),
            slotDurationMinutes = 60L
        )
        assertEquals(1, slots.size)
        assertEquals(LocalTime.of(9, 0), slots[0].startTime)
        assertEquals(LocalTime.of(10, 0), slots[0].endTime)
    }

    @Test
    fun `intervalo mais curto que a duracao do slot nao gera nenhum slot`() {
        val slots = AvailabilityService.splitIntoSlots(
            dayOfWeek = 1,
            startTime = LocalTime.of(9, 0),
            endTime = LocalTime.of(9, 30),
            slotDurationMinutes = 60L
        )
        assertTrue(slots.isEmpty())
    }

    @Test
    fun `funciona com duracao de slot diferente de 60 minutos`() {
        // sessões de 30 minutos: 09:00-10:00 deve dar 2 slots
        val slots = AvailabilityService.splitIntoSlots(
            dayOfWeek = 1,
            startTime = LocalTime.of(9, 0),
            endTime = LocalTime.of(10, 0),
            slotDurationMinutes = 30L
        )
        assertEquals(2, slots.size)
        assertEquals(LocalTime.of(9, 30), slots[0].endTime)
        assertEquals(LocalTime.of(10, 0), slots[1].endTime)
    }
}
