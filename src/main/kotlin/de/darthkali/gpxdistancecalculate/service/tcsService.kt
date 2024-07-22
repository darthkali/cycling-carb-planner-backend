package de.darthkali.gpxdistancecalculate.service

import java.io.File
import javax.xml.parsers.DocumentBuilderFactory
import kotlin.math.round
import org.w3c.dom.Element
import org.w3c.dom.Node

data class TrackPoint(val timestamp: String, val altitude: Double, val speed: Double, val gradient: Double = 0.0)

fun main() {
    val filePath = "/Users/dannysteinbrecher/Downloads/_Der_Wahoo_hat_mir_meine_Rennsteigtour_versaut_Absturz_bei_Kilometer_160_und_dann_nur_noch_komisches_Routing_Entt_uschung_pur_.tcx"
    val trackPoints = parseTcxFile(filePath)
    val trackPointsWithGradient = calculateGradients(trackPoints)
    val sortedTrackPoints = trackPointsWithGradient.sortedBy { it.gradient }

    // Ausgabe in CSV
    val csvFile = File("/Users/dannysteinbrecher/Downloads/BlaBlub.csv")
    csvFile.printWriter().use { out ->
        out.println("timestamp,altitude,speed,gradient")
        sortedTrackPoints.forEach { out.println("${it.timestamp},${it.altitude},${it.speed},${it.gradient}") }
    }

    println("Die sortierten Steigungsdaten wurden erfolgreich gespeichert.")
}

fun parseTcxFile(filePath: String): List<TrackPoint> {
    val trackPoints = mutableListOf<TrackPoint>()
    val xmlDoc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(File(filePath))
    xmlDoc.documentElement.normalize()

    val nodeList = xmlDoc.getElementsByTagName("Trackpoint")

    for (i in 0 until nodeList.length) {
        val node = nodeList.item(i)
        if (node.nodeType == Node.ELEMENT_NODE) {
            val element = node as Element
            val time = element.getElementsByTagName("Time").item(0).textContent
            val altitude = element.getElementsByTagName("AltitudeMeters").item(0).textContent.toDouble()
            val speed = element.getElementsByTagName("Speed").item(0)?.textContent?.toDouble() ?: 0.0

            trackPoints.add(TrackPoint(time, altitude, speed))
        }
    }
    return trackPoints
}

fun calculateGradients(trackPoints: List<TrackPoint>): List<TrackPoint> {
    if (trackPoints.size < 2) return trackPoints

    val result = mutableListOf<TrackPoint>()
    result.add(trackPoints[0]) // First point has no gradient

    for (i in 1 until trackPoints.size) {
        val previous = trackPoints[i - 1]
        val current = trackPoints[i]

        val timeDiff = (current.timestamp.toDate().time - previous.timestamp.toDate().time) / 1000.0
        val altitudeDiff = current.altitude - previous.altitude
        val speed = if (current.speed != 0.0) current.speed else 1.0 // Avoid division by zero

        val gradient = (altitudeDiff / (timeDiff * speed)) * 100
        result.add(current.copy(gradient = round(gradient * 100) / 100))
    }
    return result
}

fun String.toDate(): java.util.Date {
    val sdf = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'")
    return sdf.parse(this)
}
