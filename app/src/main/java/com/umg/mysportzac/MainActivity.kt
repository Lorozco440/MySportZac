package com.umg.mysportzac

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.facebook.login.LoginManager
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.umg.mysportzac.LoginActivity.Companion.usermail
import com.umg.mysportzac.LoginActivity.Companion.providerSession
import com.umg.mysportzac.R.*

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener{
    private lateinit var drawer: DrawerLayout
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(layout.activity_main)

        initToolBar()
        initNavigationView()

       // Toast.makeText(this, "Hola $usermail", Toast.LENGTH_SHORT).show()
    }

    private fun initToolBar(){
        val toolbar: androidx.appcompat.widget.Toolbar = findViewById(id.toolbar_main)
        setSupportActionBar(toolbar)

        drawer = findViewById(id.drawer_layout)
        val toggle = ActionBarDrawerToggle(
            this, drawer, toolbar, R.string.bar_title, R.string.navigation_drawer_close)

        drawer.addDrawerListener(toggle)

        toggle.syncState()

    }

    private fun  initNavigationView(){
        var navigationView: NavigationView = findViewById(R.id.nav_view)
        navigationView.setNavigationItemSelectedListener(this)
        var headerView: View = LayoutInflater.from(this).inflate(R.layout.nav_header_main, navigationView, false)
        navigationView.removeHeaderView(headerView)
        navigationView.addHeaderView(headerView)

        var tvUser: TextView = headerView.findViewById(R.id.tvUser)
        tvUser.text = usermail
    }


    fun callSignOut(view: View) {
        signOut()
    }
    private fun signOut(){
        usermail = " "

        if(providerSession == "Facebook") LoginManager.getInstance().logOut()
        FirebaseAuth.getInstance().signOut()
        startActivity(Intent(this, LoginActivity::class.java))

    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {

        when (item.itemId){
            R.id.nav_item_record -> callRecordActivity()
            R.id.nav_item_signout -> signOut()

        }
        drawer.closeDrawer(GravityCompat.START)
        return true
    }
    private fun callRecordActivity(){
        val intent = Intent(this, RecordActivity::class.java)
        startActivity(intent)
    }
}
























