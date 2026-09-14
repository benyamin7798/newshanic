package ir.newshanik.watcher.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import ir.newshanik.watcher.data.WatchedItem
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WatchlistScreen(
    items: List<WatchedItem>,
    onAddClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onDelete: (WatchedItem) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("دیده‌بان نیوشانیک") },
                actions = {
                    IconButton(onClick = onSettingsClick) {
                        Icon(Icons.Filled.Settings, contentDescription = "تنظیمات")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddClick) {
                Icon(Icons.Filled.Add, contentDescription = "افزودن محصول")
            }
        }
    ) { padding ->
        if (items.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "هنوز محصولی برای دیده‌بانی اضافه نکرده‌اید.\nبا دکمه‌ی + یکی اضافه کنید.",
                    textAlign = TextAlign.Center
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(items, key = { it.productId }) { item ->
                    WatchedItemCard(item, onDelete = { onDelete(item) })
                }
            }
        }
    }
}

@Composable
private fun WatchedItemCard(item: WatchedItem, onDelete: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(12.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(item.productName, fontWeight = FontWeight.Bold)
                Text(item.categoryName, style = MaterialTheme.typography.bodySmall)
                val timeStr = SimpleDateFormat("HH:mm", Locale.getDefault())
                    .format(Date(item.lastCheckedAt))
                Text("آخرین بررسی: $timeStr", style = MaterialTheme.typography.labelSmall)
            }
            StatusBadge(item.available)
            IconButton(onClick = onDelete) {
                Icon(Icons.Filled.Delete, contentDescription = "حذف")
            }
        }
    }
}

@Composable
private fun StatusBadge(available: Boolean) {
    val (text, color) = if (available) {
        "موجود" to MaterialTheme.colorScheme.primary
    } else {
        "ناموجود" to MaterialTheme.colorScheme.error
    }
    Surface(
        color = color.copy(alpha = 0.15f),
        contentColor = color,
        shape = MaterialTheme.shapes.small
    ) {
        Text(text, modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp))
    }
}
