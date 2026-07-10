package testsuport

import database.tables.AvailabilityTable
import database.tables.LessonStudentTable
import database.tables.LessonTable
import database.tables.RestrictionsTable
import database.tables.StudentRestrictionsTable
import database.tables.StudentTable
import database.tables.TeacherTable
import database.tables.TimeSlotTable
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.deleteAll
import org.jetbrains.exposed.sql.transactions.transaction

/**
 * Base de dados H2 em memória, PARTILHADA por todos os testes que tocam a BD
 * (LessonServiceTest, testes de rotas HTTP, etc).
 *
 * Importante: só há UMA chamada a Database.connect() em todo o processo de
 * testes (graças ao `by lazy`, só corre na primeira vez que algum teste usa
 * `TestDatabase.db`). Isto garante que qualquer `transaction { ... }` dentro
 * do código de produção (que não indica explicitamente a BD) usa sempre esta
 * mesma ligação — se cada ficheiro de teste ligasse à sua própria BD H2,
 * só a última ligação feita "ganhava" como BD por omissão, e os outros
 * testes falhariam de forma imprevisível consoante a ordem de execução.
 */
object TestDatabase {
    val db: Database by lazy {
        val database = Database.connect(
            url = "jdbc:h2:mem:sph_test;DB_CLOSE_DELAY=-1;",
            driver = "org.h2.Driver"
        )
        transaction(database) {
            SchemaUtils.create(
                TeacherTable,
                StudentTable,
                AvailabilityTable,
                RestrictionsTable,
                TimeSlotTable,
                StudentRestrictionsTable,
                LessonTable,
                LessonStudentTable
            )
        }
        database
    }

    /** Limpa todas as tabelas (filhos primeiro, por causa das foreign keys). Chamar antes de cada teste. */
    fun reset() {
        transaction(db) {
            LessonStudentTable.deleteAll()
            LessonTable.deleteAll()
            StudentRestrictionsTable.deleteAll()
            RestrictionsTable.deleteAll()
            AvailabilityTable.deleteAll()
            TimeSlotTable.deleteAll()
            StudentTable.deleteAll()
            TeacherTable.deleteAll()
        }
    }
}