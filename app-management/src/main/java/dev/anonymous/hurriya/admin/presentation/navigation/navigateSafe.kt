package dev.anonymous.hurriya.admin.presentation.navigation

import androidx.navigation.NavController
import androidx.navigation.NavDirections

/**
 * get current destination using [currentDestination][NavController.currentDestination]
 * and navigate to navDirections if not equals destinationId
 * this function is safe to avoid problem of click twice to navigate
 * */
fun NavController.navigateSafe(navDirections: NavDirections) {
    currentDestination?.let { currentDestination ->
        val navAction = currentDestination.getAction(navDirections.actionId)
        navAction?.let { nonNullNavAction ->
            if (nonNullNavAction.destinationId != currentDestination.id) {
                navigate(navDirections)
            }
        }
    }
}
