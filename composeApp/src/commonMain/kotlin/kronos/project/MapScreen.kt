package kronos.project

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun MapScreen(modifier: Modifier = Modifier) {
    PlatformMapHost(modifier = modifier)
}

@Composable
expect fun PlatformMapHost(modifier: Modifier = Modifier)

