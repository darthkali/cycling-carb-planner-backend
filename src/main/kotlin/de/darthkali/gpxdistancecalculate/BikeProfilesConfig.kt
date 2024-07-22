package de.darthkali.gpxdistancecalculate

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@ConfigurationProperties(prefix = "bikeprofiles")
class BikeProfilesConfig {
    lateinit var roadbike: Map<String, Double>
    lateinit var gravel: Map<String, Double>
    lateinit var mtb: Map<String, Double>
}
