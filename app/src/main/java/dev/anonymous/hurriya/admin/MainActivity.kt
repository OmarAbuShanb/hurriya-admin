package dev.anonymous.hurriya.admin

import android.animation.ValueAnimator
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateInterpolator
import androidx.activity.addCallback
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.animation.doOnEnd
import androidx.core.animation.doOnStart
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.GravityCompat
import androidx.core.view.isVisible
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import dagger.hilt.android.AndroidEntryPoint
import dev.anonymous.hurriya.admin.databinding.ActivityMainBinding
import dev.anonymous.hurriya.admin.presentation.navigation.navigateSafe
import dev.anonymous.hurriya.admin.presentation.screens.main.dashboard.DashboardFragmentDirections
import dev.anonymous.hurriya.admin.utils.ToolbarTitleCentering
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    lateinit var binding: ActivityMainBinding
    private val viewModel: MainViewModel by viewModels()
    private lateinit var navController: NavController
    private lateinit var appBarConfig: AppBarConfiguration

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        installSplashScreen()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        ToolbarTitleCentering.initWith(binding.toolbar, applicationContext)

        setupNavControllerAndDrawer()

        if (!viewModel.hasSetStartDestination) {
            setupStartDestination()
            viewModel.markStartDestinationSet()
        }

        setupDrawerMenuVisibility()
        setupDrawerMenuClicks()
        handleToolbarVisibility()
        handleBackPressed()
    }

    private fun setupNavControllerAndDrawer() {
        val navHostFragment =
            supportFragmentManager.findFragmentById(binding.mainNavHost.id) as NavHostFragment
        navController = navHostFragment.navController

        val topLevelDestinations = setOf(R.id.dashboardFragment)
        appBarConfig = AppBarConfiguration(topLevelDestinations, binding.drawerLayout)
        setupActionBarWithNavController(navController, appBarConfig)
        binding.navView.setupWithNavController(navController)

        binding.drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
        binding.drawerLayout.addDrawerListener(object : DrawerLayout.DrawerListener {
            override fun onDrawerSlide(drawerView: View, slideOffset: Float) {}
            override fun onDrawerOpened(drawerView: View) {
                binding.drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)
            }

            override fun onDrawerClosed(drawerView: View) {
                binding.drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
            }

            override fun onDrawerStateChanged(newState: Int) {}
        })
    }

    private fun setupDrawerMenuVisibility() {
        val menu = binding.navView.menu
        val inviteItem = menu.findItem(R.id.nav_invite_management)

        lifecycleScope.launch {
            viewModel.checkSuperAdmin().collectLatest { isSuper ->
                inviteItem.isVisible = isSuper
            }
        }
    }

    private fun setupDrawerMenuClicks() {
        binding.navView.setNavigationItemSelectedListener { menuItem ->
            binding.drawerLayout.closeDrawers()
            when (menuItem.itemId) {
                R.id.nav_staff -> {
                    navController.navigateSafe(
                        DashboardFragmentDirections.actionDashboardFragmentToStaffManagementFragment()
                    )
                }

                R.id.nav_invite_management -> {
                    navController.navigateSafe(
                        DashboardFragmentDirections.actionToInviteManagementFragment()
                    )
                }

                R.id.nav_logout -> {
                    viewModel.logoutUser()
                    navController.navigateSafe(
                        DashboardFragmentDirections.actionToLoginFragment()
                    )
                }
            }
            true
        }
    }

    private fun handleToolbarVisibility() {
        val showUpButtonIn = setOf(R.id.inviteRegistrationFragment, R.id.loginFragment)
        val hideToolbarIn = setOf(R.id.inviteRegistrationFragment)

        navController.addOnDestinationChangedListener { _, destination, _ ->
            val shouldShowUpButton = destination.id !in showUpButtonIn
            supportActionBar?.setDisplayHomeAsUpEnabled(shouldShowUpButton)
            supportActionBar?.setHomeButtonEnabled(shouldShowUpButton)

            val shouldHideToolbar = destination.id in hideToolbarIn
            val toolbarHeight = binding.toolbar.height

            if (shouldHideToolbar && binding.toolbar.isVisible) {
                animateToolbarVisibility(show = false, height = toolbarHeight)
            } else if (!shouldHideToolbar && !binding.toolbar.isVisible) {
                animateToolbarVisibility(show = true, height = toolbarHeight)
            }
        }
    }

    private fun setupStartDestination() {
        runBlocking {
            if (viewModel.isUserLoggedIn()) {
                val navInflater = navController.navInflater
                val navGraph = navInflater.inflate(R.navigation.main_nav_graph)
                navGraph.setStartDestination(R.id.dashboardFragment)
                navController.graph = navGraph
            }
        }
    }

    private fun animateToolbarVisibility(show: Boolean, height: Int) {
        val (startValue, endValue, duration) = if (show) {
            Triple(-height, 0, 250)
        } else {
            Triple(0, -height, 350)
        }

        ValueAnimator.ofInt(startValue, endValue).apply {
            this.duration = duration.toLong()
            interpolator = AccelerateInterpolator()

            if (show) {
                doOnStart {
                    binding.toolbar.visibility = View.VISIBLE
                }
            } else {
                doOnEnd {
                    binding.toolbar.visibility = View.INVISIBLE
                }
            }

            addUpdateListener {
                binding.toolbar.apply {
                    val params =
                        layoutParams as? ViewGroup.MarginLayoutParams ?: return@addUpdateListener
                    params.topMargin = it.animatedValue as Int
                    layoutParams = params
                }
            }

            start()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp(appBarConfig) || super.onSupportNavigateUp()
    }

    private fun handleBackPressed() {
        onBackPressedDispatcher.addCallback(this) {
            if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
                binding.drawerLayout.closeDrawers()
            } else {
                isEnabled = false
                onBackPressedDispatcher.onBackPressed()
                isEnabled = true
            }
        }
    }
}