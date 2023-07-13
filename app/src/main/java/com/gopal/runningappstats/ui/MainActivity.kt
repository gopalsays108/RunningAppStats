package com.gopal.runningappstats.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.fragment.app.FragmentContainerView
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.gopal.runningappstats.R
import com.gopal.runningappstats.databinding.ActivityMainBinding
import com.gopal.runningappstats.db.RunDao
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        val navController = setUpBottomNavigation()

        navController
            .addOnDestinationChangedListener { _, destination, _ ->
                when (destination.id) {
                    R.id.settingsFragment, R.id.runFragment, R.id.statisticsFragment ->
                        binding.bottomNavigationView.visibility = View.VISIBLE

                    else -> binding.bottomNavigationView.visibility = View.GONE
                }
            }
    }

    private fun setUpBottomNavigation(): NavController {
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.navHostFragment) as NavHostFragment
        val navController =navHostFragment.navController
        binding.bottomNavigationView.setupWithNavController(navController)
        return navController
    }
}