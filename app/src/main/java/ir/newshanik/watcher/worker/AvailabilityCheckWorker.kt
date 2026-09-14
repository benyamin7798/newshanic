package ir.newshanik.watcher.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import ir.newshanik.watcher.data.LocalStorage
import ir.newshanik.watcher.network.NewshanikScraper
import ir.newshanik.watcher.notification.Notifier
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Calendar

/**
 * این Worker به‌صورت دوره‌ای (هر ۳۰ یا ۶۰ دقیقه، بر اساس تنظیمات) اجرا می‌شود،
 * ولی فقط بین ساعت ۷ صبح تا ۱۰ شب واقعاً کاری انجام می‌دهد.
 */
class AvailabilityCheckWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    companion object {
        // بازه‌ی زمانی مجاز برای بررسی: از ساعت ۷ صبح تا ۱۰ شب (۲۲:۰۰)
        private const val START_HOUR = 7
        private const val END_HOUR = 22
    }

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        if (currentHour < START_HOUR || currentHour >= END_HOUR) {
            // خارج از بازه‌ی ساعتی مجاز — فعلاً کاری انجام نمی‌شود.
            return@withContext Result.success()
        }

        val watchlist = LocalStorage.loadWatchlist(applicationContext)
        if (watchlist.isEmpty()) return@withContext Result.success()

        // برای کم‌کردن تعداد درخواست‌ها، محصولات را بر اساس دسته‌بندی گروه‌بندی می‌کنیم
        // تا هر صفحه‌ی دسته‌بندی فقط یک‌بار در هر دور بررسی، خوانده شود.
        val byCategory = watchlist.groupBy { it.categoryId }

        var changed = false
        for ((categoryId, items) in byCategory) {
            val products = try {
                NewshanikScraper.fetchProducts(categoryId)
            } catch (e: Exception) {
                emptyList()
            }
            if (products.isEmpty()) continue

            val statusById = products.associateBy { it.id }

            for (item in items) {
                val fresh = statusById[item.productId] ?: continue
                val wasAvailable = item.available
                item.available = fresh.available
                item.lastCheckedAt = System.currentTimeMillis()
                changed = true

                if (!wasAvailable && fresh.available) {
                    Notifier.notifyBackInStock(applicationContext, item.productName)
                }
            }
        }

        if (changed) {
            LocalStorage.saveWatchlist(applicationContext, watchlist)
        }

        Result.success()
    }
}
