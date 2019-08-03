package com.example.nittcompanion.common

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.nittcompanion.MainNavigationDirections
import com.example.nittcompanion.R
import com.example.nittcompanion.common.factoryAndInjector.InjectorUtils
import com.example.nittcompanion.login.LoginActivity
import com.google.android.material.navigation.NavigationView
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.nav_head.view.*

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {


    private lateinit var navigationController: NavController
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var viewModel: BaseViewModel
    private var eventFromNotiId : String? = null
    private var courseFromNotiId : String? = null
    private var isClassFromNoti = false
    private lateinit var loadSnack : Snackbar
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(Toolbar)

        appBarConfiguration = AppBarConfiguration.Builder(
            setOf(
                R.id.destination_home,
                R.id.destination_calender,
                R.id.destination_courses
            )
        )
            .setDrawerLayout(DrawerLayout)
            .build()

        navigationController = Navigation.findNavController(this, R.id.HostFragment)

        NavigationUI.setupWithNavController(NavigationMain, navigationController)

        NavigationUI.setupWithNavController(Toolbar, navigationController, appBarConfiguration)

        NavigationMain.setNavigationItemSelectedListener(this)

        viewModel = ViewModelProviders.of(this, InjectorUtils(application).provideBaseViewModelFactory())
            .get(BaseViewModel::class.java)

        val mintent = intent
        if(mintent.hasExtra(KEY_EVENT_ID) and mintent.hasExtra(KEY_IS_CLASS)) {
            Log.e("MainActivity","getting from intent")
            eventFromNotiId = mintent.getStringExtra(KEY_EVENT_ID)
            courseFromNotiId = mintent.getStringExtra(KEY_COURSE_ID)
            isClassFromNoti = mintent.getBooleanExtra(KEY_IS_CLASS,false)
            Log.e("MAINActivity","id from notification $eventFromNotiId")
        } else eventFromNotiId = null
    }

    override fun onStart() {
        super.onStart()
        loadSnack = Snackbar.make(this.findViewById(android.R.id.content),"Loading ...",Snackbar.LENGTH_INDEFINITE)
        viewModel.listen(ListenTo.ActivityStarted)
        val user = when (val result = getCurrentUser()) {
            is Result.Value -> result.value
            is Result.Error -> {
                this.createSnackbar("Error verifying user", Snackbar.LENGTH_LONG)
                null
            }
        }
        val name = user?.name
        val email = user?.email
        val headderView = NavigationMain.getHeaderView(0)
        Glide.with(this)
            .load(user?.dispPicUrl)
            .apply(RequestOptions.circleCropTransform())
            .into(headderView.ProfilePic)
        headderView.UserName.text = resources.getString(R.string.hi_name, name ?: "NITTian")
        headderView.UserEmail.text = email ?: ""
        headderView.LogOutBtn.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Confirm Logout")
                .setMessage("Do you really want to logout")
                .setPositiveButton("Cancel") { _, _ ->

                }
                .setNegativeButton("Yes") { _, _ ->
                    logout()
                }
                .create()
                .show()
        }
        setObservations()
        Log.e("MAINActivity","id from notification $eventFromNotiId")
        eventFromNotiId?.let {eventid ->
            viewModel.listen(ListenTo.NotificationTappedEvent(eventid))
            if(isClassFromNoti){
                courseFromNotiId?.let { courseid ->
                    viewModel.listen(ListenTo.NotificationTappedCourse(courseid))
                }
            }
            val direction = MainNavigationDirections.actionMainToDestinationEventDetail(true)
            findNavController(R.id.HostFragment).navigate(direction)
        }
    }

    private fun setObservations() {
        viewModel.error.observe(
            this,
            Observer {
                createSnackbar(it, Snackbar.LENGTH_LONG)
            }
        )
        viewModel.loading.observe(
            this,
            Observer {
                if(it)
                    loadSnack.show()
                else
                    loadSnack.dismiss()
            }
        )
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
       /* menuInflater.inflate(R.menu.nav_toolbar, menu)
        val drawable = menu?.getItem(0)?.icon
        drawable?.let {
            it.mutate()
            it.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP)
        }*/
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        //val navigated = NavigationUI.onNavDestinationSelected(item!!,navigationController)
        super.onOptionsItemSelected(item)
        /*if (item?.itemId == R.id.destination_alarm) {
            navigationController.navigate(R.id.destination_alarm)
            return true
        }*/
        return false
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        Log.d("Main Activity", "navigation item selected")
        when (item.itemId) {
            R.id.destination_home -> findNavController(R.id.HostFragment).navigate(R.id.destination_home)
            R.id.destination_calender -> findNavController(R.id.HostFragment).navigate(R.id.destination_calender)
            R.id.destination_courses -> findNavController(R.id.HostFragment).navigate(R.id.destination_courses)
            R.id.website -> startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://www.nitt.edu/")))
            R.id.forms -> startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("https://www.nitt.edu/home/academics/formats/")
                )
            )
            R.id.webmail -> startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("https://webmail.nitt.edu/hordenew/login.php/")
                )
            )
            R.id.ruby -> startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("http://ruby.nitt.edu/")))
            R.id.mis -> startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("http://misnew.nitt.edu/NITTSTUDENT")))
        }
        DrawerLayout.closeDrawer(GravityCompat.START,true)
        return true
    }

    private fun logout() {
        logOutUser()
        val intent = Intent(this, LoginActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NO_HISTORY)
        startActivity(intent)
        finishAffinity()
    }
}
