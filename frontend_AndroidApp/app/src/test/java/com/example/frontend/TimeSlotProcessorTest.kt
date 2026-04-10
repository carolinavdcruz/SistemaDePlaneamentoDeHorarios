package com.example.frontend

import com.example.frontend.data.model.scheduling.TimeSlotProcessor
import org.junit.Assert.*
import org.junit.Test
import java.time.LocalTime

class TimeSlotProcessorTest {

    @Test
    fun `09h-11h gera exatamente 2 slots de 1h`() {
        val slots = TimeSlotProcessor.process(1, "09:00", "11:00", 60L)
        assertEquals(2, slots.size)
    }

    @Test
    fun `primeiro slot começa às 09h e termina às 10h`() {
        val slots = TimeSlotProcessor.process(1, "09:00", "11:00", 60L)
        assertEquals(LocalTime.of(9, 0),  slots[0].startTime)
        assertEquals(LocalTime.of(10, 0), slots[0].endTime)
    }

    @Test
    fun `segundo slot começa às 10h e termina às 11h`() {
        val slots = TimeSlotProcessor.process(1, "09:00", "11:00", 60L)
        assertEquals(LocalTime.of(10, 0), slots[1].startTime)
        assertEquals(LocalTime.of(11, 0), slots[1].endTime)
    }

    @Test
    fun `todos os slots herdam o dia correto`() {
        val slots = TimeSlotProcessor.process(3, "09:00", "11:00", 60L)
        assertTrue(slots.all { it.dayOfWeek == 3 })
    }

    @Test
    fun `intervalo de 8h gera 8 slots`() {
        val slots = TimeSlotProcessor.process(2, "09:00", "17:00", 60L)
        assertEquals(8, slots.size)
    }

    @Test
    fun `intervalo de 30min nao gera nenhum slot quando duracao e 1h`() {
        val slots = TimeSlotProcessor.process(1, "09:00", "09:30", 60L)
        assertEquals(0, slots.size)
    }

    @Test
    fun `start igual a end nao gera slots`() {
        val slots = TimeSlotProcessor.process(1, "10:00", "10:00", 60L)
        assertEquals(0, slots.size)
    }

    @Test
    fun `start depois de end nao gera slots`() {
        val slots = TimeSlotProcessor.process(1, "11:00", "09:00", 60L)
        assertEquals(0, slots.size)
    }

    @Test
    fun `slots sao consecutivos sem sobreposicao`() {
        val slots = TimeSlotProcessor.process(1, "09:00", "12:00", 60L)
        for (i in 0 until slots.size - 1) {
            assertEquals(slots[i].endTime, slots[i + 1].startTime)
        }
    }

    @Test
    fun `processAll com dois dias gera slots de ambos`() {
        val input = listOf(
            Triple(1, "09:00", "11:00"),  // 2 slots
            Triple(3, "14:00", "16:00")   // 2 slots
        )
        val slots = TimeSlotProcessor.processAll(input, 60L)
        assertEquals(4, slots.size)
    }

    @Test
    fun `processAll lista vazia devolve lista vazia`() {
        val slots = TimeSlotProcessor.processAll(emptyList(), 60L)
        assertEquals(0, slots.size)
    }

    @Test
    fun `processAll preserva o dia de cada grupo`() {
        val input = listOf(
            Triple(1, "09:00", "10:00"),
            Triple(5, "09:00", "10:00")
        )
        val slots = TimeSlotProcessor.processAll(input, 60L)
        val days = slots.map { it.dayOfWeek }
        assertTrue(1 in days && 5 in days)
    }
}