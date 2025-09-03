package dev.anonymous.hurriya.admin.presentation.navigation

import androidx.annotation.IdRes
import androidx.navigation.NavController
import androidx.navigation.NavDirections
import androidx.navigation.NavOptions
import dev.anonymous.hurriya.admin.R

private val options = NavOptions.Builder()
    .setEnterAnim(R.anim.enter_from_right)
    .setExitAnim(R.anim.exit_to_left_dim)
    .setPopEnterAnim(R.anim.enter_from_left_restore)
    .setPopExitAnim(R.anim.exit_to_right)
    .build()

fun NavController.navigateWithAnimation(directions: NavDirections) {
    navigate(directions, options)
}

fun NavController.navigateWithAnimation(@IdRes resId: Int) {
    navigate(resId, null, options)
}
