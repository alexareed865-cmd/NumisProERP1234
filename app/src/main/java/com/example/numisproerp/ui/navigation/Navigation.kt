package com.numisproerp.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.numisproerp.ui.screens.*
import androidx.navigation.NavType
import androidx.navigation.navArgument

sealed class Screen(val route: String) {
    object Dashboard : Screen("dashboard")
    object Stock : Screen("stock")
    object Clients : Screen("clients")
    object Reports : Screen("reports")
    object Suppliers : Screen("suppliers")
    object Purchase : Screen("purchase")
    object Sale : Screen("sale")
    object Expenses : Screen("expenses")
    object Documents : Screen("documents")
    object Details : Screen("details/{type}/{title}") {
        fun passArguments(type: String, title: String): String = "details/$type/$title"
    }
    object Settings : Screen("settings")
    object Products : Screen("products")  // Усі товари в базі (drawer "Товари")
    object Writeoff : Screen("writeoff")  // Списання товарів зі складу
    object History : Screen("history")    // Повна історія операцій
    object Catalog : Screen("catalog")
    object MyCollection : Screen("my_collection")  // Моя колекція (п. 12-13 ТЗ)
    object Help : Screen("help")                    // Довідка (п. 15 ТЗ)
    object Notifications : Screen("notifications")  // Сповіщення (п. 15 ТЗ)
}

@Composable
fun NavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    onNavigate: (String) -> Unit = {}
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Dashboard.route,
        modifier = modifier
    ) {
        composable(Screen.Dashboard.route) {
            DashboardScreen(navController = navController)
        }
        composable(Screen.Stock.route) {
            StockScreen(navController = navController)
        }
        composable(Screen.Clients.route) {
            ClientsScreen(navController = navController)
        }
        composable(Screen.Reports.route) {
            ReportsScreen(navController = navController)
        }
        composable(Screen.Purchase.route) {
            PurchaseScreen(navController = navController)
        }
        composable(Screen.Sale.route) {
            SaleScreen(navController = navController)
        }
        composable(Screen.Suppliers.route) {
            SuppliersScreen(navController = navController)
        }
        composable(Screen.Expenses.route) {
            ExpensesScreen(navController = navController)
        }
        composable(Screen.Documents.route) {
            DocumentsScreen(navController = navController)
        }
        // ДОДАНО: реальний екран каталогу
        composable(Screen.Catalog.route) {
            CatalogScreen(navController = navController)
        }
        composable(
            route = Screen.Details.route,
            arguments = listOf(
                navArgument("type") { type = NavType.StringType },
                navArgument("title") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val type = backStackEntry.arguments?.getString("type") ?: "balance"
            val title = backStackEntry.arguments?.getString("title") ?: "Деталі"
            DetailsScreen(
                navController = navController,
                type = type,
                title = title
            )
        }
        // Усі товари в базі (drawer пункт "Товари")
        composable(Screen.Products.route) {
            ProductsScreen(navController = navController)
        }
        composable(Screen.Writeoff.route) {
            WriteoffScreen(navController = navController)
        }
        composable(Screen.History.route) {
            HistoryScreen(navController = navController)
        }
        composable(Screen.Settings.route) {
            SettingsScreen(navController = navController)
        }
        composable(Screen.MyCollection.route) {
            MyCollectionScreen(navController = navController)
        }
        composable(Screen.Help.route) {
            HelpScreen(navController = navController)
        }
        composable(Screen.Notifications.route) {
            NotificationsScreen(navController = navController)
        }
    }
}