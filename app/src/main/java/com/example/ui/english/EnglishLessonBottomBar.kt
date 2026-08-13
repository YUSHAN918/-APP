package com.example.ui.english

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

enum class FeedbackBannerState {
    NONE,
    CORRECT,
    INCORRECT,
    WARNING
}

@Composable
fun EnglishLessonBottomBar(
    modifier: Modifier = Modifier,
    enableImePadding: Boolean = false,
    hasPrevious: Boolean = true,
    previousEnabled: Boolean = true,
    nextEnabled: Boolean = true,
    nextText: String = "继续学习",
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    feedbackState: FeedbackBannerState = FeedbackBannerState.NONE,
    feedbackText: String = ""
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .then(if (enableImePadding) Modifier.imePadding() else Modifier)
            .navigationBarsPadding(),
        color = Color(0xFF0F172A), // Slate 900
        tonalElevation = 8.dp,
        shadowElevation = 8.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Independent Feedback Banner (if any)
            if (feedbackState != FeedbackBannerState.NONE && feedbackText.isNotBlank()) {
                val bannerBg = when (feedbackState) {
                    FeedbackBannerState.CORRECT -> Color(0xFF10B981).copy(alpha = 0.18f)
                    FeedbackBannerState.INCORRECT -> Color(0xFFEF4444).copy(alpha = 0.18f)
                    FeedbackBannerState.WARNING -> Color(0xFFF59E0B).copy(alpha = 0.18f)
                    else -> Color.Transparent
                }
                val bannerBorder = when (feedbackState) {
                    FeedbackBannerState.CORRECT -> Color(0xFF10B981)
                    FeedbackBannerState.INCORRECT -> Color(0xFFEF4444)
                    FeedbackBannerState.WARNING -> Color(0xFFF59E0B)
                    else -> Color.Transparent
                }
                val bannerTextColor = when (feedbackState) {
                    FeedbackBannerState.CORRECT -> Color(0xFF34D399)
                    FeedbackBannerState.INCORRECT -> Color(0xFFF87171)
                    FeedbackBannerState.WARNING -> Color(0xFFFBBF24)
                    else -> Color.White
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(bannerBg, RoundedCornerShape(12.dp))
                        .border(1.dp, bannerBorder, RoundedCornerShape(12.dp))
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = if (feedbackState == FeedbackBannerState.CORRECT) Icons.Default.CheckCircle else Icons.Default.Refresh,
                        contentDescription = null,
                        tint = bannerTextColor,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = feedbackText,
                        color = bannerTextColor,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Navigation Row (Minimum 64dp container height guaranteed)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 64.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (hasPrevious) {
                    OutlinedButton(
                        onClick = onPrevious,
                        enabled = previousEnabled,
                        modifier = Modifier
                            .weight(0.32f)
                            .heightIn(min = 56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = Color(0xFF1E293B),
                            contentColor = Color.White,
                            disabledContainerColor = Color(0xFF0F172A),
                            disabledContentColor = Color(0xFF475569)
                        ),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (previousEnabled) Color(0xFF334155) else Color(0xFF1E293B)
                        ),
                        contentPadding = PaddingValues(horizontal = 8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "上一步",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "上一步",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                }

                Button(
                    onClick = onNext,
                    enabled = nextEnabled,
                    modifier = Modifier
                        .weight(if (hasPrevious) 0.68f else 1.0f)
                        .heightIn(min = 56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF00E5FF),
                        contentColor = Color(0xFF0284C7),
                        disabledContainerColor = Color(0xFF334155),
                        disabledContentColor = Color(0xFF64748B)
                    ),
                    contentPadding = PaddingValues(horizontal = 12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = nextText,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (nextEnabled) Color(0xFF0F172A) else Color(0xFF94A3B8),
                            maxLines = 1,
                            softWrap = false
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            tint = if (nextEnabled) Color(0xFF0F172A) else Color(0xFF94A3B8),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun EnglishChallengeBottomBar(
    modifier: Modifier = Modifier,
    enableImePadding: Boolean = false,
    hasPrevious: Boolean = true,
    previousText: String = "上一步",
    previousEnabled: Boolean = true,
    actionText: String = "提交答案",
    actionEnabled: Boolean = true,
    actionColor: Color = Color(0xFF00E5FF),
    onPrevious: () -> Unit,
    onAction: () -> Unit,
    feedbackState: FeedbackBannerState = FeedbackBannerState.NONE,
    feedbackText: String = ""
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .then(if (enableImePadding) Modifier.imePadding() else Modifier)
            .navigationBarsPadding(),
        color = Color(0xFF0F172A),
        tonalElevation = 8.dp,
        shadowElevation = 8.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (feedbackState != FeedbackBannerState.NONE && feedbackText.isNotBlank()) {
                val bannerBg = when (feedbackState) {
                    FeedbackBannerState.CORRECT -> Color(0xFF10B981).copy(alpha = 0.18f)
                    FeedbackBannerState.INCORRECT -> Color(0xFFEF4444).copy(alpha = 0.18f)
                    FeedbackBannerState.WARNING -> Color(0xFFF59E0B).copy(alpha = 0.18f)
                    else -> Color.Transparent
                }
                val bannerBorder = when (feedbackState) {
                    FeedbackBannerState.CORRECT -> Color(0xFF10B981)
                    FeedbackBannerState.INCORRECT -> Color(0xFFEF4444)
                    FeedbackBannerState.WARNING -> Color(0xFFF59E0B)
                    else -> Color.Transparent
                }
                val bannerTextColor = when (feedbackState) {
                    FeedbackBannerState.CORRECT -> Color(0xFF34D399)
                    FeedbackBannerState.INCORRECT -> Color(0xFFF87171)
                    FeedbackBannerState.WARNING -> Color(0xFFFBBF24)
                    else -> Color.White
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(bannerBg, RoundedCornerShape(12.dp))
                        .border(1.dp, bannerBorder, RoundedCornerShape(12.dp))
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = if (feedbackState == FeedbackBannerState.CORRECT) Icons.Default.CheckCircle else Icons.Default.Refresh,
                        contentDescription = null,
                        tint = bannerTextColor,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = feedbackText,
                        color = bannerTextColor,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 64.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (hasPrevious) {
                    OutlinedButton(
                        onClick = onPrevious,
                        enabled = previousEnabled,
                        modifier = Modifier
                            .weight(0.32f)
                            .heightIn(min = 56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = Color(0xFF1E293B),
                            contentColor = Color.White,
                            disabledContainerColor = Color(0xFF0F172A),
                            disabledContentColor = Color(0xFF475569)
                        ),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (previousEnabled) Color(0xFF334155) else Color(0xFF1E293B)
                        ),
                        contentPadding = PaddingValues(horizontal = 8.dp)
                    ) {
                        Text(
                            text = previousText,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                }

                Button(
                    onClick = onAction,
                    enabled = actionEnabled,
                    modifier = Modifier
                        .weight(if (hasPrevious) 0.68f else 1.0f)
                        .heightIn(min = 56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = actionColor,
                        contentColor = Color.White,
                        disabledContainerColor = Color(0xFF334155),
                        disabledContentColor = Color(0xFF64748B)
                    ),
                    contentPadding = PaddingValues(horizontal = 12.dp)
                ) {
                    Text(
                        text = actionText,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (actionEnabled) Color(0xFF0F172A) else Color(0xFF94A3B8),
                        maxLines = 1,
                        softWrap = false
                    )
                }
            }
        }
    }
}
