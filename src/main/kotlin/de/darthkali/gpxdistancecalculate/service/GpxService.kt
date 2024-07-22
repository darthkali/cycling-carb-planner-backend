package de.darthkali.gpxdistancecalculate.service

import de.darthkali.gpxdistancecalculate.BikeProfilesConfig
import de.darthkali.gpxdistancecalculate.model.Point
import de.darthkali.gpxdistancecalculate.model.HourlySection
import de.darthkali.gpxdistancecalculate.model.TDE
import de.darthkali.gpxdistancecalculate.model.TimeDistance
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.w3c.dom.Element
import java.io.File
import java.lang.NumberFormatException
import javax.xml.parsers.DocumentBuilderFactory
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

@Service
class GpxService() {
    private val log = LoggerFactory.getLogger(GpxService::class.java)

    //Berechnet die Entfernung zwischen zwei geographischen Punkten
    fun haversine(firstPoint: Point, secondPoint: Point): Double {
        val lon1 = firstPoint.longitude
        val lat1 = firstPoint.latitude
        val lon2 = secondPoint.longitude
        val lat2 = secondPoint.latitude

        val R = 6371.0  // Erdradius in Kilometern
        val dlon = Math.toRadians(lon2 - lon1)
        val dlat = Math.toRadians(lat2 - lat1)
        val a = sin(dlat / 2).pow(2.0) + cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) * sin(dlon / 2).pow(2.0)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        val distance = R * c

        log.debug("Haversine distance between ($lat1, $lon1) and ($lat2, $lon2): $distance km")
        return distance
    }

    //Klassifiziert die Strecke basierend auf dem Höhenunterschied
    fun classifySection(firstPoint: Point, secondPoint: Point): Double {
        val gradient = calculateGradient(firstPoint, secondPoint)
        val speed = calculateSpeedBasedOnGradient(gradient)
        log.debug("Classified section with gradient $gradient%: $speed km/h")
        return speed
    }

    fun calculateGradient(firstPoint: Point, secondPoint: Point): Double {
        val elevationChange = calculateElevationChange(
            firstElevation = firstPoint.elevation,
            secondElevation = secondPoint.elevation
        )
        val distance = haversine(firstPoint, secondPoint)
        val gradient = if (distance == 0.0) {
            0.0
        } else {
            elevationChange / distance
        }


        if (gradient.isNaN()) {
            throw NumberFormatException("Gradient was NaN")
        }
        return gradient
//        return if (gradient > 30.0) {
//            30.0
//        } else if (gradient < -30.0) {
//            -30.0
//        } else {
//            gradient
//        }
    }

    private fun calculateElevationChange(
        firstElevation: Double,
        secondElevation: Double
    ): Double {
        return secondElevation - firstElevation
    }

    fun calculateSpeedBasedOnGradient(gradient: Double): Double {
        val speed = when {
            gradient >= 31 -> 10.0
            gradient >= 30 -> 4.5
            gradient >= 27 -> 6.0
            gradient >= 24 -> 7.5
            gradient >= 21 -> 9.0
            gradient >= 18 -> 10.5
            gradient >= 15 -> 12.0
            gradient >= 12 -> 13.5
            gradient >= 9 -> 15.0
            gradient >= 6 -> 16.5
            gradient >= 3 -> 18.0
            gradient >= 0 -> 25.0
            gradient >= -3 -> 28.0
            gradient >= -6 -> 31.0
            gradient >= -9 -> 34.0
            gradient >= -12 -> 37.0
            gradient >= -15 -> 40.0
            gradient >= -18 -> 43.0
            gradient >= -21 -> 46.0
            gradient >= -24 -> 49.0
            gradient >= -27 -> 52.0
            gradient >= -30 -> 60.0
            gradient < -30 -> 35.0
            else -> {
                log.error("gradient was: $gradient")
                25.0
            }
        }
        log.info("Calculated speed for gradient $gradient%: $speed km/h")
        return speed
    }

    fun hourlySections(tdeList: List<TDE>): List<HourlySection> {
        val hourlySections = mutableListOf<HourlySection>();
        var previousHour = 0.0;
        var firstSectionGpxPoint = tdeList.first()
        var lastGpxPoint = tdeList.first()
        var positiveElevationChange = 0.0
        var negativeElevationChange = 0.0

        tdeList.forEach() { currentGpxPoint ->

            (currentGpxPoint.elevation - lastGpxPoint.elevation).let{
                if (it >= 0 ){
                    positiveElevationChange += it
                } else{
                    negativeElevationChange += abs(it)
                }
            }

            val currentHour = floor(currentGpxPoint.time);
            if (currentHour > previousHour) {
                hourlySections.add(
                    HourlySection(
                        distance = currentGpxPoint.distance,
                        time = currentGpxPoint.time,
                        sectionPositiveElevation = positiveElevationChange,
                        sectionNegativeElevation = negativeElevationChange,
                        sectionDistance = currentGpxPoint.distance - firstSectionGpxPoint.distance
                    )
                );
                previousHour = currentHour;
                positiveElevationChange = 0.0
                negativeElevationChange = 0.0
                firstSectionGpxPoint = currentGpxPoint
            }

            lastGpxPoint = currentGpxPoint
        };
        return hourlySections
    }


    // Berechnet die Geschwindigkeit basierend auf der Steigung und dem ausgewählten Profil
//    fun calculateSpeedBasedOnGradient(gradient: Double): Double {
//        val profile = "roadbike"
//        val profileSpeeds = when (profile.lowercase()) {
//            "roadbike" -> bikeProfilesConfig.roadbike
//            "gravel" -> bikeProfilesConfig.gravel
//            "mtb" -> bikeProfilesConfig.mtb
//            else -> throw IllegalArgumentException("Unknown bike profile: $profile")
//        }
//
//        val sortedKeys = profileSpeeds.keys.map { it.toDouble() }.sortedDescending()
//
//        for (key in sortedKeys) {
//            if (gradient >= key) {
//                val speed = profileSpeeds[key.toString()] ?: 25.0
//                log.info("Calculated speed for gradient $gradient% and profile $profile: $speed km/h")
//                return speed
//            }
//        }
//
//        // Falls der Gradient niedriger als der kleinste Schlüsselwert ist
//        val minSpeed = profileSpeeds["min"] ?: 25.0
//        log.info("Calculated speed for gradient $gradient% and profile $profile: $minSpeed km/h")
//        return minSpeed
//    }

    //Berechnet die Zeit basierend auf dem Streckentyp
    fun calculateTime(distance: Double, speed: Double): Double {
        return distance / speed
    }

    //Liest die GPX-Datei ein, extrahiert die Punkte, berechnet die Distanzen, Zeiten und fügt die Stundenmarken hinzu
    fun processGpx(file: File): List<TDE> {
        val points = mutableListOf<Point>()
        val hourlyDistances = mutableListOf<TimeDistance>()

        val docBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder()
        val doc = docBuilder.parse(file)
        doc.documentElement.normalize()

        val trkpts = doc.getElementsByTagName("trkpt")
        for (i in 0 until trkpts.length) {
            val trkpt = trkpts.item(i) as Element
            val lat = trkpt.getAttribute("lat").toDouble()
            val lon = trkpt.getAttribute("lon").toDouble()
            val ele = trkpt.getElementsByTagName("ele").item(0).textContent.toDouble()
            points.add(Point(lat, lon, ele))
        }

        var totalTime = 0.0
        var totalDistance = 0.0

        val tdeList = mutableListOf<TDE>()

        tdeList.add(TDE(elevation = points[0].elevation, time = 0.0, distance = 0.0))

        for (i in 1 until points.size) {
            val firstPoint = points[i - 1]
            val secondPoint = points[i]
            val distance = haversine(firstPoint, secondPoint)
            val time = calculateTime(distance, classifySection(firstPoint, secondPoint))

            totalTime += time
            totalDistance += distance

            tdeList.add(TDE(elevation = secondPoint.elevation, time = totalTime, distance = totalDistance))
        }

        return tdeList
    }
}
