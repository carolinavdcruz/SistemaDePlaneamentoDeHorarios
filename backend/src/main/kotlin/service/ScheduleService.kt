package service

import model.Session
import model.Student
import model.TimeSlot

object ScheduleService {

    /**
     * Algoritmo greedy — Use Case 5 e 6.
     * Para cada slot disponível do professor, atribui alunos
     * que também estejam disponíveis nesse slot,
     * respeitando: maxDailySessions por aluno, maxCapacity por sessão.
     */
    fun generate(
        teacherId: Int,
        teacherSlots: List<TimeSlot>,
        students: List<Student>,
        studentAvailabilities: Map<Int, List<TimeSlot>>,  // studentId → slots
        maxCapacityPerSession: Int = 5
    ): List<Session> {
        val sessions       = mutableListOf<Session>()
        val dailyCount     = mutableMapOf<Int, MutableMap<Int, Int>>() // studentId → day → count
        val assignedSlots  = mutableSetOf<Pair<Int, Int>>() // (studentId, slotId)

        // Ordena alunos por menos disponibilidade (greedy — mais restrito primeiro)
        val sortedStudents = students.sortedBy { student ->
            studentAvailabilities[student.id]?.size ?: 0
        }

        for (slot in teacherSlots) {
            val enrolled = mutableListOf<Int>()

            for (student in sortedStudents) {
                if (enrolled.size >= maxCapacityPerSession) break

                val studentSlots = studentAvailabilities[student.id] ?: continue

                // Aluno disponível neste slot?
                val available = studentSlots.any {
                    it.dayOfWeek == slot.dayOfWeek &&
                            it.startTime == slot.startTime
                }
                if (!available) continue

                // Já foi atribuído a este slot?
                if (assignedSlots.contains(student.id to slot.id)) continue

                // Já atingiu o máximo de sessões no dia?
                val countToday = dailyCount
                    .getOrPut(student.id) { mutableMapOf() }
                    .getOrDefault(slot.dayOfWeek, 0)
                if (countToday >= student.maxDailySessions) continue

                // Atribui
                enrolled.add(student.id)
                assignedSlots.add(student.id to slot.id)
                dailyCount[student.id]!![slot.dayOfWeek] = countToday + 1
            }

            if (enrolled.isNotEmpty()) {
                sessions.add(Session(slot = slot, studentIds = enrolled))
            }
        }
        return sessions
    }
}