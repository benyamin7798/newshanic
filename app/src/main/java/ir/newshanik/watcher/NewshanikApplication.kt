package ir.newshanik.watcher

import android.app.Application
import ir.newshanik.watcher.notification.Notifier
import ir.newshanik.watcher.worker.WorkScheduler

class NewshanikApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        Notifier.createChannel(this)
        WorkScheduler.reschedule(this)
    }
}
