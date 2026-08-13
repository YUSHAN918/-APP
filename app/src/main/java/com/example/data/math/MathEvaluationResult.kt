package com.example.data.math

sealed interface MathEvaluationResult {
    object Correct : MathEvaluationResult
    data class Incorrect(val reason: String) : MathEvaluationResult
    data class InvalidInput(val message: String) : MathEvaluationResult
    data class UnitMissing(val valueStr: String) : MathEvaluationResult
    data class NotSimplified(val actualFraction: String) : MathEvaluationResult
}
