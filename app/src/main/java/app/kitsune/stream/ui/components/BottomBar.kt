package app.kitsune.stream.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp

data class KitsuneTab(val route: String, val icon: ImageVector, val labelRes: Int)

@Composable
fun KitsuneBottomBar(
    tabs: List<KitsuneTab>,
    currentRoute: String?,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.padding(horizontal = 24.dp, vertical = 12.dp),
        shape = RoundedCornerShape(32.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        shadowElevation = 8.dp
    ) {
        Row(
            Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            tabs.forEach { tab ->
                val selected = tab.route == currentRoute
                Surface(
                    shape = CircleShape,
                    color = if (selected) Color.White else Color.Transparent,
                    modifier = Modifier.clickable { onSelect(tab.route) }
                ) {
                    Row(
                        Modifier.padding(horizontal = if (selected) 18.dp else 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            tab.icon,
                            contentDescription = stringResource(tab.labelRes),
                            tint = if (selected) Color.Black else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (selected) {
                            Spacer(Modifier.width(8.dp))
                            Text(
                                stringResource(tab.labelRes),
                                style = MaterialTheme.typography.labelLarge,
                                color = Color.Black
                            )
                        }
                    }
                }
            }
        }
    }
}
