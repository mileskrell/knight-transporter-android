package edu.rutgers.knighttransporter

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.setupActionBarWithNavController
import com.mapbox.android.core.permissions.PermissionsManager
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(R.layout.activity_main) {

    lateinit var navController: NavController

    lateinit var permissionsManager: PermissionsManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setSupportActionBar(toolbar)

        navController = findNavController(R.id.nav_host_fragment)
        setupActionBarWithNavController(navController)
    }

    override fun onSupportNavigateUp() = navController.navigateUp() || super.onSupportNavigateUp()

    override fun onBackPressed() {
        if (!simple_search_view.onBackPressed()) {
            super.onBackPressed()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == 0) { // PermissionsManager.REQUEST_PERMISSIONS_CODE
            permissionsManager.onRequestPermissionsResult(requestCode, permissions, grantResults)
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }
}
