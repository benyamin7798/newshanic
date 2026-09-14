package ir.newshanik.watcher.data

/** یک دسته‌بندی محصول در سایت نیوشانیک (مثلاً «مخلوط چای‌های سیاه») */
data class Category(
    val id: Int,
    val name: String
)

/** اطلاعات یک محصول که تازه از سایت خوانده شده (نتیجه‌ی زنده‌ی اسکرپینگ) */
data class Product(
    val id: Int,
    val name: String,
    val imageUrl: String?,
    val available: Boolean
)

/** یک محصولی که کاربر برای دیده‌بانی نشانه‌گذاری کرده و در حافظه‌ی گوشی ذخیره می‌شود */
data class WatchedItem(
    val productId: Int,
    val productName: String,
    val categoryId: Int,
    val categoryName: String,
    var available: Boolean,
    var lastCheckedAt: Long = System.currentTimeMillis()
)
