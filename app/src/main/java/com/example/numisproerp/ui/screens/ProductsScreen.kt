package com.numisproerp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Clear
import androidx.compose.material.icons.outlined.PhotoCamera
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import androidx.compose.ui.platform.LocalContext
import android.widget.Toast
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import com.numisproerp.data.entities.Product
import com.numisproerp.ui.i18n.tr
import com.numisproerp.ui.theme.AccentBlue
import com.numisproerp.ui.theme.AccentGreen
import com.numisproerp.ui.theme.AccentOrange
import com.numisproerp.ui.theme.IOSDesign
import com.numisproerp.ui.theme.IOSIconChip
import com.numisproerp.ui.viewmodel.ProductsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductsScreen(
    navController: NavHostController,
    viewModel: ProductsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedProduct by remember { mutableStateOf<Product?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        IconButton(
            onClick = { navController.popBackStack() },
            modifier = Modifier
                .padding(16.dp)
                .align(Alignment.TopStart)
        ) {
            Icon(
                Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = tr("Назад", "Back"),
                tint = MaterialTheme.colorScheme.primary
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 16.dp, start = 16.dp, end = 16.dp, bottom = 16.dp)
        ) {
            Text(
                text = tr("Товари", "Products"),
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp, bottom = 12.dp),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )

            OutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = { viewModel.updateSearchQuery(it) },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text(tr("Пошук за назвою, серією або ID...", "Search by name, series or ID...")) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (uiState.searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.updateSearchQuery("") }) {
                            Icon(Icons.Outlined.Clear, contentDescription = tr("Очистити", "Clear"))
                        }
                    }
                },
                shape = RoundedCornerShape(IOSDesign.ButtonCornerRadius)
            )

            if (uiState.categories.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    item {
                        FilterChip(
                            selected = uiState.selectedCategory.isBlank(),
                            onClick = { viewModel.updateSelectedCategory("") },
                            label = { Text(tr("Усі", "All")) }
                        )
                    }
                    items(uiState.categories) { category ->
                        FilterChip(
                            selected = uiState.selectedCategory == category,
                            onClick = { viewModel.updateSelectedCategory(category) },
                            label = { Text(category) }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            val visibleProducts = viewModel.filteredProducts()

            when {
                uiState.isLoading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                visibleProducts.isEmpty() -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text = tr("Немає товарів у базі.\nІмпортуйте каталог через 'Каталог НБУ'", "No products in database.\nImport via 'NBU Catalog'"),
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
                else -> {
                    Text(
                        text = "${tr("Знайдено", "Found")}: ${visibleProducts.size}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        items(visibleProducts) { product ->
                            ProductRowCard(
                                product = product,
                                imageUrl = viewModel.getProductImageUrl(product),
                                onClick = { selectedProduct = product }
                            )
                        }
                    }
                }
            }
        }
    }

    val coroutineScope = rememberCoroutineScope()
    selectedProduct?.let { product ->
        val context = LocalContext.current
        val imageUrls = viewModel.getProductImageUrls(product)
        val addedText = tr("Додано до колекції", "Added to collection")
        val alreadyText = tr("Вже в колекції", "Already in collection")
        ProductDetailDialog(
            product = product,
            imageUrlFront = imageUrls.first,
            imageUrlBack = imageUrls.second,
            onDismiss = { selectedProduct = null },
            onAddToCollection = {
                coroutineScope.launch {
                    val added = viewModel.addProductToCollection(product)
                    Toast.makeText(
                        context,
                        if (added) addedText else alreadyText,
                        Toast.LENGTH_SHORT
                    ).show()
                }
                selectedProduct = null
            }
        )
    }
}

@Composable
private fun ProductRowCard(
    product: Product,
    imageUrl: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(IOSDesign.CardCornerRadius),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = IOSDesign.CardElevation)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ProductThumbnail(photoPath = imageUrl)
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = product.name,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp
                )
                if (product.series.isNotBlank()) {
                    Text(
                        text = product.series,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
                Row(
                    modifier = Modifier.padding(top = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (product.category.isNotBlank()) {
                        TagChip(text = product.category, color = AccentBlue)
                    }
                    if (product.material.isNotBlank()) {
                        TagChip(text = product.material, color = AccentOrange)
                    }
                }
            }
            Text(
                text = product.catalogId,
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
            )
        }
    }
}

@Composable
private fun TagChip(text: String, color: androidx.compose.ui.graphics.Color) {
    Text(
        text = text,
        fontSize = 10.sp,
        color = color,
        modifier = Modifier
            .background(color.copy(alpha = 0.12f), RoundedCornerShape(4.dp))
            .padding(horizontal = 6.dp, vertical = 2.dp)
    )
}

@Composable
private fun ProductThumbnail(photoPath: String) {
    val context = LocalContext.current
    if (photoPath.isBlank()) {
        IOSIconChip(
            icon = Icons.Outlined.PhotoCamera,
            tint = MaterialTheme.colorScheme.primary,
            chipSize = IOSDesign.IconChipLarge,
            iconSize = IOSDesign.IconSizeLarge,
            cornerRadius = IOSDesign.CardCornerRadiusSmall,
            contentDescription = tr("Фото", "Photo")
        )
    } else {
        AsyncImage(
            model = ImageRequest.Builder(context).data(photoPath).build(),
            contentDescription = tr("Фото товару", "Product photo"),
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(IOSDesign.IconChipLarge)
                .clip(RoundedCornerShape(IOSDesign.CardCornerRadiusSmall))
        )
    }
}

@Composable
fun ProductDetailDialog(
    product: Product,
    imageUrlFront: String = product.photoPath,
    imageUrlBack: String = "",
    onDismiss: () -> Unit,
    onAddToCollection: (() -> Unit)? = null
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (onAddToCollection != null) {
                    TextButton(onClick = onAddToCollection) {
                        Text(tr("В колекцію", "To collection"), color = AccentGreen)
                    }
                }
                TextButton(onClick = onDismiss) { Text(tr("Закрити", "Close")) }
            }
        },
        title = {
            Text(
                text = product.name,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                if (imageUrlFront.isNotBlank() || imageUrlBack.isNotBlank()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (imageUrlFront.isNotBlank()) {
                            Column(
                                modifier = Modifier.weight(1f),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                ProductDetailPhoto(photoPath = imageUrlFront)
                                if (imageUrlBack.isNotBlank()) {
                                    Text(
                                        text = tr("Аверс", "Obverse"),
                                        fontSize = 10.sp,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                                        modifier = Modifier.padding(top = 2.dp)
                                    )
                                }
                            }
                        }
                        if (imageUrlBack.isNotBlank()) {
                            Column(
                                modifier = Modifier.weight(1f),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                ProductDetailPhoto(photoPath = imageUrlBack)
                                Text(
                                    text = tr("Реверс", "Reverse"),
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                                    modifier = Modifier.padding(top = 2.dp)
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
                DetailRow("ID каталогу", product.catalogId)
                DetailRow(tr("Серія", "Series"), product.series)
                DetailRow(tr("Категорія", "Category"), product.category)
                DetailRow(tr("Матеріал", "Material"), product.material)
                DetailRow(tr("Номінал", "Nominal"), product.nominal)
                DetailRow(tr("Якість", "Quality"), product.quality)
                DetailRow(tr("Діаметр", "Diameter"), product.diameter)
                DetailRow(tr("Вага", "Weight"), product.weight)
                DetailRow(tr("Тираж (заявлено)", "Mintage (announced)"), product.mintageAnnounced)
                DetailRow(tr("Тираж (фактично)", "Mintage (actual)"), product.mintageActual)
                DetailRow(tr("Дата випуску", "Issue date"), product.issueDate)
                DetailRow(tr("Художник", "Artist"), product.artist)
                DetailRow(tr("Скульптор", "Sculptor"), product.sculptor)
            }
        }
    )
}

@Composable
private fun ProductDetailPhoto(photoPath: String) {
    val context = LocalContext.current
    if (photoPath.isBlank()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp)
                .clip(RoundedCornerShape(IOSDesign.CardCornerRadius))
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Outlined.PhotoCamera,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                modifier = Modifier.size(48.dp)
            )
        }
    } else {
        AsyncImage(
            model = ImageRequest.Builder(context).data(photoPath).build(),
            contentDescription = tr("Фото", "Photo"),
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .clip(RoundedCornerShape(IOSDesign.CardCornerRadius))
        )
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    if (value.isBlank()) return
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            modifier = Modifier.weight(1f)
        )
        Text(
            text = value,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(1.5f)
        )
    }
}
