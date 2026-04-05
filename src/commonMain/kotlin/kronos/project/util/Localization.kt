package kronos.project.util

import gel.composeapp.generated.resources.*
import org.jetbrains.compose.resources.StringResource

fun getCategoryResource(id: String): StringResource {
    return when (id.lowercase()) {
        "road_hazards" -> Res.string.road_hazards
        "lighting" -> Res.string.lighting
        "sanitation" -> Res.string.sanitation
        "infrastructure" -> Res.string.infrastructure
        "public_transport" -> Res.string.public_transport
        "utilities" -> Res.string.utilities
        "parking" -> Res.string.parking
        "crime_safety" -> Res.string.crime_safety
        "commerce_store_access" -> Res.string.commerce_store_access
        else -> Res.string.other
    }
}
