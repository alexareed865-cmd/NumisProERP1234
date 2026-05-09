package com.numisproerp

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Store
import androidx.compose.material.icons.outlined.Help
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.numisproerp.data.settings.SettingsManager
import com.numisproerp.ui.navigation.NavGraph
import com.numisproerp.ui.navigation.Screen
import com.numisproerp.ui.theme.NumisProERPTheme
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
            NumisProERPTheme(appTheme = theme) {
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
        DrawerItem("Головне меню", Screen.Dashboard.route, false),
        DrawerItem("Товари", Screen.Products.route, false),
        DrawerItem("Документи", Screen.Documents.route, false),
        DrawerItem("Витрати", Screen.Expenses.route, false),
        DrawerItem("Звіти", Screen.Reports.route, false),
        DrawerItem("Постачальники", Screen.Suppliers.route, false),
        DrawerItem("Клієнти", Screen.Clients.route, false),
        DrawerItem("Списання", Screen.Writeoff.route, false),
        DrawerItem("Історія", Screen.History.route, false),
        DrawerItem("Налаштування", Screen.Settings.route, false)
    )

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                drawerItems.forEach { item ->
                    NavigationDrawerItem(
                        label = { Text(item.title) },
                        selected = false,
                        onClick = {
                            scope.launch { drawerState.close() }
                            if (item.isPlaceholder) {
                                Toast.makeText(context, "Розділ в розробці", Toast.LENGTH_SHORT).show()
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
                    TopBar(onMenuClick = { scope.launch { drawerState.open() } })
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
fun TopBar(onMenuClick: () -> Unit) {
    val context = LocalContext.current
    TopAppBar(
        title = { Text("NumisProERP") },
        navigationIcon = {
            IconButton(onClick = onMenuClick) {
                Icon(Icons.Default.Menu, contentDescription = "Меню")
            }
        },
        actions = {
            IconButton(onClick = { Toast.makeText(context, "Довідка в розробці", Toast.LENGTH_SHORT).show() }) {
                Icon(Icons.Outlined.Help, contentDescription = "Довідка")
            }
            IconButton(onClick = { Toast.makeText(context, "Сповіщення в розробці", Toast.LENGTH_SHORT).show() }) {
                BadgedBox(badge = { Badge { Text(" ") } }) {
                    Icon(Icons.Default.Notifications, contentDescription = "Сповіщення")
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
        BottomNavItem("Головна", Icons.Default.Home, Screen.Dashboard.route, false),
        BottomNavItem("Каталог", Icons.Default.Store, Screen.Catalog.route, false),
        BottomNavItem("Склад", Icons.Default.Store, Screen.Stock.route, false),
        BottomNavItem("Налаштування", Icons.Default.Settings, Screen.Settings.route, false)
    )

    NavigationBar {
        items.forEach { item ->
            NavigationBarItem(
                selected = false,
                onClick = {
                    if (item.isPlaceholder) {
                        Toast.makeText(context, "${item.title} в розробці", Toast.LENGTH_SHORT).show()
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

data class DrawerItem(val title: String, val route: String, val isPlaceholder: Boolean)
data class BottomNavItem(val title: String, val icon: androidx.compose.ui.graphics.vector.ImageVector, val route: String, val isPlaceholder: Boolean)
