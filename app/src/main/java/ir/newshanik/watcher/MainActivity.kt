package ir.newshanik.watcher

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import ir.newshanik.watcher.data.Category
import ir.newshanik.watcher.data.LocalStorage
import ir.newshanik.watcher.data.Product
import ir.newshanik.watcher.data.WatchedItem
import ir.newshanik.watcher.network.NewshanikScraper
import ir.newshanik.watcher.ui.screens.AddProductScreen
import ir.newshanik.watcher.ui.screens.SettingsScreen
import ir.newshanik.watcher.ui.screens.WatchlistScreen
import ir.newshanik.watcher.ui.theme.AppThemeMode
import ir.newshanik.watcher.ui.theme.NewshanikWatcherTheme
import ir.newshanik.watcher.worker.WorkScheduler

private enum class Screen { WATCHLIST, ADD, SETTINGS }

class MainActivity : ComponentActivity() {

    private val requestNotificationPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { /* نتیجه لازم نیست جایی استفاده شود؛ فقط اجازه‌ی نمایش نوتیفیکیشن لازم است */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = ContextCompat.checkSelfPermission(
                this, Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            if (!granted) {
                requestNotificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        setContent {
            var themeModeStr by remember { mutableStateOf(LocalStorage.getThemeMode(this)) }
            val themeMode = when (themeModeStr) {
                "light" -> AppThemeMode.LIGHT
                "dark" -> AppThemeMode.DARK
                else -> AppThemeMode.SYSTEM
            }

            var screen by remember { mutableStateOf(Screen.WATCHLIST) }
            var watchlist by remember { mutableStateOf(LocalStorage.loadWatchlist(this)) }
            var cachedCategories by remember { mutableStateOf(LocalStorage.loadCategoriesCache(this)) }
            var intervalMinutes by remember { mutableStateOf(LocalStorage.getIntervalMinutes(this)) }

            NewshanikWatcherTheme(themeMode = themeMode) {
                when (screen) {
                    Screen.WATCHLIST -> WatchlistScreen(
                        items = watchlist,
                        onAddClick = { screen = Screen.ADD },
                        onSettingsClick = { screen = Screen.SETTINGS },
                        onDelete = { item ->
                            watchlist = watchlist.filter { it.productId != item.productId }
                            LocalStorage.saveWatchlist(this, watchlist)
                        }
                    )

                    Screen.ADD -> AddProductScreen(
                        cachedCategories = cachedCategories,
                        onFetchCategories = {
                            val list = NewshanikScraper.fetchCategories()
                            if (list.isNotEmpty()) {
                                cachedCategories = list
                                LocalStorage.saveCategoriesCache(this@MainActivity, list)
                            }
                            list.ifEmpty { cachedCategories }
                        },
                        onFetchProducts = { categoryId -> NewshanikScraper.fetchProducts(categoryId) },
                        onSave = { category: Category, product: Product ->
                            val newItem = WatchedItem(
                                productId = product.id,
                                productName = product.name,
                                categoryId = category.id,
                                categoryName = category.name,
                                available = product.available
                            )
                            val updated = watchlist.filter { it.productId != product.id } + newItem
                            watchlist = updated
                            LocalStorage.saveWatchlist(this, updated)
                        },
                        onBack = { screen = Screen.WATCHLIST }
                    )

                    Screen.SETTINGS -> SettingsScreen(
                        intervalMinutes = intervalMinutes,
                        themeMode = themeMode,
                        onIntervalChange = { minutes ->
                            intervalMinutes = minutes
                            LocalStorage.setIntervalMinutes(this, minutes)
                            WorkScheduler.reschedule(this)
                        },
                        onThemeChange = { mode ->
                            themeModeStr = when (mode) {
                                AppThemeMode.LIGHT -> "light"
                                AppThemeMode.DARK -> "dark"
                                AppThemeMode.SYSTEM -> "system"
                            }
                            LocalStorage.setThemeMode(this, themeModeStr)
                        },
                        onBack = { screen = Screen.WATCHLIST }
                    )
                }
            }
        }
    }
}
