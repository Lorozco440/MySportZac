package com.umg.mysportzac


import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.widget.LinearLayout
import android.widget.NumberPicker
import android.widget.Switch
import android.widget.TextView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.facebook.login.LoginManager
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.umg.mysportzac.LoginActivity.Companion.usermail
import com.umg.mysportzac.LoginActivity.Companion.providerSession
import com.umg.mysportzac.R.*
import com.umg.mysportzac.Utility.animateViewofFloat
import com.umg.mysportzac.Utility.animateViewofInt
import com.umg.mysportzac.Utility.getSecFromWatch
import com.umg.mysportzac.Utility.setHeightLinearLayout
import me.tankery.lib.circularseekbar.CircularSeekBar


class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener{
    private lateinit var drawer: DrawerLayout
    private lateinit var swIntervalMode: Switch
    private lateinit var swChallenges: Switch
    private lateinit var swVolumes: Switch

    private lateinit var npChallengeDistance: NumberPicker
    private lateinit var npChallengeDurationHH: NumberPicker
    private lateinit var npChallengeDurationMM: NumberPicker
    private lateinit var npChallengeDurationSS: NumberPicker

    private var challengeDistance: Float = 0f
    private var challengeDuration: Int = 0

    private lateinit var tvChrono: TextView
    private var widthScreenPixels: Int = 0
    private var heightScreenPixels: Int = 0
    private var widthAnimations: Int = 0

    private lateinit var csbChallengeDistance: CircularSeekBar
    private lateinit var csbCurrentDistance: CircularSeekBar
    private lateinit var csbRecordDistance: CircularSeekBar

    private lateinit var csbCurrentAvgSpeed: CircularSeekBar
    private lateinit var csbRecordAvgSpeed: CircularSeekBar

    private lateinit var csbCurrentSpeed: CircularSeekBar
    private lateinit var csbCurrentMaxSpeed: CircularSeekBar
    private lateinit var csbRecordSpeed: CircularSeekBar

    private lateinit var tvDistanceRecord: TextView
    private lateinit var tvAvgSpeedRecord: TextView
    private lateinit var tvMaxSpeedRecord: TextView

    private lateinit var npDurationInterval: NumberPicker
    private lateinit var tvRunningTime: TextView
    private lateinit var tvWalkingTime: TextView
    private lateinit var csbRunWalk: CircularSeekBar

    private var ROUND_INTERVAL = 300
    private var TIME_RUNNING: Int = 0

    private lateinit var lyPopupRun: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(layout.activity_main)

        initObjects()
        initToolBar()
        initNavigationView()

       //Toast.makeText(this, "Hola $usermail", Toast.LENGTH_SHORT).show()
    }


    override fun onBackPressed() {
        if(drawer.isDrawerOpen(GravityCompat.START))
            drawer.closeDrawer(GravityCompat.START)
        else
            signOut()
    }


    private fun initToolBar(){
        val toolbar: Toolbar = findViewById(id.toolbar_main)
        setSupportActionBar(toolbar)

        drawer = findViewById(id.drawer_layout)
        val toggle = ActionBarDrawerToggle(
            this, drawer, toolbar, string.bar_title, string.navigation_drawer_close)

        drawer.addDrawerListener(toggle)

        toggle.syncState()

    }
    private fun  initNavigationView(){
        var navigationView: NavigationView = findViewById(id.nav_view)
        navigationView.setNavigationItemSelectedListener(this)
        var headerView: View = LayoutInflater.from(this).inflate(layout.nav_header_main, navigationView, false)
        navigationView.removeHeaderView(headerView)
        navigationView.addHeaderView(headerView)

        var tvUser: TextView = headerView.findViewById(id.tvUser)
        tvUser.text = usermail
    }
    private fun initStopWatch() {
        tvChrono.text = getString(string.init_stop_watch_value)
    }
    private fun initObjects(){
        tvChrono = findViewById(id.tvChrono)
        tvChrono.setTextColor(ContextCompat.getColor( this, color.white))
        initStopWatch()

        val LyMap = findViewById<LinearLayout>(id.lyMap)
        val LyFragmentMap = findViewById<LinearLayout>(id.lyFragmentMap)
        val lyIntervalModeSpace = findViewById<LinearLayout>(id.lyIntervalModeSpace)
        val lyIntervalMode = findViewById<LinearLayout>(id.lyIntervalMode)
        val lyChallengesSpace = findViewById<LinearLayout>(id.lyChallengesSpace)
        val lyChallenges = findViewById<LinearLayout>(id.lyChallenges)
        val lySettingsVolumesSpace = findViewById<LinearLayout>(id.lySettingsVolumesSpace)
        val lySettingsVolumes = findViewById<LinearLayout>(id.lySettingsVolumes)
        var lySoftTrack = findViewById<LinearLayout>(id.lySoftTrack)
        var lySoftVolume = findViewById<LinearLayout>(id.lySoftVolume)

        setHeightLinearLayout(LyMap, 0)
        setHeightLinearLayout(lyIntervalModeSpace,0)
        setHeightLinearLayout(lyChallengesSpace,0)
        setHeightLinearLayout(lySettingsVolumesSpace,0)
        setHeightLinearLayout(lySoftTrack,0)
        setHeightLinearLayout(lySoftVolume,0)

        LyFragmentMap.translationY = -300f
        lyIntervalMode.translationY = -300f
        lyChallenges.translationY = -300f
        lySettingsVolumes.translationY = -300f

        csbRunWalk = findViewById(id.csbRunWalk)

        swIntervalMode = findViewById(id.swIntervalMode)
        swChallenges = findViewById(id.swChallenges)
        swVolumes = findViewById(id.swVolumes)

        npChallengeDistance = findViewById(id.npChallengeDistance)
        npChallengeDurationHH = findViewById(id.npChallengeDurationHH)
        npChallengeDurationMM = findViewById(id.npChallengeDurationMM)
        npChallengeDurationSS = findViewById(id.npChallengeDurationSS)

        csbRunWalk.setOnSeekBarChangeListener(object :
            CircularSeekBar.OnCircularSeekBarChangeListener {
            override fun onProgressChanged(circularSeekBar: CircularSeekBar,progress: Float,fromUser: Boolean) {
                var STEPS_UX: Int = 15
                var set: Int = 0
                var p = progress.toInt()

                if (p%STEPS_UX != 0){
                    while (p >= 60) p -= 60
                    while (p >= STEPS_UX) p -= STEPS_UX
                    if (STEPS_UX-p > STEPS_UX/2) set = -1 * p
                    else set = STEPS_UX-p

                    csbRunWalk.progress = csbRunWalk.progress + set
                }
            }
            override fun onStopTrackingTouch(seekBar: CircularSeekBar) {
            }
            override fun onStartTrackingTouch(seekBar: CircularSeekBar) {
            }
        })
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
            id.nav_item_record -> callRecordActivity()
            id.nav_item_signout -> signOut()

        }
        drawer.closeDrawer(GravityCompat.START)
        return true
    }
    private fun callRecordActivity(){
        val intent = Intent(this, RecordActivity::class.java)
        startActivity(intent)
    }
    fun inflateIntervalMode(v: View){
        val lyIntervalMode = findViewById<LinearLayout>(id.lyIntervalMode)
        val lyIntervalModeSpace = findViewById<LinearLayout>(id.lyIntervalModeSpace)
        var lySoftTrack = findViewById<LinearLayout>(id.lySoftTrack)
        var lySoftVolume = findViewById<LinearLayout>(id.lySoftVolume)
        var tvRounds = findViewById<TextView>(id.tvRounds)

        if (swIntervalMode.isChecked){
            animateViewofInt(swIntervalMode, "textColor", ContextCompat.getColor(this, color.orange), 500)
            setHeightLinearLayout(lyIntervalModeSpace, 600)
            animateViewofFloat(lyIntervalMode, "translationY", 0f, 500)
            animateViewofFloat (tvChrono, "translationX", -110f, 500)
            tvRounds.setText(string.rounds)
            animateViewofInt(tvRounds, "textColor", ContextCompat.getColor(this, color.white), 500)

            setHeightLinearLayout(lySoftTrack,120)
            setHeightLinearLayout(lySoftVolume,200)
            if (swVolumes.isChecked){
                var lySettingsVolumesSpace = findViewById<LinearLayout>(id.lySettingsVolumesSpace)
                setHeightLinearLayout(lySettingsVolumesSpace,600)
            }
        }
        else{
            swIntervalMode.setTextColor(ContextCompat.getColor(this, color.white))
            setHeightLinearLayout(lyIntervalModeSpace,0)
            lyIntervalMode.translationY = -200f
            animateViewofFloat (tvChrono, "translationX", 0f, 500)
            tvRounds.text = ""
            setHeightLinearLayout(lySoftTrack,0)
            setHeightLinearLayout(lySoftVolume,0)
            if (swVolumes.isChecked){
                var lySettingsVolumesSpace = findViewById<LinearLayout>(id.lySettingsVolumesSpace)
                setHeightLinearLayout(lySettingsVolumesSpace,400)
            }
        }
    }
    fun inflateChallenges(v: View){
        val lyChallengesSpace = findViewById<LinearLayout>(id.lyChallengesSpace)
        val lyChallenges = findViewById<LinearLayout>(id.lyChallenges)

        if (swChallenges.isChecked){
            animateViewofInt(swChallenges, "textColor", ContextCompat.getColor(this, color.orange), 500)
            setHeightLinearLayout(lyChallengesSpace, 750)
            animateViewofFloat(lyChallenges, "translationY", 0f, 500)
        }
        else{
            swChallenges.setTextColor(ContextCompat.getColor(this, color.white))
            setHeightLinearLayout(lyChallengesSpace,0)
            lyChallenges.translationY = -300f

            challengeDistance = 0f
            challengeDuration = 0
        }
    }
    fun showDuration(v: View){
        showChallenge("duration")
    }
    fun showDistance(v:View){
        showChallenge("distance")
    }
    private fun showChallenge(option: String){
        var lyChallengeDuration = findViewById<LinearLayout>(id.lyChallengeDuration)
        var lyChallengeDistance = findViewById<LinearLayout>(id.lyChallengeDistance)
        var tvChallengeDuration = findViewById<TextView>(id.tvChallengeDuration)
        var tvChallengeDistance = findViewById<TextView>(id.tvChallengeDistance)

        when (option){
            "duration" ->{
                lyChallengeDuration.translationZ = 5f
                lyChallengeDistance.translationZ = 0f

                tvChallengeDuration.setTextColor(ContextCompat.getColor(this, color.orange))
                tvChallengeDuration.setBackgroundColor(ContextCompat.getColor(this, color.gray_dark))

                tvChallengeDistance.setTextColor(ContextCompat.getColor(this, color.white))
                tvChallengeDistance.setBackgroundColor(ContextCompat.getColor(this, color.gray_medium))

                challengeDistance = 0f
                getChallengeDuration(npChallengeDurationHH.value, npChallengeDurationMM.value, npChallengeDurationSS.value)
            }
            "distance" -> {
                lyChallengeDuration.translationZ = 0f
                lyChallengeDistance.translationZ = 5f

                tvChallengeDuration.setTextColor(ContextCompat.getColor(this, color.white))
                tvChallengeDuration.setBackgroundColor(ContextCompat.getColor(this, color.gray_medium))

                tvChallengeDistance.setTextColor(ContextCompat.getColor(this, color.orange))
                tvChallengeDistance.setBackgroundColor(ContextCompat.getColor(this, color.gray_dark))

                challengeDuration = 0
                challengeDistance = npChallengeDistance.value.toFloat()
            }
        }
    }
    private fun getChallengeDuration(hh: Int, mm: Int, ss: Int){
        var hours: String = hh.toString()
        if (hh<10) hours = "0"+hours
        var minutes: String = mm.toString()
        if (mm<10) minutes = "0"+minutes
        var seconds: String = ss.toString()
        if (ss<10) seconds = "0"+seconds

        challengeDuration = getSecFromWatch("${hours}:${minutes}:${seconds}")
    }
    fun inflateVolumnes(view: View) {
        val lySettingsVolumesSpace = findViewById<LinearLayout>(id.lySettingsVolumesSpace)
        val lySettingsVolumes = findViewById<LinearLayout>(id.lySettingsVolumes)

        if (swVolumes.isChecked){
            animateViewofInt(swVolumes, "textColor", ContextCompat.getColor(this, color.orange), 500)
            var value = 400
            if (swIntervalMode.isChecked) value = 600

            setHeightLinearLayout(lySettingsVolumesSpace, value)
            animateViewofFloat(lySettingsVolumes, "translationY", 0f, 500)
        }
        else{
            swVolumes.setTextColor(ContextCompat.getColor(this, color.white))
            setHeightLinearLayout(lySettingsVolumesSpace,0)
            lySettingsVolumes.translationY = -300f
        }
    }
}
























