package ir.newshanik.watcher.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import ir.newshanik.watcher.ui.theme.AppThemeMode

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    intervalMinutes: Int,
    themeMode: AppThemeMode,
    onIntervalChange: (Int) -> Unit,
    onThemeChange: (AppThemeMode) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("تنظیمات") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "بازگشت")
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).padding(16.dp).fillMaxWidth()) {

            Text(
                "بازه بررسی خودکار (بین ساعت ۷ صبح تا ۱۰ شب)",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row {
                FilterChip(
                    selected = intervalMinutes == 30,
                    onClick = { onIntervalChange(30) },
                    label = { Text("هر ۳۰ دقیقه") }
                )
                Spacer(modifier = Modifier.width(8.dp))
                FilterChip(
                    selected = intervalMinutes == 60,
                    onClick = { onIntervalChange(60) },
                    label = { Text("هر ۶۰ دقیقه") }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text("پوسته‌ی اپلیکیشن", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            Column {
                RadioRow(
                    "پیش‌فرض سیستم",
                    themeMode == AppThemeMode.SYSTEM
                ) { onThemeChange(AppThemeMode.SYSTEM) }
                RadioRow(
                    "روشن",
                    themeMode == AppThemeMode.LIGHT
                ) { onThemeChange(AppThemeMode.LIGHT) }
                RadioRow(
                    "تاریک",
                    themeMode == AppThemeMode.DARK
                ) { onThemeChange(AppThemeMode.DARK) }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                "برای اطمینان از دریافت به‌موقع نوتیفیکیشن‌ها، توصیه می‌شود بهینه‌سازی " +
                    "باتری برای این اپ غیرفعال شود.",
                style = MaterialTheme.typography.bodySmall
            )
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = { requestIgnoreBatteryOptimizations(context) }) {
                Text("غیرفعال‌سازی بهینه‌سازی باتری")
            }
        }
    }
}

@Composable
private fun RadioRow(label: String, selected: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(selected = selected, onClick = onClick)
        Text(label)
    }
}

private fun requestIgnoreBatteryOptimizations(context: Context) {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) return
    val pm = context.getSystemService(Context.POWER_SERVICE) as PowerManager
    if (!pm.isIgnoringBatteryOptimizations(context.packageName)) {
        val intent = Intent(
            Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS,
            Uri.parse("package:${context.packageName}")
        )
        context.startActivity(intent)
    }
}
