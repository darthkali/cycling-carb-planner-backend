package de.darthkali.gpxdistancecalculate.service

import de.darthkali.gpxdistancecalculate.model.HourlySection
import de.darthkali.gpxdistancecalculate.model.Point
import de.darthkali.gpxdistancecalculate.model.TDE
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import kotlin.math.floor

class GpxServiceTest {

    private val gpxService = GpxService()

    @Test
    fun testCalculateGradient() {
        val point1 = Point(50.0, 10.0, 100.0)
        val point2 = Point(50.001, 10.001, 110.0)

        // Entfernung berechnet anhand der Haversine-Formel: 0.131 km
        // Höhenunterschied: 10 m
        // Gradient: (10 / 0.131) * 100 = 76.3358779
        val expectedGradient = 75.65162044078488
        val calculatedGradient = gpxService.calculateGradient(point1, point2)

        assertEquals(expectedGradient, calculatedGradient, 0.0001, "The calculated gradient should be close to the expected gradient")
    }

    @Test
    fun testCalculateGradientNegative() {
        val point1 = Point(50.0, 10.0, 110.0)
        val point2 = Point(50.001, 10.001, 100.0)

        val expectedGradient = -75.65162044078488
        val calculatedGradient = gpxService.calculateGradient(point1, point2)

        assertEquals(expectedGradient, calculatedGradient, 0.0001, "The calculated gradient should be close to the expected negative gradient")
    }

    @Test
    fun testCalculateGradientZero() {
        val point1 = Point(50.0, 10.0, 100.0)
        val point2 = Point(50.001, 10.001, 100.0)

        // Entfernung berechnet anhand der Haversine-Formel: 0.131 km
        // Höhenunterschied: 0 m
        // Gradient: (0 / 0.131) * 100 = 0
        val expectedGradient = 0.0
        val calculatedGradient = gpxService.calculateGradient(point1, point2)

        assertEquals(expectedGradient, calculatedGradient, 0.0001, "The calculated gradient should be zero")
    }

    @Test
    fun hourlySectionsTest(){

        val tdeList = listOf(
            TDE(elevation = 400.0,time= 0.0,distance = 0.0),
            TDE(elevation = 402.0,time= 0.2,distance = 2.0),
            TDE(elevation = 404.0,time= 0.4,distance = 4.0),
            TDE(elevation = 100.0,time= 0.6,distance = 6.0),
            TDE(elevation = 1000.0,time= 0.7,distance = 7.0),
            TDE(elevation = 420.0,time= 0.8,distance = 8.0),
            TDE(elevation = 450.0, time= 1.0, distance = 10.0),

            TDE(elevation = 455.0, time= 2.0, distance = 20.0),
            TDE(elevation = 420.0, time= 3.0, distance = 30.0),
            TDE(elevation = 410.0, time= 4.0, distance = 40.0),
            TDE(elevation = 400.0, time= 5.0, distance = 50.0)
        )
        val hourlySectionsList = gpxService.hourlySections(tdeList)

        assertEquals(HourlySection(time = 1.0, distance= 10.0, sectionPositiveElevation = 934.0, sectionNegativeElevation = 884.0, sectionDistance= 10.0), hourlySectionsList[0])
        assertEquals(HourlySection(time = 2.0, distance= 20.0, sectionPositiveElevation = 5.0, sectionNegativeElevation = 0.0, sectionDistance= 10.0), hourlySectionsList[1])
        assertEquals(HourlySection(time = 3.0, distance= 30.0, sectionPositiveElevation = 0.0, sectionNegativeElevation = 35.0, sectionDistance= 10.0), hourlySectionsList[2])
        assertEquals(HourlySection(time = 4.0, distance= 40.0, sectionPositiveElevation = 0.0, sectionNegativeElevation = 10.0, sectionDistance= 10.0), hourlySectionsList[3])
        assertEquals(HourlySection(time = 5.0, distance= 50.0, sectionPositiveElevation = 0.0, sectionNegativeElevation = 10.0, sectionDistance= 10.0), hourlySectionsList[4])


    }
}