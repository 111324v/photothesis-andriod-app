package com.photosynthesis.app.ui

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.photosynthesis.app.ui.screens.ArchiveScreen
import com.photosynthesis.app.ui.screens.CameraScreen
import com.photosynthesis.app.ui.screens.ChoosePlantScreen
import com.photosynthesis.app.ui.screens.HomeScreen
import com.photosynthesis.app.ui.screens.ProfileScreen
import com.photosynthesis.app.ui.screens.SplashScreen

/**
 * 导航路由定义
 * 页面流：Splash → ChoosePlant → Home ↔ Camera / Archive / Profile
 */
object Routes {
    const val SPLASH = "splash"
    const val CHOOSE_PLANT = "choose_plant"
    const val HOME = "home"
    const val CAMERA = "camera"
    const val ARCHIVE = "archive"
    const val PROFILE = "profile"
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Routes.SPLASH
    ) {
        // 启动页
        composable(Routes.SPLASH) {
            SplashScreen(
                onTap = { navController.navigate(Routes.CHOOSE_PLANT) {
                    popUpTo(Routes.SPLASH) { inclusive = true }
                }}
            )
        }

        // 选择植物页
        composable(Routes.CHOOSE_PLANT) {
            ChoosePlantScreen(
                onPlantChosen = { plantType ->
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.CHOOSE_PLANT) { inclusive = true }
                    }
                }
            )
        }

        // 主页
        composable(Routes.HOME) {
            HomeScreen(
                onNavigateToCamera = { navController.navigate(Routes.CAMERA) },
                onNavigateToArchive = { navController.navigate(Routes.ARCHIVE) },
                onNavigateToProfile = { navController.navigate(Routes.PROFILE) }
            )
        }

        // 拍摄页
        composable(Routes.CAMERA) {
            CameraScreen(
                onBack = { navController.popBackStack() },
                onNavigateToArchive = { navController.navigate(Routes.ARCHIVE) }
            )
        }

        // 光档案页
        composable(Routes.ARCHIVE) {
            ArchiveScreen(
                onBack = { navController.popBackStack() }
            )
        }

        // 个人页
        composable(Routes.PROFILE) {
            ProfileScreen(
                onBack = { navController.popBackStack() }
            )
        }
    }
}
