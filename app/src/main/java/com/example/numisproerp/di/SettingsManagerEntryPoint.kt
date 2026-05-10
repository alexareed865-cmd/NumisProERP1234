package com.numisproerp.di

import com.numisproerp.data.settings.SettingsManager
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/**
 * Доступ до [SettingsManager] із не-Hilt-контекстів (наприклад, з Composable
 * без ViewModel — як кнопка налаштувань сповіщень у NotesScreen).
 */
@EntryPoint
@InstallIn(SingletonComponent::class)
interface SettingsManagerEntryPoint {
    fun settings(): SettingsManager
}
