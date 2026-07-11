package com.example.ui

import androidx.navigation.NavController

object BrushNavigator {
    /**
     * 跳转至冒险商店
     */
    fun navigateToShop(navController: NavController) {
        navController.navigate("adventure_shop")
    }

    /**
     * 跳转至武器库（笔刷库），可选参数 focusBrushId 用于高亮并滚动定位
     */
    fun navigateToLibrary(navController: NavController, focusBrushId: String? = null) {
        val route = if (focusBrushId != null) "brush_library?focus=$focusBrushId" else "brush_library"
        navController.navigate(route) {
            // 避免路由栈无限堆积
            popUpTo("home") { saveState = true }
            launchSingleTop = true
            restoreState = true
        }
    }

    /**
     * 跳转至武器详情页
     */
    fun navigateToDetail(navController: NavController, brushId: String) {
        navController.navigate("brush_detail/$brushId")
    }

    /**
     * 跳转至调校试练场
     */
    fun navigateToTuning(navController: NavController, brushId: String) {
        navController.navigate("brush_tuning/$brushId")
    }
}
