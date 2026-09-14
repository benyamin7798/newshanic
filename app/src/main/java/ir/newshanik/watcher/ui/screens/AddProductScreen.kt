package ir.newshanik.watcher.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ir.newshanik.watcher.data.Category
import ir.newshanik.watcher.data.Product
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddProductScreen(
    cachedCategories: List<Category>,
    onFetchCategories: suspend () -> List<Category>,
    onFetchProducts: suspend (Int) -> List<Product>,
    onSave: (Category, Product) -> Unit,
    onBack: () -> Unit
) {
    var categories by remember { mutableStateOf(cachedCategories) }
    var isLoadingCategories by remember { mutableStateOf(false) }
    var selectedCategory by remember { mutableStateOf<Category?>(null) }

    var products by remember { mutableStateOf<List<Product>>(emptyList()) }
    var isLoadingProducts by remember { mutableStateOf(false) }
    var selectedProduct by remember { mutableStateOf<Product?>(null) }

    var categoryMenuExpanded by remember { mutableStateOf(false) }
    var productMenuExpanded by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()

    fun refreshCategories() {
        scope.launch {
            isLoadingCategories = true
            val fresh = withContext(Dispatchers.IO) { onFetchCategories() }
            if (fresh.isNotEmpty()) categories = fresh
            isLoadingCategories = false
        }
    }

    fun loadProducts(category: Category) {
        scope.launch {
            isLoadingProducts = true
            selectedProduct = null
            products = withContext(Dispatchers.IO) { onFetchProducts(category.id) }
            isLoadingProducts = false
        }
    }

    LaunchedEffect(Unit) {
        if (categories.isEmpty()) refreshCategories()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("افزودن محصول برای دیده‌بانی") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "بازگشت")
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).padding(16.dp).fillMaxSize()) {

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "۱. انتخاب دسته‌بندی",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = { refreshCategories() }) {
                    Icon(Icons.Filled.Refresh, contentDescription = "بازخوانی دسته‌بندی‌ها")
                }
            }

            if (isLoadingCategories) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp))
            }

            ExposedDropdownMenuBox(
                expanded = categoryMenuExpanded,
                onExpandedChange = { categoryMenuExpanded = it }
            ) {
                OutlinedTextField(
                    value = selectedCategory?.name ?: "دسته‌بندی را انتخاب کنید",
                    onValueChange = {},
                    readOnly = true,
                    modifier = Modifier.menuAnchor().fillMaxWidth(),
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryMenuExpanded)
                    }
                )
                ExposedDropdownMenu(
                    expanded = categoryMenuExpanded,
                    onDismissRequest = { categoryMenuExpanded = false }
                ) {
                    categories.forEach { category ->
                        DropdownMenuItem(
                            text = { Text(category.name) },
                            onClick = {
                                selectedCategory = category
                                categoryMenuExpanded = false
                                loadProducts(category)
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text("۲. انتخاب محصول", style = MaterialTheme.typography.titleMedium)

            if (isLoadingProducts) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp))
            }

            ExposedDropdownMenuBox(
                expanded = productMenuExpanded,
                onExpandedChange = { if (selectedCategory != null) productMenuExpanded = it }
            ) {
                val label = selectedProduct?.let {
                    "${it.name}  —  ${if (it.available) "موجود" else "ناموجود"}"
                } ?: "ابتدا دسته‌بندی را انتخاب کنید"

                OutlinedTextField(
                    value = label,
                    onValueChange = {},
                    readOnly = true,
                    enabled = selectedCategory != null,
                    modifier = Modifier.menuAnchor().fillMaxWidth(),
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = productMenuExpanded)
                    }
                )
                ExposedDropdownMenu(
                    expanded = productMenuExpanded,
                    onDismissRequest = { productMenuExpanded = false }
                ) {
                    products.forEach { product ->
                        DropdownMenuItem(
                            text = {
                                Text("${product.name}  —  ${if (product.available) "موجود ✅" else "ناموجود ❌"}")
                            },
                            onClick = {
                                selectedProduct = product
                                productMenuExpanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = {
                    val cat = selectedCategory
                    val prod = selectedProduct
                    if (cat != null && prod != null) {
                        onSave(cat, prod)
                        onBack()
                    }
                },
                enabled = selectedCategory != null && selectedProduct != null,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("افزودن به لیست دیده‌بانی")
            }
        }
    }
}
