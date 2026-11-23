package com.team.taskmanagementapp.data.model

sealed class ValidationResult {
    data class Success(val validItemCount: Int) : ValidationResult()
    data class Error(val message: String, val step: Int) : ValidationResult()
}
