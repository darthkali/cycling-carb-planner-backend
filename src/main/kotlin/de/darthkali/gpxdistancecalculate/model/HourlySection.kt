package de.darthkali.gpxdistancecalculate.model

data class HourlySection(
    val time: Double,
    val distance: Double,
    val sectionPositiveElevation: Double,
    val sectionNegativeElevation: Double,
    val sectionDistance: Double,
)
