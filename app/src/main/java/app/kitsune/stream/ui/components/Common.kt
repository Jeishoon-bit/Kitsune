package app.kitsune.stream.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import app.kitsune.stream.R
import app.kitsune.stream.data.model.Anime
import coil.compose.AsyncImage
import java.util.Locale

fun formatCompact(n: Int): String = when {
    n >= 1_000_000 -> String.format(Locale.US, "%.1fM", n / 1_000_000.0)
    n >= 1_000 -> String.format(Locale.US, "%.1fK", n / 1_000.0)
    else -> n.toString()
}

fun countdownLabel(airingAt: Long?): String? {
    if (airingAt == null) return null
    val diff = airingAt - System.currentTimeMillis() / 1000
    if (diff <= 0) return null
    val days = diff / 86400
    val hours = (diff % 86400) / 3600
    return "${days}D ${hours.toString().padStart(2, '0')}H"
}

@Composable
fun MetaRow(anime: Anime, modifier: Modifier = Modifier, color: Color = Color.White) {
    val parts = listOfNotNull(
        anime.format?.replace("_", " "),
        anime.episodes?.toString(),
        anime.averageScore?.let { "★ $it" },
        anime.duration?.let { stringResource(R.string.duration_format, it) }
    )
    Text(
        parts.joinToString("  ·  "),
        modifier = modifier,
        style = MaterialTheme.typography.bodyMedium,
        color = color.copy(alpha = 0.85f)
    )
}

@Composable
fun SectionHeader(title: String, modifier: Modifier = Modifier) {
    Row(
        modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            title,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun PosterCard(anime: Anime, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Column(modifier.width(120.dp).clickable(onClick = onClick)) {
        Box {
            AsyncImage(
                model = anime.coverExtraLarge ?: anime.coverImage,
                contentDescription = anime.title,
                modifier = Modifier.width(120.dp).height(170.dp).clip(RoundedCornerShape(12.dp)),
                contentScale = ContentScale.Crop
            )
            anime.averageScore?.let { score ->
                Surface(
                    modifier = Modifier.align(Alignment.TopEnd).padding(6.dp),
                    shape = RoundedCornerShape(8.dp),
                    color = Color.Black.copy(alpha = 0.65f)
                ) {
                    Text(
                        "★ $score%",
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White
                    )
                }
            }
        }
        Spacer(Modifier.height(6.dp))
        Text(
            anime.title,
            style = MaterialTheme.typography.bodySmall,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun PosterRow(items: List<Anime>, onAnimeClick: (Int) -> Unit, modifier: Modifier = Modifier) {
    LazyRow(
        modifier = modifier,
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(items, key = { it.anilistId }) { anime ->
            PosterCard(anime = anime, onClick = { onAnimeClick(anime.anilistId) })
        }
    }
}

@Composable
fun GenreChips(
    genres: List<String>,
    selected: String?,
    onSelect: (String?) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(modifier, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        item {
            GenreChip(stringResource(R.string.genre_all), selected == null) { onSelect(null) }
        }
        items(genres, key = { it }) { genre ->
            GenreChip(genre, selected == genre) { onSelect(genre) }
        }
    }
}

@Composable
private fun GenreChip(label: String, selected: Boolean, onClick: () -> Unit) {
    Surface(
        shape = CircleShape,
        color = if (selected) Color.White else Color.Black.copy(alpha = 0.35f),
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Text(
            label,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            style = MaterialTheme.typography.labelLarge,
            color = if (selected) Color.Black else Color.White
        )
    }
}
