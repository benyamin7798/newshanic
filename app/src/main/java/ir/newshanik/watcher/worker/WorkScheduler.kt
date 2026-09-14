package ir.newshanik.watcher.worker

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import ir.newshanik.watcher.data.LocalStorage
import java.util.concurrent.TimeUnit

object WorkScheduler {

    private const val WORK_NAME = "availability_check_work"

    /** بازه‌ی بررسی دوره‌ای را بر اساس تنظیمات فعلی، (دوباره) زمان‌بندی می‌کند */
    fun reschedule(context: Context) {
        // حداقل بازه‌ی مجاز در WorkManager، ۱۵ دقیقه است.
        val intervalMinutes = LocalStorage.getIntervalMinutes(context).coerceAtLeast(15)

        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val request = PeriodicWorkRequestBuilder<AvailabilityCheckWorker>(
            intervalMinutes.toLong(), TimeUnit.MINUTES
        )
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            WORK_NAME,
            ExistingPeriodicWorkPolicy.UPDATE,
            request
        )
    }
}
