package com.example.ui

sealed interface RewardClaimState {
    data object Idle : RewardClaimState
    data object Claiming : RewardClaimState
    data class Claimed(val coins: Int, val exp: Int, val intimacy: Int) : RewardClaimState
    data class Failed(val error: String) : RewardClaimState
}
