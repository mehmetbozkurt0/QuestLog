package com.mehmetbozkurt.questlog.core.common

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration
import com.mehmetbozkurt.questlog.domain.model.CatalogTask
import com.mehmetbozkurt.questlog.domain.model.Pathway
import com.mehmetbozkurt.questlog.domain.model.PathwayQuest

@Composable
fun isEnglishLocale(): Boolean =
    LocalConfiguration.current.locales[0].language != "tr"

@Composable
private fun pick(turkish: String, english: String?): String =
    if (isEnglishLocale()) english?.takeIf { it.isNotBlank() } ?: turkish else turkish

@Composable
fun CatalogTask.localizedTitle(): String = pick(title, titleEn)

@Composable
fun CatalogTask.localizedDescription(): String = pick(description, descriptionEn)

@Composable
fun Pathway.localizedTitle(): String = pick(title, titleEn)

@Composable
fun Pathway.localizedDescription(): String = pick(description, descriptionEn)

@Composable
fun PathwayQuest.localizedTitle(): String = pick(title, titleEn)

@Composable
fun PathwayQuest.localizedDescription(): String = pick(description, descriptionEn)
