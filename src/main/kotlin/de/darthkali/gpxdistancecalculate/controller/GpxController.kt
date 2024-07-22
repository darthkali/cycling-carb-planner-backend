package de.darthkali.gpxdistancecalculate.controller

import de.darthkali.gpxdistancecalculate.model.TDE
import de.darthkali.gpxdistancecalculate.service.GpxService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import java.io.File

@CrossOrigin
@RestController
@RequestMapping("/api/gpx")
class GpxController @Autowired constructor(private val gpxService: GpxService) {

    @PostMapping("/process")
    fun processGpx(@RequestParam("file") file: MultipartFile): ResponseEntity<Map<String, List<Any>>> {
        val tempFile = File.createTempFile("upload", file.originalFilename)
        file.transferTo(tempFile)
        val tdeList = gpxService.processGpx(tempFile)

        val response = mapOf(
            "points" to tdeList,
            "hourlySections" to gpxService.hourlySections(tdeList)
        )

        tempFile.delete()

        return ResponseEntity.ok(response)
    }
}