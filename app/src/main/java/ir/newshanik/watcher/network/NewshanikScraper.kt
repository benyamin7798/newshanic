package ir.newshanik.watcher.network

import ir.newshanik.watcher.data.Category
import ir.newshanik.watcher.data.Product
import okhttp3.OkHttpClient
import okhttp3.Request
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import java.util.concurrent.TimeUnit

/**
 * این آبجکت مسئول خواندن اطلاعات از سایت newshanik.ir است.
 * توجه: این توابع شبکه‌ای (بلاک‌کننده) هستند و باید همیشه از یک ترد پس‌زمینه
 * (Dispatchers.IO) یا داخل WorkManager فراخوانی شوند، نه مستقیم در UI thread.
 */
object NewshanikScraper {

    private const val BASE_URL = "https://newshanik.ir"

    private val client = OkHttpClient.Builder()
        .connectTimeout(20, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .build()

    private const val USER_AGENT =
        "Mozilla/5.0 (Linux; Android 12; Mobile) AppleWebKit/537.36 " +
            "(KHTML, like Gecko) Chrome/120.0 Mobile Safari/537.36"

    private fun fetchHtml(url: String): String? {
        val request = Request.Builder()
            .url(url)
            .header("User-Agent", USER_AGENT)
            .header("Accept-Language", "fa-IR,fa;q=0.9")
            .build()
        return try {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return null
                response.body?.string()
            }
        } catch (e: Exception) {
            null
        }
    }

    /** خواندن لیست کامل دسته‌بندی‌های محصولات از منوی کناری صفحه‌ی اصلی سایت */
    fun fetchCategories(): List<Category> {
        val html = fetchHtml(BASE_URL) ?: return emptyList()
        val doc = Jsoup.parse(html)
        val links = doc.select(".sidebar-submenu ul li a[href~=^/productlist/]")
        val seen = LinkedHashSet<Int>()
        val result = mutableListOf<Category>()
        for (a in links) {
            val href = a.attr("href")
            val id = Regex("/productlist/(\\d+)").find(href)?.groupValues?.get(1)?.toIntOrNull()
                ?: continue
            val name = a.text().trim()
            if (id !in seen && name.isNotBlank()) {
                seen.add(id)
                result.add(Category(id, name))
            }
        }
        return result
    }

    /** خواندن لیست محصولات و وضعیت موجودی هرکدام برای یک دسته‌بندی خاص */
    fun fetchProducts(categoryId: Int): List<Product> {
        val html = fetchHtml("$BASE_URL/productlist/$categoryId") ?: return emptyList()
        val doc = Jsoup.parse(html)
        val cards = doc.select("div.box.box-product")
        val result = mutableListOf<Product>()

        for (card: Element in cards) {
            val nameEl = card.selectFirst(".product-name-short") ?: continue
            val name = nameEl.text().trim()

            val link = card.selectFirst("a[href~=^/product/]")
            val href = link?.attr("href") ?: ""
            val id = Regex("/product/(\\d+)/").find(href)?.groupValues?.get(1)?.toIntOrNull()
                ?: continue

            val imageUrl = card.selectFirst(".box-header-img")?.attr("src")

            // اگر کارت محصول کلاس desableProduct داشته باشد، یعنی «موقتاً ناموجود» است.
            val available = !card.hasClass("desableProduct")

            result.add(Product(id, name, imageUrl, available))
        }
        return result
    }
}
