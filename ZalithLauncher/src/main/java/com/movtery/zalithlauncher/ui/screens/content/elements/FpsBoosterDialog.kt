/*
 * SahaLauncher (based on Zalith Launcher 2, GPL-3.0)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.movtery.zalithlauncher.ui.screens.content.elements

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.setting.AllSettings
import com.movtery.zalithlauncher.setting.launcherMMKV

const val FPS_BOOSTER_NORMAL = 0
const val FPS_BOOSTER_BOOST = 1
const val FPS_BOOSTER_MAXIMAL = 2

private const val KEY_FPS_BOOSTER_MODE = "fps_booster_mode"
private const val KEY_FPS_BOOSTER_PREV_RATIO = "fps_booster_prev_ratio"

private const val BOOST_MAX_RATIO = 60
private const val MAXIMAL_MAX_RATIO = 35

/** 读取上一次选择的 FPS Booster 模式 */
fun currentFpsBoosterMode(): Int =
    launcherMMKV().getInt(KEY_FPS_BOOSTER_MODE, FPS_BOOSTER_NORMAL)

/**
 * 应用 FPS Booster 模式：
 * Normal 恢复原本的分辨率设置；Boost / Maximal 降低渲染分辨率并关闭 FPS 限制
 */
fun applyFpsBoosterMode(mode: Int) {
    val mmkv = launcherMMKV()
    mmkv.putInt(KEY_FPS_BOOSTER_MODE, mode)

    if (mode == FPS_BOOSTER_NORMAL) {
        if (mmkv.containsKey(KEY_FPS_BOOSTER_PREV_RATIO)) {
            val previous = mmkv.getInt(KEY_FPS_BOOSTER_PREV_RATIO, AllSettings.resolutionRatio.getValue())
            AllSettings.resolutionRatio.save(previous)
            mmkv.removeValueForKey(KEY_FPS_BOOSTER_PREV_RATIO)
        }
        return
    }

    val current = AllSettings.resolutionRatio.getValue()
    if (!mmkv.containsKey(KEY_FPS_BOOSTER_PREV_RATIO)) {
        mmkv.putInt(KEY_FPS_BOOSTER_PREV_RATIO, current)
    }
    val previous = mmkv.getInt(KEY_FPS_BOOSTER_PREV_RATIO, current)
    val limit = if (mode == FPS_BOOSTER_MAXIMAL) MAXIMAL_MAX_RATIO else BOOST_MAX_RATIO
    AllSettings.resolutionRatio.save(minOf(previous, limit))
    AllSettings.fpsLimitEnabled.save(false)
}

/**
 * 启动游戏前弹出的 FPS Booster 选择窗口
 */
@Composable
fun FpsBoosterDialog(
    onSelect: (mode: Int) -> Unit,
    onCancel: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onCancel,
        title = {
            Text(text = stringResource(R.string.fps_booster_title))
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = stringResource(R.string.fps_booster_desc),
                    style = MaterialTheme.typography.bodyMedium
                )
                FilledTonalButton(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { onSelect(FPS_BOOSTER_NORMAL) }
                ) {
                    ModeLabel(
                        title = stringResource(R.string.fps_booster_normal),
                        summary = stringResource(R.string.fps_booster_normal_desc)
                    )
                }
                FilledTonalButton(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { onSelect(FPS_BOOSTER_BOOST) }
                ) {
                    ModeLabel(
                        title = stringResource(R.string.fps_booster_boost),
                        summary = stringResource(R.string.fps_booster_boost_desc)
                    )
                }
                Button(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { onSelect(FPS_BOOSTER_MAXIMAL) }
                ) {
                    ModeLabel(
                        title = stringResource(R.string.fps_booster_maximal),
                        summary = stringResource(R.string.fps_booster_maximal_desc)
                    )
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onCancel) {
                Text(text = stringResource(R.string.fps_booster_cancel))
            }
        }
    )
}

@Composable
private fun ModeLabel(
    title: String,
    summary: String
) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = summary,
            style = MaterialTheme.typography.bodySmall
        )
    }
}


/**
 * 设置页里的 FPS Booster 模式选择器（选择后立即生效）
 */
@Composable
fun FpsBoosterModePicker(modifier: Modifier = Modifier) {
    var mode by remember { mutableIntStateOf(currentFpsBoosterMode()) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(all = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = stringResource(R.string.fps_booster_title),
            style = MaterialTheme.typography.titleSmall
        )
        Text(
            text = stringResource(R.string.fps_booster_settings_hint),
            style = MaterialTheme.typography.labelSmall
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FpsBoosterModeButton(
                modifier = Modifier.weight(1f),
                selected = mode == FPS_BOOSTER_NORMAL,
                label = stringResource(R.string.fps_booster_normal),
                onClick = { applyFpsBoosterMode(FPS_BOOSTER_NORMAL); mode = FPS_BOOSTER_NORMAL }
            )
            FpsBoosterModeButton(
                modifier = Modifier.weight(1f),
                selected = mode == FPS_BOOSTER_BOOST,
                label = stringResource(R.string.fps_booster_boost),
                onClick = { applyFpsBoosterMode(FPS_BOOSTER_BOOST); mode = FPS_BOOSTER_BOOST }
            )
            FpsBoosterModeButton(
                modifier = Modifier.weight(1f),
                selected = mode == FPS_BOOSTER_MAXIMAL,
                label = stringResource(R.string.fps_booster_maximal),
                onClick = { applyFpsBoosterMode(FPS_BOOSTER_MAXIMAL); mode = FPS_BOOSTER_MAXIMAL }
            )
        }
    }
}

@Composable
private fun FpsBoosterModeButton(
    selected: Boolean,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (selected) {
        Button(modifier = modifier, onClick = onClick) {
            Text(text = label)
        }
    } else {
        OutlinedButton(modifier = modifier, onClick = onClick) {
            Text(text = label)
        }
    }
}
