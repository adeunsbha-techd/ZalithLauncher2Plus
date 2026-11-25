/*
 * SahaLauncher (based on Zalith Launcher 2, GPL-3.0)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.movtery.zalithlauncher.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

private class Star(
    val x: Float,
    val y: Float,
    val radius: Float,
    val phase: Float,
    val twinkle: Int,
    val drift: Int,
    val tint: Color
)

private fun buildStars(): List<Star> {
    val random = Random(2026)
    val tints = listOf(
        Color(0xFFFFFFFF),
        Color(0xFFBFDBFE),
        Color(0xFFE9D5FF),
        Color(0xFFFDE68A)
    )
    return List(150) {
        val layer = random.nextInt(3) //0: 远处, 2: 近处
        Star(
            x = random.nextFloat(),
            y = random.nextFloat(),
            radius = 0.5f + layer * 0.55f + random.nextFloat() * 0.5f,
            phase = random.nextFloat() * (2f * PI.toFloat()),
            twinkle = 40 + random.nextInt(50),
            drift = layer + 1,
            tint = tints[random.nextInt(tints.size)]
        )
    }
}

/**
 * 宇宙动画背景：渐变夜空、缓慢漂移的星云、闪烁并视差移动的星星、偶尔划过的流星
 */
@Composable
fun SpaceBackground(modifier: Modifier = Modifier) {
    val stars = remember { buildStars() }
    val transition = rememberInfiniteTransition(label = "space")
    val time by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 180_000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "spaceTime"
    )

    Canvas(modifier = modifier.fillMaxSize()) {
        val t = time
        val w = size.width
        val h = size.height
        val angle = t * 2f * PI.toFloat()
        val dp = density

        //夜空底色
        drawRect(
            brush = Brush.verticalGradient(
                listOf(Color(0xFF03030D), Color(0xFF0A0A2B), Color(0xFF150B36))
            )
        )

        //星云
        val n1 = Offset(w * (0.22f + 0.04f * cos(angle)), h * (0.35f + 0.05f * sin(angle)))
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color(0x667C3AED), Color.Transparent),
                center = n1,
                radius = w * 0.5f
            ),
            radius = w * 0.5f,
            center = n1
        )
        val n2 = Offset(w * (0.78f + 0.05f * sin(angle)), h * (0.62f + 0.04f * cos(angle)))
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color(0x5538BDF8), Color.Transparent),
                center = n2,
                radius = w * 0.45f
            ),
            radius = w * 0.45f,
            center = n2
        )
        val n3 = Offset(w * 0.55f, h * (1.05f + 0.02f * sin(angle)))
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color(0x55F472B6), Color.Transparent),
                center = n3,
                radius = w * 0.4f
            ),
            radius = w * 0.4f,
            center = n3
        )

        //星星
        stars.forEach { star ->
            val sx = ((star.x + star.drift * t) % 1f) * w
            val sy = star.y * h
            val pulse = 0.5f + 0.5f * sin(angle * star.twinkle + star.phase)
            val alpha = 0.3f + 0.7f * pulse
            val r = star.radius * dp
            val center = Offset(sx, sy)
            if (star.radius > 1.6f) {
                drawCircle(
                    color = star.tint.copy(alpha = alpha * 0.18f),
                    radius = r * 3.2f,
                    center = center
                )
            }
            drawCircle(
                color = star.tint.copy(alpha = alpha),
                radius = r,
                center = center
            )
        }

        //流星（每个循环 6 次）
        val shoot = (t * 6f) % 1f
        if (shoot < 0.04f) {
            val index = (t * 6f).toInt()
            val p = shoot / 0.04f
            val startX = w * (0.15f + 0.14f * (index % 5))
            val startY = h * (0.04f + 0.09f * (index % 3))
            val head = Offset(startX + p * w * 0.35f, startY + p * h * 0.25f)
            val tail = Offset(head.x - w * 0.08f, head.y - h * 0.056f)
            drawLine(
                brush = Brush.linearGradient(
                    colors = listOf(Color.Transparent, Color.White),
                    start = tail,
                    end = head
                ),
                start = tail,
                end = head,
                strokeWidth = 2.2f * dp,
                cap = StrokeCap.Round,
                alpha = 1f - p * 0.4f
            )
        }
    }
}
