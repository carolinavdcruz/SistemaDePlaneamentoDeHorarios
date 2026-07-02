package model

import kotlinx.serialization.Serializable

@Serializable
enum class RecurrenceType {
    NONE,
    WEEKLY
}