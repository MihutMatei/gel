package kronos.project.models

import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.javatime.timestamp
import java.time.Instant

object PinImagesTable : UUIDTable("pin_images") {
    val pinId = reference("pin_id", PinsTable, onDelete = ReferenceOption.CASCADE)
    val imageUrl = text("image_url")
    val uploadedAt = timestamp("uploaded_at").clientDefault { Instant.now() }
}

