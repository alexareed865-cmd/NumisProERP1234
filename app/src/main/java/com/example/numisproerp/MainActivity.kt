package com.numisproerp

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Store
import androidx.compose.material.icons.automirrored.outlined.Help
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.Receipt
import androidx.compose.material.icons.outlined.Sell
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.ui.unit.dp
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.compose.runtime.CompositionLocalProvider
import com.numisproerp.data.settings.SettingsManager
import com.numisproerp.ui.i18n.LocalAppLanguage
import com.numisproerp.ui.i18n.tr
import com.numisproerp.ui.navigation.NavGraph
import com.numisproerp.ui.navigation.Screen
import com.numisproerp.ui.theme.NumisProERPTheme
import com.numisproerp.ui.viewmodel.NotificationsViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var settingsManager: SettingsManager

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val theme by settingsManager.themeState
            val language by settingsManager.languageState
            NumisProERPTheme(appTheme = theme) {
                CompositionLocalProvider(LocalAppLanguage provides language) {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        NumisProERPNavigation()
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NumisProERPNavigation() {
    val context = LocalContext.current
    val navController = rememberNavController()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val showBars = currentRoute == Screen.Dashboard.route

    val drawerItems = listOf(
        DrawerItem(tr("Головне меню", "Home"), Screen.Dashboard.route, false, Icons.Default.Home),
        DrawerItem(tr("Додати товар", "Add product"), Screen.Purchase.route, false, Icons.Default.Add),
        DrawerItem(tr("Товари", "Products"), Screen.Products.route, false, Icons.Outlined.Inventory2),
        DrawerItem(tr("Мої замітки", "My Notes"), Screen.MyNotes.route, false, Icons.Outlined.Edit),
        DrawerItem(tr("Історія продажів", "Sales History"), Screen.SalesHistory.route, false, Icons.Outlined.Sell),
        DrawerItem(tr("Документи", "Documents"), Screen.Documents.route, false, Icons.Outlined.Description),
        DrawerItem(tr("Витрати", "Expenses"), Screen.Expenses.route, false, Icons.Outlined.Receipt),
        DrawerItem(tr("Звіти", "Reports"), Screen.Reports.route, false, Icons.Outlined.BarChart),
        DrawerItem(tr("Постачальники", "Suppliers"), Screen.Suppliers.route, false, Icons.Default.ShoppingCart),
        DrawerItem(tr("Клієнти", "Clients"), Screen.Clients.route, false, Icons.Default.People),
        DrawerItem(tr("Списання", "Writeoff"), Screen.Writeoff.route, false, Icons.Outlined.Delete),
        DrawerItem(tr("Історія", "History"), Screen.History.route, false, Icons.Outlined.History),
        DrawerItem(tr("Налаштування", "Settings"), Screen.Settings.route, false, Icons.Default.Settings)
    )
    val sectionInDevText = tr("Розділ в розробці", "Section in development")

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                drawerItems.forEach { item ->
                    NavigationDrawerItem(
                        icon = {
                            Icon(
                                imageVector = item.icon,
                                contentDescription = item.title,
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        label = { Text(item.title) },
                        selected = false,
                        onClick = {
                            scope.launch { drawerState.close() }
                            if (item.isPlaceholder) {
                                Toast.makeText(context, sectionInDevText, Toast.LENGTH_SHORT).show()
                            } else {
                                navController.navigate(item.route) {
                                    popUpTo(Screen.Dashboard.route) { inclusive = false }
                                    launchSingleTop = true
                                }
                            }
                        }
                    )
                }
            }
        }
    ) {
        Scaffold(
            topBar = {
                if (showBars) {
                    TopBar(
                        onMenuClick = { scope.launch { drawerState.open() } },
                        onHelpClick = {
                            navController.navigate(Screen.Help.route) {
                                launchSingleTop = true
                            }
                        },
                        onNotificationsClick = {
                            navController.navigate(Screen.Notifications.route) {
                                launchSingleTop = true
                            }
                        }
                    )
                }
            },
            bottomBar = {
                if (showBars) {
                    BottomBar(navController = navController)
                }
            }
        ) { paddingValues ->
            NavGraph(
                navController = navController,
                modifier = Modifier.padding(paddingValues),
                onNavigate = { route ->
                    navController.navigate(route) {
                        popUpTo(Screen.Dashboard.route) { inclusive = false }
                        launchSingleTop = true
                    }
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBar(
    onMenuClick: () -> Unit,
    onHelpClick: () -> Unit,
    onNotificationsClick: () -> Unit,
    notificationsViewModel: NotificationsViewModel = hiltViewModel()
) {
    val notifications by notificationsViewModel.notifications.collectAsState()
    val unreadCount = notifications.size
    TopAppBar(
        title = { Text("NumisProERP") },
        navigationIcon = {
            IconButton(onClick = onMenuClick) {
                Icon(Icons.Default.Menu, contentDescription = tr("Меню", "Menu"))
            }
        },
        actions = {
            IconButton(onClick = onHelpClick) {
                Icon(Icons.AutoMirrored.Outlined.Help, contentDescription = tr("Довідка", "Help"))
            }
            IconButton(onClick = onNotificationsClick) {
                BadgedBox(badge = {
                    if (unreadCount > 0) {
                        Badge { Text(unreadCount.toString()) }
                    }
                }) {
                    Icon(Icons.Default.Notifications, contentDescription = tr("Сповіщення", "Notifications"))
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    )
}

@Composable
fun BottomBar(navController: NavHostController) {
    val context = LocalContext.current
    val items = listOf(
        BottomNavItem(tr("Головна", "Home"), Icons.Default.Home, Screen.Dashboard.route, false),
        BottomNavItem(tr("Каталог", "Catalog"), Icons.Default.Store, Screen.Catalog.route, false),
        BottomNavItem(tr("Склад", "Stock"), Icons.Default.Store, Screen.Stock.route, false),
        BottomNavItem(tr("Налаштування", "Settings"), Icons.Default.Settings, Screen.Settings.route, false)
    )
    val inDevSuffix = tr(" в розробці", " in development")

    NavigationBar {
        items.forEach { item ->
            NavigationBarItem(
                selected = false,
                onClick = {
                    if (item.isPlaceholder) {
                        Toast.makeText(context, "${item.title}$inDevSuffix", Toast.LENGTH_SHORT).show()
                    } else {
                        navController.navigate(item.route) {
                            popUpTo(Screen.Dashboard.route) { inclusive = false }
                            launchSingleTop = true
                        }
                    }
                },
                icon = { Icon(item.icon, contentDescription = item.title) },
                label = { Text(item.title) }
            )
        }
    }
}

data class DrawerItem(val title: String, val route: String, val isPlaceholder: Boolean, val icon: ImageVector = Icons.Default.Home)
data class BottomNavItem(val title: String, val icon: androidx.compose.ui.graphics.vector.ImageVector, val route: String, val isPlaceholder: Boolean)
