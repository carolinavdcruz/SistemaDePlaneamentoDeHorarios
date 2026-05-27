package model

import kotlinx.serialization.Serializable

@Serializable
enum class OwnerType {
    TEACHER,
    STUDENT
}