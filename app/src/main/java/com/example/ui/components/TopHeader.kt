package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Stars
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.GoldDark
import com.example.ui.theme.GoldLight
import com.example.ui.theme.NavyBackground
import com.example.ui.theme.NavyBorder
import com.example.ui.theme.NavyCard
import com.example.ui.theme.NavySurface
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.WinGreen
import com.example.ui.theme.WinGreenBg
import com.example.ui.theme.WinGreenLight

@Composable
fun TopHeader(
    isVipActive: Boolean,
    winRate: Double,
    onVipClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = NavyBackground
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Logo & Brand Name
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = GoldAccent,
                    modifier = Modifier.size(36.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = "PS",
                            fontWeight = FontWeight.Black,
                            fontSize = 16.sp,
                            color = NavyBackground,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "PICK",
                            fontWeight = FontWeight.Black,
                            fontSize = 20.sp,
                            color = TextPrimary,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "SLIP",
                            fontWeight = FontWeight.Black,
                            fontSize = 20.sp,
                            color = GoldAccent,
                            letterSpacing = 1.sp
                        )
                    }
                    Text(
                        text = "100% Algorithmique • Zéro IA",
                        fontSize = 10.sp,
                        color = TextSecondary
                    )
                }
            }

            // Status & Quick Actions
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Win Rate Pill
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = WinGreenBg,
                    border = androidx.compose.foundation.BorderStroke(1.dp, WinGreen.copy(alpha = 0.5f)),
                    modifier = Modifier.padding(end = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = WinGreenLight,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${String.format("%.1f", winRate)}%",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = WinGreenLight,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                // VIP Badge
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = if (isVipActive) GoldAccent else NavySurface,
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (isVipActive) GoldLight else NavyBorder
                    ),
                    modifier = Modifier
                        .clickable { onVipClick() }
                        .testTag("vip_header_badge")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Stars,
                            contentDescription = null,
                            tint = if (isVipActive) NavyBackground else GoldAccent,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isVipActive) "VIP ACTIF" else "VIP (99 DH)",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            color = if (isVipActive) NavyBackground else GoldAccent,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }
    }
}
