package com.example.ecodot.domain.model

data class SponsorSegment(
    val category: String,
    val start: Long, // in milliseconds
    val end: Long    // in milliseconds
)
