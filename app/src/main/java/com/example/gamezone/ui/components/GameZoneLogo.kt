package com.example.gamezone.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gamezone.ui.theme.GameZoneTheme
import com.example.gamezone.ui.theme.PrimaryNeon
import com.example.gamezone.ui.theme.PrimaryVariant

@Composable
fun GameZoneLogo(
    modifier: Modifier = Modifier,
    isCompact: Boolean = false
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        // Icono de control con degradado
        Box(
            modifier = Modifier
                .size(if (isCompact) 32.dp else 48.dp)
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(PrimaryNeon, PrimaryVariant)
                    ),
                    shape = RoundedCornerShape(8.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Star,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(if (isCompact) 20.dp else 30.dp)
            )
        }

        if (!isCompact) {
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "Game",
                style = MaterialTheme.typography.displayLarge.copy(
                    fontSize = 28.sp,
                    fontWeight = FontWeight.ExtraBold
                ),
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "Zone",
                style = MaterialTheme.typography.displayLarge.copy(
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Light
                ),
                color = PrimaryNeon
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0F0F0F)
@Composable
fun GameZoneLogoPreview() {
    GameZoneTheme {
        Column(modifier = Modifier.padding(20.dp)) {
            GameZoneLogo()
            Spacer(modifier = Modifier.height(20.dp))
            GameZoneLogo(isCompact = true)
        }
    }
}
