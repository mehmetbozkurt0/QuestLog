package com.mehmetbozkurt.questlog.core.common

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

fun startOfTodayMillis(zone: ZoneId = ZoneId.systemDefault()): Long =
    LocalDate.now(zone).atStartOfDay(zone).toInstant().toEpochMilli()

fun startOfDayMillis(instant: Instant, zone: ZoneId = ZoneId.systemDefault()): Long =
    instant.atZone(zone).toLocalDate().atStartOfDay(zone).toInstant().toEpochMilli()

fun daysAgoMillis(days: Long, zone: ZoneId = ZoneId.systemDefault()): Long =
    LocalDate.now(zone).minusDays(days).atStartOfDay(zone).toInstant().toEpochMilli()