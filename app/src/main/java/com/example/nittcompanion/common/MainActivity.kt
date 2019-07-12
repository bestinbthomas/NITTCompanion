package com.example.nittcompanion.common

import android.graphics.Color
import android.graphics.PorterDuff
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import com.example.nittcompanion.R
import com.google.android.material.navigation.NavigationView
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {


    private lateinit var navigationController :NavController
    private lateinit var appBarConfiguration: AppBarConfiguration

    override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(Toolbar)

        appBarConfiguration = AppBarConfiguration.Builder(setOf(R.id.destination_home,R.id.destination_calender,R.id.destination_courses))
            .setDrawerLayout(DrawerLayout)
            .build()

        navigationController = Navigation.findNavController(this,R.id.HostFragment)

        NavigationUI.setupWithNavController(NavigationMain, navigationController)

        NavigationUI.setupWithNavController(Toolbar,navigationController,appBarConfiguration)

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.nav_toolbar,menu)
        val drawable = menu?.getItem(0)?.icon
        drawable?.let {
            it.mutate()
            it.setColorFilter(Color.WHITE,PorterDuff.Mode.SRC_ATOP)
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        //val navigated = NavigationUI.onNavDestinationSelected(item!!,navigationController)
        super.onOptionsItemSelected(item)
        if (item?.itemId == R.id.destination_alarm) {
            navigationController.navigate(R.id.destination_alarm)
            return true
        }
        return false
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        return true
    }
}
