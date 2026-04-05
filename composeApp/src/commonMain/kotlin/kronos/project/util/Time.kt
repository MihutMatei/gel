package kronos.project.util

import kotlinx.datetime.Instant
import kotlin.random.Random

private val fallbackInstant = Instant.parse("2026-01-01T00:00:00Z")

fun nowInstant(): Instant = fallbackInstant

fun newIdFromTime(): String = Random.nextLong().toString()
