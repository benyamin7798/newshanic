package ir.newshanik.watcher.data

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

/**
 * تمام داده‌های اپ (بدون نیاز به دیتابیس یا سرور جانبی) به‌صورت فایل JSON ساده
 * در حافظه‌ی داخلی اپ ذخیره می‌شود. این ساده‌ترین و سبک‌ترین راه برای این پروژه است.
 */
object LocalStorage {

    private const val WATCHLIST_FILE = "watchlist.json"
    private const val CATEGORIES_FILE = "categories_cache.json"

    private const val PREFS_NAME = "settings"
    private const val KEY_INTERVAL_MINUTES = "interval_minutes"
    private const val KEY_THEME_MODE = "theme_mode" // "light" | "dark" | "system"

    // ---------- لیست دیده‌بانی ----------

    fun loadWatchlist(context: Context): MutableList<WatchedItem> {
        val file = File(context.filesDir, WATCHLIST_FILE)
        if (!file.exists()) return mutableListOf()
        return try {
            val arr = JSONArray(file.readText())
            val list = mutableListOf<WatchedItem>()
            for (i in 0 until arr.length()) {
                val o = arr.getJSONObject(i)
                list.add(
                    WatchedItem(
                        productId = o.getInt("productId"),
                        productName = o.getString("productName"),
                        categoryId = o.getInt("categoryId"),
                        categoryName = o.getString("categoryName"),
                        available = o.getBoolean("available"),
                        lastCheckedAt = o.optLong("lastCheckedAt", System.currentTimeMillis())
                    )
                )
            }
            list
        } catch (e: Exception) {
            mutableListOf()
        }
    }

    fun saveWatchlist(context: Context, items: List<WatchedItem>) {
        val arr = JSONArray()
        items.forEach { item ->
            val o = JSONObject()
            o.put("productId", item.productId)
            o.put("productName", item.productName)
            o.put("categoryId", item.categoryId)
            o.put("categoryName", item.categoryName)
            o.put("available", item.available)
            o.put("lastCheckedAt", item.lastCheckedAt)
            arr.put(o)
        }
        File(context.filesDir, WATCHLIST_FILE).writeText(arr.toString())
    }

    // ---------- کش دسته‌بندی‌ها ----------

    fun loadCategoriesCache(context: Context): List<Category> {
        val file = File(context.filesDir, CATEGORIES_FILE)
        if (!file.exists()) return emptyList()
        return try {
            val arr = JSONArray(file.readText())
            val list = mutableListOf<Category>()
            for (i in 0 until arr.length()) {
                val o = arr.getJSONObject(i)
                list.add(Category(o.getInt("id"), o.getString("name")))
            }
            list
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun saveCategoriesCache(context: Context, categories: List<Category>) {
        val arr = JSONArray()
        categories.forEach {
            val o = JSONObject()
            o.put("id", it.id)
            o.put("name", it.name)
            arr.put(o)
        }
        File(context.filesDir, CATEGORIES_FILE).writeText(arr.toString())
    }

    // ---------- تنظیمات ----------

    private fun prefs(context: Context) =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun getIntervalMinutes(context: Context): Int =
        prefs(context).getInt(KEY_INTERVAL_MINUTES, 60)

    fun setIntervalMinutes(context: Context, minutes: Int) {
        prefs(context).edit().putInt(KEY_INTERVAL_MINUTES, minutes).apply()
    }

    fun getThemeMode(context: Context): String =
        prefs(context).getString(KEY_THEME_MODE, "system") ?: "system"

    fun setThemeMode(context: Context, mode: String) {
        prefs(context).edit().putString(KEY_THEME_MODE, mode).apply()
    }
}
