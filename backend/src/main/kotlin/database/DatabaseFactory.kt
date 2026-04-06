package database

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.transaction

object DatabaseFactory {
    fun init() {
        val config = HikariConfig().apply {
            jdbcUrl         = "jdbc:postgresql://localhost:5432/sph"
            driverClassName = "org.postgresql.Driver"
            username        = "postgres"
            password        = "password"
            maximumPoolSize = 10
        }
        Database.connect(HikariDataSource(config))

        transaction {/*
            SchemaUtils.create(
                TeacherTable, StudentTable,
                AvailabilityTable, TimeSlotTable,
                SchedulesTable, SessionsTable, SessionEnrollmentsTable
            )*/
        }
    }
}