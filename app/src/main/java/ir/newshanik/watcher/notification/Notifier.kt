package ir.newshanik.watcher.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import kotlin.random.Random

object Notifier {

    const val CHANNEL_ID = "availability_alerts"

    /** ساخت کانال نوتیفیکیشن — باید یک‌بار در شروع اپ فراخوانی شود */
    fun createChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "اطلاع‌رسانی موجودی محصول",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "وقتی یکی از محصولات نشانه‌گذاری‌شده موجود شود، اطلاع می‌دهد"
                enableVibration(true)
            }
            val manager = context.getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }
    }

    /** ارسال نوتیفیکیشن ساده وقتی یک محصول از ناموجود به موجود تغییر وضعیت می‌دهد */
    fun notifyBackInStock(context: Context, productName: String) {
        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("موجود شد! \uD83C\uDF89")
            .setContentText("$productName الان موجود شده — سریع سفارش بده")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)

        val manager = NotificationManagerCompat.from(context)
        try {
            manager.notify(Random.nextInt(), builder.build())
        } catch (e: SecurityException) {
            // پرمیژن نوتیفیکیشن هنوز از سوی کاربر تأیید نشده است؛ کاری نمی‌کنیم.
        }
    }
}
