package jp.dev.tanaka.coordinatecalculator.ui

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.appbar.MaterialToolbar
import jp.dev.tanaka.coordinatecalculator.R

class MainActivity : AppCompatActivity() {

    private lateinit var toolbar: MaterialToolbar
    private var navController: androidx.navigation.NavController? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        toolbar.setupWithNavController(navController!!)

        // メニューの表示/非表示を画面ごとに制御
        navController?.addOnDestinationChangedListener { _, destination, _ ->
            invalidateOptionsMenu()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        val currentDestination = navController?.currentDestination?.id
        
        // シナリオ選択画面でのみメニューを表示
        val showMenu = currentDestination == R.id.scenarioSelectFragment
        menu.findItem(R.id.action_history)?.isVisible = showMenu
        menu.findItem(R.id.action_settings)?.isVisible = showMenu
        
        return super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_history -> {
                navController?.navigate(R.id.action_scenario_to_history)
                true
            }
            R.id.action_settings -> {
                navController?.navigate(R.id.action_scenario_to_settings)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
