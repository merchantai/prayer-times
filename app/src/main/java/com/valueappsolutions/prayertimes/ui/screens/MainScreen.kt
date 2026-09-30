package com.valueappsolutions.prayertimes.ui.screens

import android.Manifest
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.Alignment
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.NavType
import androidx.navigation.navArgument
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import kotlinx.coroutines.launch
import com.valueappsolutions.prayertimes.BuildConfig
import com.valueappsolutions.prayertimes.data.local.UserSettings
import com.valueappsolutions.prayertimes.ui.viewmodel.PrayerViewModel

sealed class BottomNavItem(val route: String, val icon: ImageVector, val label: String) {
    object Home : BottomNavItem("dashboard", Icons.Rounded.Home, "Home")
    object Qibla : BottomNavItem("qibla", Icons.Rounded.Explore, "Qibla")
    object Hijri : BottomNavItem("hijri", Icons.Rounded.DateRange, "Hijri")
    object Locations : BottomNavItem("locations", Icons.Rounded.LocationOn, "Locations")
    object Menu : BottomNavItem("menu", Icons.Rounded.MoreVert, "")
}

sealed class DrawerNavItem(val route: String, val icon: ImageVector, val label: String) {
    object Home : DrawerNavItem("dashboard", Icons.Rounded.Home, "Home")
    object Settings : DrawerNavItem("settings", Icons.Rounded.Settings, "Settings")
    object About : DrawerNavItem("about", Icons.Rounded.Info, "About App")
    object Terms : DrawerNavItem("terms", Icons.Rounded.Description, "Terms & Conditions")
    object Privacy : DrawerNavItem("privacy", Icons.Rounded.Policy, "Privacy Policy")
}

@Composable
fun MainScreen(
    navController: NavHostController,
    prayerViewModel: PrayerViewModel,
    uiState: com.valueappsolutions.prayertimes.ui.viewmodel.PrayerUiState,
    settings: UserSettings,
    isDarkTheme: Boolean,
    onToggleTheme: () -> Unit,
    widthSizeClass: WindowWidthSizeClass,
    onRequestLocation: () -> Unit,
    locationPermissionRequest: androidx.activity.result.ActivityResultLauncher<Array<String>>,
    checkPermissions: () -> Boolean,
    fetchLocationAndUpdate: () -> Unit
) {
    val isExpanded = widthSizeClass != WindowWidthSizeClass.Compact
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    
    val bottomItems = listOf(
        BottomNavItem.Home,
        BottomNavItem.Qibla,
        BottomNavItem.Hijri,
        BottomNavItem.Locations,
        BottomNavItem.Menu
    )
    
    val drawerItems = listOf(
        DrawerNavItem.Home,
        DrawerNavItem.Settings,
        DrawerNavItem.About,
        DrawerNavItem.Terms,
        DrawerNavItem.Privacy
    )

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Spacer(Modifier.height(12.dp))
                Text(
                    text = "Prayer Times",
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary
                )
                HorizontalDivider()
                Spacer(Modifier.height(12.dp))
                
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route
                
                drawerItems.forEach { item ->
                    val isSelected = currentRoute == item.route
                    NavigationDrawerItem(
                        icon = { Icon(item.icon, contentDescription = item.label) },
                        label = { Text(item.label) },
                        selected = isSelected,
                        onClick = {
                            scope.launch { drawerState.close() }
                            navController.navigate(item.route) {
                                popUpTo(navController.graph.findStartDestination().id)
                                launchSingleTop = true
                            }
                        },
                        colors = NavigationDrawerItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.onSurface,
                            selectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.05f),
                            unselectedIconColor = MaterialTheme.colorScheme.primary,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurface,
                            unselectedContainerColor = Color.Transparent
                        ),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .padding(NavigationDrawerItemDefaults.ItemPadding)
                            .border(
                                BorderStroke(
                                    1.dp,
                                    if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                                ),
                                shape = RoundedCornerShape(16.dp)
                            )
                    )
                }
                
                Spacer(modifier = Modifier.weight(1f))
                
                Text(
                    text = "v${BuildConfig.VERSION_NAME}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .align(Alignment.End)
                        .padding(16.dp)
                )
            }
        }
    ) {
        Scaffold(
            contentWindowInsets = WindowInsets(0.dp),
            bottomBar = {
                if (!isExpanded) {
                    NavigationBar(
                        containerColor = MaterialTheme.colorScheme.surface,
                        contentColor = MaterialTheme.colorScheme.onSurface
                    ) {
                        val navBackStackEntry by navController.currentBackStackEntryAsState()
                        val currentRoute = navBackStackEntry?.destination?.route
                        bottomItems.forEach { item ->
                            NavigationBarItem(
                                icon = { 
                                    Icon(
                                        item.icon, 
                                        contentDescription = if (item.label.isNotEmpty()) item.label else "Menu",
                                        modifier = if (item.route == "menu") Modifier.size(40.dp).offset(x = (-8).dp, y = 8.dp) else Modifier
                                    ) 
                                },
                                label = { Text(item.label) },
                                selected = currentRoute == item.route && item.route != "menu",
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = MaterialTheme.colorScheme.primary,
                                    selectedTextColor = MaterialTheme.colorScheme.primary,
                                    indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                                ),
                                onClick = {
                                    if (item.route == "menu") {
                                        scope.launch { drawerState.open() }
                                    } else {
                                        navController.navigate(item.route) {
                                            popUpTo(navController.graph.findStartDestination().id)
                                            launchSingleTop = true
                                        }
                                    }
                                }
                            )
                        }
                    }
                }
            }
        ) { innerPadding ->
            Row(modifier = Modifier.padding(innerPadding)) {
                if (isExpanded) {
                    NavigationRail(
                        containerColor = MaterialTheme.colorScheme.surface,
                        contentColor = MaterialTheme.colorScheme.onSurface
                    ) {
                        val navBackStackEntry by navController.currentBackStackEntryAsState()
                        val currentRoute = navBackStackEntry?.destination?.route
                        bottomItems.forEach { item ->
                            NavigationRailItem(
                                icon = { 
                                    Icon(
                                        item.icon, 
                                        contentDescription = if (item.label.isNotEmpty()) item.label else "Menu",
                                        modifier = if (item.route == "menu") Modifier.size(40.dp).offset(x = (-8).dp, y = 8.dp) else Modifier
                                    ) 
                                },
                                label = { Text(item.label) },
                                selected = currentRoute == item.route && item.route != "menu",
                                colors = NavigationRailItemDefaults.colors(
                                    selectedIconColor = MaterialTheme.colorScheme.primary,
                                    selectedTextColor = MaterialTheme.colorScheme.primary,
                                    indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                                ),
                                onClick = {
                                    if (item.route == "menu") {
                                        scope.launch { drawerState.open() }
                                    } else {
                                        navController.navigate(item.route) {
                                            popUpTo(navController.graph.findStartDestination().id)
                                            launchSingleTop = true
                                        }
                                    }
                                }
                            )
                        }
                    }
                }
                
                NavHost(
                    navController = navController,
                    startDestination = BottomNavItem.Home.route,
                    modifier = Modifier.weight(1f)
                ) {
                    composable("dashboard") {
                        val currentSettings by prayerViewModel.userSettings.collectAsState(initial = settings)
                        val currentUiState by prayerViewModel.uiState.collectAsState()
                        DashboardScreen(
                            uiState = currentUiState,
                            isDarkMode = isDarkTheme,
                            is24HourFormat = currentSettings.is24HourFormat,
                            isExpanded = isExpanded,
                            onToggleTheme = onToggleTheme,
                            onNavigateToSettings = {
                                navController.navigate("settings") {
                                    popUpTo(navController.graph.findStartDestination().id)
                                    launchSingleTop = true
                                }
                            },
                            onRequestLocation = onRequestLocation,
                            onRefresh = {
                                prayerViewModel.refreshData()
                            },
                            onOpenDrawer = {
                                scope.launch { drawerState.open() }
                            }
                        )
                    }
                    composable("qibla") {
                        QiblaCompassScreen(
                            viewModel = prayerViewModel
                        )
                    }
                    composable("hijri") {
                        HijriConverterScreen(hijriOffset = settings.hijriOffset)
                    }
                    composable("locations") {
                        LocationsListScreen(
                            viewModel = prayerViewModel,
                            onNavigateToDetail = { lat, lng, name, timezoneId ->
                                val tzParam = if (!timezoneId.isNullOrEmpty()) "?timezoneId=${java.net.URLEncoder.encode(timezoneId, "UTF-8")}" else ""
                                navController.navigate("location_detail/$lat/$lng/$name$tzParam")
                            }
                        )
                    }
                    composable(
                        route = "location_detail/{lat}/{lng}/{name}?timezoneId={timezoneId}",
                        arguments = listOf(
                            navArgument("lat") { type = NavType.StringType },
                            navArgument("lng") { type = NavType.StringType },
                            navArgument("name") { type = NavType.StringType },
                            navArgument("timezoneId") { 
                                type = NavType.StringType
                                nullable = true
                                defaultValue = null
                            }
                        )
                    ) { backStackEntry ->
                        val lat = backStackEntry.arguments?.getString("lat")?.toDoubleOrNull() ?: 0.0
                        val lng = backStackEntry.arguments?.getString("lng")?.toDoubleOrNull() ?: 0.0
                        val name = backStackEntry.arguments?.getString("name") ?: ""
                        val tz = backStackEntry.arguments?.getString("timezoneId")?.takeIf { it.isNotEmpty() }
                        
                        LocationDetailScreen(
                            viewModel = prayerViewModel,
                            lat = lat,
                            lng = lng,
                            name = name,
                            timezoneId = tz,
                            is24HourFormat = settings.is24HourFormat,
                            onNavigateBack = { navController.popBackStack() }
                        )
                    }
                    composable("settings") {
                        SettingsScreen(
                            onNavigateBack = { navController.popBackStack() }, // Pop to home or just do nothing if root
                            onNavigateToPrayerTimes = { navController.navigate("prayer_settings") },
                            onNavigateToLocation = { navController.navigate("location_settings") },
                            onNavigateToHijri = { navController.navigate("hijri_settings") },
                            onNavigateToGeneral = { navController.navigate("general_settings") },
                            onNavigateToNotifications = { navController.navigate("notification_settings") },
                            onNavigateToSilentMode = { navController.navigate("silent_mode_settings") }
                        )
                    }
                    composable("prayer_settings") {
                        PrayerTimeSettingsScreen(
                            viewModel = prayerViewModel,
                            onNavigateBack = { navController.popBackStack() }
                        )
                    }
                    composable("location_settings") {
                        LocationSettingsScreen(
                            viewModel = prayerViewModel,
                            settings = settings,
                            onNavigateBack = { navController.popBackStack() }
                        )
                    }
                    composable("hijri_settings") {
                        HijriSettingsScreen(
                            viewModel = prayerViewModel,
                            onNavigateBack = { navController.popBackStack() }
                        )
                    }
                    composable("general_settings") {
                        GeneralSettingsScreen(
                            viewModel = prayerViewModel,
                            settings = settings,
                            onNavigateBack = { navController.popBackStack() }
                        )
                    }
                    composable("notification_settings") {
                        NotificationSettingsScreen(
                            viewModel = prayerViewModel,
                            onNavigateBack = { navController.popBackStack() }
                        )
                    }
                    composable("silent_mode_settings") {
                        SilentModeSettingsScreen(
                            viewModel = prayerViewModel,
                            onNavigateBack = { navController.popBackStack() }
                        )
                    }
                    composable("about") {
                        AboutScreen(onNavigateBack = { navController.popBackStack() })
                    }
                    composable("terms") {
                        TermsAndConditionsScreen(onNavigateBack = { navController.popBackStack() })
                    }
                    composable("privacy") {
                        PrivacyPolicyScreen(onNavigateBack = { navController.popBackStack() })
                    }
                }
            }
        }
    }
}
