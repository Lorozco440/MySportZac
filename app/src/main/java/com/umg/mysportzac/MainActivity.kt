package com.umg.mysportzac

import android.Manifest
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.ContentValues
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.media.MediaPlayer
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.NumberPicker
import android.widget.RelativeLayout
import android.widget.SeekBar
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.core.view.isVisible
import androidx.drawerlayout.widget.DrawerLayout
import com.facebook.login.LoginManager
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationRequest.PRIORITY_HIGH_ACCURACY
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.gms.maps.model.RoundCap
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.navigation.NavigationView
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.umg.mysportzac.Constants.INTERVAL_LOCATION
import com.umg.mysportzac.Constants.LIMIT_DISTANCE_ACCEPTED_BIKE
import com.umg.mysportzac.Constants.LIMIT_DISTANCE_ACCEPTED_ROLLERSKATE
import com.umg.mysportzac.Constants.LIMIT_DISTANCE_ACCEPTED_RUNNING
import com.umg.mysportzac.Constants.key_challengeAutofinish
import com.umg.mysportzac.Constants.key_challengeDistance
import com.umg.mysportzac.Constants.key_challengeDurationHH
import com.umg.mysportzac.Constants.key_challengeDurationMM
import com.umg.mysportzac.Constants.key_challengeDurationSS
import com.umg.mysportzac.Constants.key_challengeNofify
import com.umg.mysportzac.Constants.key_hardVol
import com.umg.mysportzac.Constants.key_intervalDuration
import com.umg.mysportzac.Constants.key_maxCircularSeekBar
import com.umg.mysportzac.Constants.key_modeChallenge
import com.umg.mysportzac.Constants.key_modeChallengeDistance
import com.umg.mysportzac.Constants.key_modeChallengeDuration
import com.umg.mysportzac.Constants.key_modeInterval
import com.umg.mysportzac.Constants.key_notifyVol
import com.umg.mysportzac.Constants.key_progressCircularSeekBar
import com.umg.mysportzac.Constants.key_provider
import com.umg.mysportzac.Constants.key_runningTime
import com.umg.mysportzac.Constants.key_selectedSport
import com.umg.mysportzac.Constants.key_softVol
import com.umg.mysportzac.Constants.key_userApp
import com.umg.mysportzac.Constants.key_walkingTime
import com.umg.mysportzac.LoginActivity.Companion.usermail
import com.umg.mysportzac.LoginActivity.Companion.providerSession
import com.umg.mysportzac.R.*
import com.umg.mysportzac.Utility.animateViewofFloat
import com.umg.mysportzac.Utility.animateViewofInt
import com.umg.mysportzac.Utility.deleteRunAndLinkedData
import com.umg.mysportzac.Utility.getFormattedStopWatch
import com.umg.mysportzac.Utility.getFormattedTotalTime
import com.umg.mysportzac.Utility.getSecFromWatch
import com.umg.mysportzac.Utility.roundNumber
import com.umg.mysportzac.Utility.setHeightLinearLayout
import me.tankery.lib.circularseekbar.CircularSeekBar
import java.text.SimpleDateFormat
import java.util.Date


@SuppressLint("UseSwitchCompatOrMaterialCode")
class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener, OnMapReadyCallback,
    GoogleMap.OnMyLocationButtonClickListener, GoogleMap.OnMyLocationClickListener{

    companion object{
        lateinit var mainContext: Context

        var activatedGPS: Boolean = true

        lateinit var totalsSelectedSport: Totals
        lateinit var totalsBike: Totals
        lateinit var totalsRollerSkate: Totals
        lateinit var totalsRunning: Totals

        val REQUIRED_PERMISSIONS_GPS =
            arrayOf(
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION)
    }
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var editor: SharedPreferences.Editor

    private var mHandler: Handler? = null
    private var mInterval = 1000
    private var timeInSeconds = 0L
    private var rounds: Int = 1
    private var startButtonClicked = false

    private lateinit var drawer: DrawerLayout

    private lateinit var fbCamara: FloatingActionButton

    private lateinit var cbNotify: CheckBox
    private lateinit var cbAutoFinish: CheckBox

    private lateinit var swIntervalMode: Switch
    private lateinit var swChallenges: Switch
    private lateinit var swVolumes: Switch
    private var mpNotify : MediaPlayer? = null
    private var mpHard : MediaPlayer? = null
    private var mpSoft : MediaPlayer? = null
    private lateinit var sbHardVolume : SeekBar
    private lateinit var sbSoftVolume : SeekBar
    private lateinit var sbNotifyVolume : SeekBar

    private lateinit var sbHardTrack : SeekBar
    private lateinit var sbSoftTrack : SeekBar

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
    private var hardTime : Boolean = true
    private var TIME_RUNNING: Int = 0

    private var LIMIT_DISTANCE_ACCEPTED: Double = 0.0
    private lateinit var sportSelected : String

    private lateinit var lyPopupRun: LinearLayout

    private lateinit var map: GoogleMap
    private var mapCentered = true
    private lateinit var listPoints: Iterable<LatLng>


    private val PERMISSION_ID = 42
    private val LOCATION_PERMISSION_REQ_CODE = 1000

    //private var activatedGPS: Boolean = true
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var flagSavedLocation = false

    private var latitude: Double = 0.0
    private var longitude: Double = 0.0
    private var init_lt: Double = 0.0
    private var init_ln: Double = 0.0

    private var distance: Double = 0.0
    private var maxSpeed: Double = 0.0
    private var avgSpeed: Double = 0.0
    private var speed: Double = 0.0

    private var minAltitude: Double? = null
    private var maxAltitude: Double? = null
    private var minLatitude: Double? = null
    private var maxLatitude: Double? = null
    private var minLongitude: Double? = null
    private var maxLongitude: Double? = null

    private lateinit var levelBike: Level
    private lateinit var levelRollerSkate: Level
    private lateinit var levelRunning: Level
    private lateinit var levelSelectedSport: Level

    private lateinit var levelsListBike: ArrayList<Level>
    private lateinit var levelsListRollerSkate: ArrayList<Level>
    private lateinit var levelsListRunning: ArrayList<Level>

    private var sportsLoaded: Int = 0

    private lateinit var dateRun: String
    private lateinit var startTimeRun: String

    private lateinit var medalsListBikeDistance: ArrayList<Double>
    private lateinit var medalsListBikeAvgSpeed: ArrayList<Double>
    private lateinit var medalsListBikeMaxSpeed: ArrayList<Double>

    private lateinit var medalsListRollerSkateDistance: ArrayList<Double>
    private lateinit var medalsListRollerSkateAvgSpeed: ArrayList<Double>
    private lateinit var medalsListRollerSkateMaxSpeed: ArrayList<Double>

    private lateinit var medalsListRunningDistance: ArrayList<Double>
    private lateinit var medalsListRunningAvgSpeed: ArrayList<Double>
    private lateinit var medalsListRunningMaxSpeed: ArrayList<Double>

    private lateinit var medalsListSportSelectedDistance: ArrayList<Double>
    private lateinit var medalsListSportSelectedAvgSpeed: ArrayList<Double>
    private lateinit var medalsListSportSelectedMaxSpeed: ArrayList<Double>

    private var recDistanceGold: Boolean = false
    private var recDistanceSilver: Boolean = false
    private var recDistanceBronze: Boolean = false
    private var recAvgSpeedGold: Boolean = false
    private var recAvgSpeedSilver: Boolean = false
    private var recAvgSpeedBronze: Boolean = false
    private var recMaxSpeedGold: Boolean = false
    private var recMaxSpeedSilver: Boolean = false
    private var recMaxSpeedBronze: Boolean = false

    private lateinit var lyAlert: LinearLayout
    private lateinit var lyAlertDialogBox: LinearLayout
    private lateinit var tvAlertTitle: TextView
    private lateinit var tvAlertDialog: TextView
    private lateinit var btnAlertLeft: Button
    private lateinit var btnAlertRight: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(layout.activity_main)
        mainContext = this

        initObjects()

        initToolBar()
        initNavigationView()
        initPermissionsGPS()


        loadFromDB()

        //Toast.makeText(this, "Hola $usermail", Toast.LENGTH_SHORT).show()
    }
    @SuppressLint("MissingSuperCall")
    override fun onBackPressed() {

        if (lyPopupRun.isVisible) closePopUpRun()
        else {
            if (drawer.isDrawerOpen(GravityCompat.START))
                drawer.closeDrawer(GravityCompat.START)
            else
                if (timeInSeconds > 0L) resetClicked()
            alertSignOut()
        }
    }
    private fun alertClearPreferences(){
        sendMyAlertDefault(
            getString(string.alertClearPreferencesTitle),
            getString(string.alertClearPreferencesDescription),
            getString(string.clearPreferencesBtnLeft),
            getString(string.clearPreferencesBtnRight),
            getString(string.snackBarClearPreferences))
        {callClearPreferences()} // Función como parámetro final.

    }
    private fun alertSignOut(){
        sendMyAlertDefault(
            getString(string.alertSignOutTitle),
            getString(string.alertSignOutTDescription),
            getString(string.signOutBtnLeft),
            getString(string.signOutBtnRight),
            getString(string.snackBarSignOut))
        {signOut()} // Función como parámetro final.
    }
    private fun manageStartStop(){
        if (LIMIT_DISTANCE_ACCEPTED == 0.0)
            //Toast(getString(R.string.sportSelectMsg))
        else
            if (timeInSeconds == 0L && islocationEnabled() == false){
                sendMyAlertDefault(
                    getString(string.alertActivationGPSTitle),
                    getString(string.alertActivationGPSDescription),
                    getString(string.ignoreActivationGPS),
                    getString(string.acceptActivationGPS),
                    getString(string.snackBarDeleteRun))
                {activationLocation()} // Función como parámetro final.
            }
            else manageRun()
    }
    fun calldeleteRun(v:View){
        alertDeleteRun()
    }
    private fun alertDeleteRun(){
        sendMyAlertDefault(
            getString(string.alertDeleteRunTitle),
            getString(string.alertDeleteRunDescription),
            getString(string.deleteRunBtnLeft),
            getString(string.deleteRunBtnRight),
            getString(string.snackBarDeleteRun))
        {DeleteRun()} // Función como parámetro final.
    }
    private fun DeleteRun(){

        var id:String = usermail + dateRun + startTimeRun
        id = id.replace(":", "")
        id = id.replace("/", "")
        var lyPopUpRun = findViewById<LinearLayout>(R.id.lyPopupRun)
        var currentRun = Runs()
        currentRun.distance = roundNumber(distance.toString(),1).toDouble()
        currentRun.avgSpeed = roundNumber(avgSpeed.toString(),1).toDouble()
        currentRun.maxSpeed = roundNumber(maxSpeed.toString(),1).toDouble()
        currentRun.duration = tvChrono.text.toString()
        deleteRunAndLinkedData(id, sportSelected, lyPopUpRun, currentRun)
        loadMedalsUser()
        setLevelSport(sportSelected)
        closePopUpRun()
    }
    // Para ahorrar trabajo y manejar un estandar.
    private fun sendMyAlertDefault(

        question: String,
        dialog: String,
        txtBtnLeft: String,
        txtBtnRight: String,
        snackBarMsgWhenCallFn: String? = null,
        callAlertFunction: () -> Unit){

        sendAlert(
            // Color con transparencia para el fondo del LienearLayout principal.
            ContextCompat.getColor(this, color.black_trans),
            question,
            dialog,
            txtBtnLeft,
            // Color de fondo del botón izquierdo.
            ContextCompat.getColor(this, color.blue),
            txtBtnRight,
            // Color de fondo del botón derecho.
            ContextCompat.getColor(this, color.salmon),
            true,
            /* Los siguientes 2 parámetros son opcionales:
               Fondo de SnackBar para mensaje de respuesta.*/
            ContextCompat.getColor(this, color.salmon_dark),
            /* Mensaje de respuesta. */
            snackBarMsgWhenCallFn
        )
        // El siguiente parámetro es la función a ejecutar si el usuario autoriza.
        // Debe ser una función existente con sus correspondientes parámetros.
        { callAlertFunction() }
    }

    private fun sendAlert(

        bgColor: Int,
        question: String,
        dialog: String,
        txtBtnLeft: String,
        bgColorBtnLeft: Int,
        txtBtnRight: String,
        bgColorBtnRight: Int,
        areButtonsFalseAndTrue: Boolean,
        bgColorSnackBar: Int? = null,
        snackBarMsgWhenCallFn: String? = null,

        callAlertFunction: () -> Unit){
        lyAlert.setBackgroundColor(bgColor)
        tvAlertTitle.text = question
        tvAlertDialog.text = dialog
        btnAlertLeft.text = txtBtnLeft
        btnAlertLeft.setBackgroundColor(bgColorBtnLeft)
        btnAlertRight.text = txtBtnRight
        btnAlertRight.setBackgroundColor(bgColorBtnRight)

        btnAlertLeft.setOnClickListener {
            hideAlert()
            if(!areButtonsFalseAndTrue){
                if(snackBarMsgWhenCallFn != null)
                    showSnackBar(snackBarMsgWhenCallFn, Snackbar.LENGTH_SHORT, bgColorSnackBar)
                callAlertFunction()
            }
        }
        btnAlertRight.setOnClickListener {

            hideAlert()
            if(areButtonsFalseAndTrue){
                if(snackBarMsgWhenCallFn != null)
                    showSnackBar(snackBarMsgWhenCallFn, Snackbar.LENGTH_SHORT, bgColorSnackBar)
                callAlertFunction()
            }
        }
        showAlert()

    }
    private fun showAlert(){

        var rlMain = findViewById<RelativeLayout>(id.rlMain)
        rlMain.isEnabled = false
        lyAlert.visibility = View.VISIBLE
        ObjectAnimator.ofFloat(lyAlertDialogBox, "translationX", 0f ).apply {
            duration = 200L
            start()

        }

    }
    private fun hideAlert(){

        var rlMain = findViewById<RelativeLayout>(id.rlMain)

        lyAlert = findViewById(id.lyAlert)
        lyAlertDialogBox = findViewById(id.lyAlertDialogBox)
        lyAlertDialogBox = findViewById(id.lyAlertDialogBox)
        tvAlertTitle = findViewById(id.tvAlertTitle)
        tvAlertDialog = findViewById(id.tvAlertDialog)
        btnAlertLeft = findViewById(id.btnAlertLeft)
        btnAlertRight = findViewById(id.btnAlertRight)
        lyAlert.visibility = View.INVISIBLE
        lyAlertDialogBox.translationX = 400f
        rlMain.isEnabled = true

    }
    private fun showSnackBar(message: String, duration: Int, bgColor: Int? = null) {
        val mySnackbar = Snackbar.make(findViewById(id.drawer_layout), message, duration)
        if(bgColor != null)
            mySnackbar.setBackgroundTint(bgColor)
        mySnackbar.show()
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
        val navigationView: NavigationView = findViewById(id.nav_view)
        navigationView.setNavigationItemSelectedListener(this)
        val headerView: View = LayoutInflater.from(this).inflate(layout.nav_header_main, navigationView, false)
        navigationView.removeHeaderView(headerView)
        navigationView.addHeaderView(headerView)

        val tvUser: TextView = headerView.findViewById(id.tvUser)
        tvUser.text = usermail
    }
    private fun initStopWatch() {
        tvChrono.text = getString(string.init_stop_watch_value)
    }
    private fun  initChrono(){
        tvChrono = findViewById(id.tvChrono)
        tvChrono.setTextColor(ContextCompat.getColor( this, color.white))
        initStopWatch()

        widthScreenPixels = resources.displayMetrics.widthPixels
        heightScreenPixels = resources.displayMetrics.heightPixels

        widthAnimations = widthScreenPixels

        val lyChronoProgressBg = findViewById<LinearLayout>(id.lyChronoProgressBg)
        val lyRoundProgressBg = findViewById<LinearLayout>(id.lyRoundProgressBg)
        lyChronoProgressBg.translationX = -widthAnimations.toFloat()
        lyRoundProgressBg.translationX = -widthAnimations.toFloat()

        val tvReset : TextView = findViewById(id.tvReset)
        tvReset.setOnClickListener { resetClicked() }

        fbCamara = findViewById(id.fbCamera)
        fbCamara.isVisible = false
    }
    private fun hideLayouts(){
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
    }
    private fun initMetrics(){
        csbCurrentDistance = findViewById(id.csbCurrentDistance)
        csbChallengeDistance = findViewById(id.csbChallengeDistance)
        csbRecordDistance = findViewById(id.csbRecordDistance)

        csbCurrentAvgSpeed = findViewById(id.csbCurrentAvgSpeed)
        csbRecordAvgSpeed = findViewById(id.csbRecordAvgSpeed)

        csbCurrentSpeed = findViewById(id.csbCurrentSpeed)
        csbCurrentMaxSpeed = findViewById(id.csbCurrentMaxSpeed)
        csbRecordSpeed = findViewById(id.csbRecordSpeed)

        csbCurrentDistance.progress = 0f
        csbChallengeDistance.progress = 0f

        csbCurrentAvgSpeed.progress = 0f

        csbCurrentSpeed.progress = 0f
        csbCurrentMaxSpeed.progress = 0f

        tvDistanceRecord = findViewById(id.tvDistanceRecord)
        tvAvgSpeedRecord = findViewById(id.tvAvgSpeedRecord)
        tvMaxSpeedRecord = findViewById(id.tvMaxSpeedRecord)

        tvDistanceRecord.text = ""
        tvAvgSpeedRecord.text = ""
        tvMaxSpeedRecord.text = ""
    }
    private fun initSwitchs(){
        swIntervalMode = findViewById(id.swIntervalMode)
        swChallenges = findViewById(id.swChallenges)
        swVolumes = findViewById(id.swVolumes)

    }
    private fun initIntervalMode(){
        npDurationInterval = findViewById(id.npDurationInterval)
        tvRunningTime = findViewById(id.tvRunningTime)
        tvWalkingTime = findViewById(id.tvWalkingTime)
        csbRunWalk = findViewById(id.csbRunWalk)

        npDurationInterval.minValue = 1
        npDurationInterval.maxValue = 60
        npDurationInterval.value = 5
        npDurationInterval.wrapSelectorWheel = true
        npDurationInterval.setFormatter(NumberPicker.Formatter { i -> String.format("%02d", i) })

        npDurationInterval.setOnValueChangedListener { picker, oldVal, newVal ->
            csbRunWalk.max = (newVal * 60).toFloat()
            csbRunWalk.progress = csbRunWalk.max / 2

            tvRunningTime.text = getFormattedStopWatch(((newVal * 60 / 2) * 1000).toLong()).subSequence(3, 8)
            tvWalkingTime.text = tvRunningTime.text

            ROUND_INTERVAL = newVal * 60
            TIME_RUNNING = ROUND_INTERVAL / 2
        }

        csbRunWalk.max = 300f
        csbRunWalk.progress = 150f
        csbRunWalk.setOnSeekBarChangeListener(object :CircularSeekBar.OnCircularSeekBarChangeListener {
            override fun onProgressChanged(circularSeekBar: CircularSeekBar,progress: Float,fromUser: Boolean) {

                if (fromUser){
                    var STEPS_UX: Int = 15
                    if (ROUND_INTERVAL > 600) STEPS_UX = 60
                    if (ROUND_INTERVAL > 1800) STEPS_UX = 300
                    var set: Int = 0
                    var p = progress.toInt()

                    var limit = 60
                    if (ROUND_INTERVAL > 1800) limit = 300

                    if (p%STEPS_UX != 0 && progress != csbRunWalk.max){
                        while (p >= limit) p -= limit
                        while (p >= STEPS_UX) p -= STEPS_UX
                        if (STEPS_UX-p > STEPS_UX/2) set = -1 * p
                        else set = STEPS_UX-p

                        if (csbRunWalk.progress + set > csbRunWalk.max)
                            csbRunWalk.progress = csbRunWalk.max
                        else
                            csbRunWalk.progress = csbRunWalk.progress + set
                    }
                }
                if (csbRunWalk.progress == 0f) manageEnableButtonsRun(false, false)
                else manageEnableButtonsRun(false, true)

                tvRunningTime.text = getFormattedStopWatch((csbRunWalk.progress.toInt() *1000).toLong()).subSequence(3,8)
                tvWalkingTime.text = getFormattedStopWatch(((ROUND_INTERVAL- csbRunWalk.progress.toInt())*1000).toLong()).subSequence(3,8)
                TIME_RUNNING = getSecFromWatch(tvRunningTime.text.toString())

            }
            override fun onStopTrackingTouch(seekBar: CircularSeekBar) {
            }
            override fun onStartTrackingTouch(seekBar: CircularSeekBar) {
            }
        })
    }
    private fun initChallengeMode(){
        npChallengeDistance = findViewById(id.npChallengeDistance)
        npChallengeDurationHH = findViewById(id.npChallengeDurationHH)
        npChallengeDurationMM = findViewById(id.npChallengeDurationMM)
        npChallengeDurationSS = findViewById(id.npChallengeDurationSS)

        npChallengeDistance.minValue = 1
        npChallengeDistance.maxValue = 300
        npChallengeDistance.value = 10
        npChallengeDistance.wrapSelectorWheel = true


        npChallengeDistance.setOnValueChangedListener { picker, oldVal, newVal ->
            challengeDistance = newVal.toFloat()
            csbChallengeDistance.max = newVal.toFloat()
            csbChallengeDistance.progress = newVal.toFloat()
            challengeDuration = 0

            if (csbChallengeDistance.max > csbRecordDistance.max)
                csbCurrentDistance.max = csbChallengeDistance.max
        }

        npChallengeDurationHH.minValue = 0
        npChallengeDurationHH.maxValue = 23
        npChallengeDurationHH.value = 1
        npChallengeDurationHH.wrapSelectorWheel = true
        npChallengeDurationHH.setFormatter(NumberPicker.Formatter { i -> String.format("%02d", i) })

        npChallengeDurationMM.minValue = 0
        npChallengeDurationMM.maxValue = 59
        npChallengeDurationMM.value = 0
        npChallengeDurationMM.wrapSelectorWheel = true
        npChallengeDurationMM.setFormatter(NumberPicker.Formatter { i -> String.format("%02d", i) })

        npChallengeDurationSS.minValue = 0
        npChallengeDurationSS.maxValue = 59
        npChallengeDurationSS.value = 0
        npChallengeDurationSS.wrapSelectorWheel = true
        npChallengeDurationSS.setFormatter(NumberPicker.Formatter { i -> String.format("%02d", i) })

        npChallengeDurationHH.setOnValueChangedListener { picker, oldVal, newVal ->
            getChallengeDuration(newVal, npChallengeDurationMM.value, npChallengeDurationSS.value)
        }
        npChallengeDurationMM.setOnValueChangedListener { picker, oldVal, newVal ->
            getChallengeDuration(npChallengeDurationHH.value, newVal, npChallengeDurationSS.value)
        }
        npChallengeDurationSS.setOnValueChangedListener { picker, oldVal, newVal ->
            getChallengeDuration(npChallengeDurationHH.value, npChallengeDurationMM.value, newVal)
        }
        cbNotify = findViewById<CheckBox>(id.cbNotify)
        cbAutoFinish = findViewById<CheckBox>(id.cbAutoFinish)
    }
    private fun setVolumes() {
        sbHardVolume.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(p0: SeekBar?, i: Int, p2: Boolean) {
                mpHard?.setVolume(i / 100.0f, i / 100.0f)
            }

            override fun onStartTrackingTouch(p0: SeekBar?) {}
            override fun onStopTrackingTouch(p0: SeekBar?) {}
        })
        sbSoftVolume.setOnSeekBarChangeListener(object: SeekBar.OnSeekBarChangeListener{
            override fun onProgressChanged(p0: SeekBar?, i: Int, p2: Boolean) {
                mpSoft?.setVolume(i/100.0f, i/100.0f)
            }
            override fun onStartTrackingTouch(p0: SeekBar?) { }
            override fun onStopTrackingTouch(p0: SeekBar?) { }
        })
        sbNotifyVolume.setOnSeekBarChangeListener(object: SeekBar.OnSeekBarChangeListener{
            override fun onProgressChanged(p0: SeekBar?, i: Int, p2: Boolean) {
                mpNotify?.setVolume(i/100.0f, i/100.0f)
            }
            override fun onStartTrackingTouch(p0: SeekBar?) { }
            override fun onStopTrackingTouch(p0: SeekBar?) { }
        })
    }
    private fun updateTimesTrack(timesH: Boolean, timesS: Boolean){

        if (timesH){
            val tvHardPosition = findViewById<TextView>(id.tvHardPosition)
            val tvHardRemaining = findViewById<TextView>(id.tvHardRemaining)
            tvHardPosition.text = getFormattedStopWatch(mpHard!!.currentPosition.toLong())
            tvHardRemaining.text = "-" + getFormattedStopWatch( mpHard!!.duration.toLong() - sbHardTrack.progress.toLong())
        }
        if (timesS){
            val tvSoftPosition = findViewById<TextView>(id.tvSoftPosition)
            val tvSoftRemaining = findViewById<TextView>(id.tvSoftRemaining)
            tvSoftPosition.text = getFormattedStopWatch(mpSoft!!.currentPosition.toLong())
            tvSoftRemaining.text = "-" + getFormattedStopWatch( mpSoft!!.duration.toLong() - sbSoftTrack.progress.toLong())
        }
    }
    private fun setProgressTracks(){

        sbHardTrack.max = mpHard!!.duration
        sbSoftTrack.max = mpSoft!!.duration
        sbHardTrack.isEnabled = false
        sbSoftTrack.isEnabled = false
        updateTimesTrack(true, true)

        sbHardTrack.setOnSeekBarChangeListener(object: SeekBar.OnSeekBarChangeListener{
            override fun onProgressChanged(p0: SeekBar?, i: Int, fromUser: Boolean) {
                if (fromUser){
                    mpHard?.pause()
                    mpHard?.seekTo(i)
                    mpHard?.start()
                    if (!(timeInSeconds > 0L && hardTime && startButtonClicked)) mpHard?.pause()
                    updateTimesTrack(true, false)
                }
            }
            override fun onStartTrackingTouch(p0: SeekBar?) {}
            override fun onStopTrackingTouch(p0: SeekBar?) {}
        })

        sbSoftTrack.setOnSeekBarChangeListener(object: SeekBar.OnSeekBarChangeListener{
            override fun onProgressChanged(p0: SeekBar?, i: Int, fromUser: Boolean) {
                if (fromUser){
                    mpSoft?.pause()
                    mpSoft?.seekTo(i)
                    mpSoft?.start()
                    if (!(timeInSeconds > 0L && !hardTime && startButtonClicked)) mpSoft?.pause()
                    updateTimesTrack(false, true)
                }
            }
            override fun onStartTrackingTouch(p0: SeekBar?) {}
            override fun onStopTrackingTouch(p0: SeekBar?) {}
        })


    }
    private fun initMusic(){
        mpNotify = MediaPlayer.create(this, raw.notificacion)
        mpHard = MediaPlayer.create(this, raw.musica_fuerte)
        mpSoft = MediaPlayer.create(this, raw.musica_suave)

        mpHard?.isLooping = true
        mpSoft?.isLooping = true


        sbHardVolume = findViewById(id.sbHardVolume)
        sbSoftVolume = findViewById(id.sbSoftVolume)
        sbNotifyVolume = findViewById(id.sbNotifyVolume)

        sbHardTrack = findViewById(id.sbHardTrack)
        sbSoftTrack = findViewById(id.sbSoftTrack)

        setVolumes()
        setProgressTracks()
    }
    private fun notifySound(){
        mpNotify?.start()
    }
    private fun initObjects(){
        masiveNotifications()
        initChrono()
        hideLayouts()
        initSwitchs()
        initMetrics()
        initIntervalMode()
        initChallengeMode()
        initMusic()
        hidePopUpRun()

        initMap()
        hideAlert()

        initTotals()
        initLevels()
        initMedals()

        initPreferences()
        recoveryPreferences()
    }
    private fun masiveNotifications(){
        var dbNotifications = FirebaseFirestore.getInstance()
        dbNotifications.collection("notifications")
            .get()
            .addOnSuccessListener { documents->
                for (notification in documents){
                    dbNotifications.collection("notificationsReceived/$usermail/$usermail")
                        .whereEqualTo("notificationId", notification.get("id").toString())
                        .get()
                        .addOnSuccessListener { recived->
                            if (recived.size() == 0){
                                sendNotification(
                                    notification.get("title").toString(), notification.get("text").toString(),
                                    SimpleDateFormat("yyMMdd").format(Date()).toInt(), R.mipmap.ic_launcher)
                                dbNotifications.collection("notificationsReceived/$usermail/$usermail")
                                    .document(notification.get("id").toString()).set(hashMapOf(
                                        "notificationId" to notification.get("id").toString(),
                                        "receivedDate" to SimpleDateFormat("yyyyMMdd").format(Date())
                                    ))
                                    .addOnFailureListener { exception ->
                                        Log.w(ContentValues.TAG, "Error getting documents: ", exception)
                                    }
                            }
                        }
                        .addOnFailureListener { exception ->
                            Log.w(ContentValues.TAG, "Error getting documents: ", exception)
                        }
                }
            }
            .addOnFailureListener { exception ->
                Log.w(ContentValues.TAG, "Error getting documents: ", exception)
            }
    }

    private fun sendNotification(title: String, text: String,
                                 notificationId: Int, icon: Int){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){

            // Creación del canal de mensajes
            val channelId = "MySportZac"
            val channelName = "MySportZac"
            // Definir la prioridad del mensaje
            val importance = NotificationManager.IMPORTANCE_HIGH
            // Crear el canal para los envíos
            val channel = NotificationChannel(channelId, channelName, importance)

            // Crear el administrador de notificaciones
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)

            // Crear la notificación
            val notification =
                NotificationCompat.Builder(this, channelId).also{noti->
                    noti.setContentTitle(title)
                    noti.setContentText(text)
                    noti.setSmallIcon(icon)
                }.build()

            // Crear manejador de envíos y lanzar la notificación
            val notificationManager = NotificationManagerCompat.from(this)
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return
            }
            notificationManager.notify(notificationId, notification)
        }
    }
    private fun initTotals(){
        totalsBike = Totals()
        totalsRollerSkate = Totals()
        totalsRunning = Totals()

        totalsBike.totalRuns = 0
        totalsBike.totalDistance = 0.0
        totalsBike.totalTime = 0
        totalsBike.recordDistance = 0.0
        totalsBike.recordSpeed = 0.0
        totalsBike.recordAvgSpeed = 0.0

        totalsRollerSkate.totalRuns = 0
        totalsRollerSkate.totalDistance = 0.0
        totalsRollerSkate.totalTime = 0
        totalsRollerSkate.recordDistance = 0.0
        totalsRollerSkate.recordSpeed = 0.0
        totalsRollerSkate.recordAvgSpeed = 0.0

        totalsRunning.totalRuns = 0
        totalsRunning.totalDistance = 0.0
        totalsRunning.totalTime = 0
        totalsRunning.recordDistance = 0.0
        totalsRunning.recordSpeed = 0.0
        totalsRunning.recordAvgSpeed = 0.0
    }
    private fun initLevels(){
        levelSelectedSport = Level()
        levelBike = Level()
        levelRollerSkate = Level()
        levelRunning = Level()

        levelsListBike = arrayListOf()
        levelsListBike.clear()

        levelsListRollerSkate = arrayListOf()
        levelsListRollerSkate.clear()

        levelsListRunning = arrayListOf()
        levelsListRunning.clear()

        levelBike.name = "turtle"
        levelBike.image = "level_1"
        levelBike.RunsTarget = 5
        levelBike.DistanceTarget = 40

        levelRollerSkate.name = "turtle"
        levelRollerSkate.image = "level_1"
        levelRollerSkate.RunsTarget = 5
        levelRollerSkate.DistanceTarget = 20

        levelRunning.name = "turtle"
        levelRunning.image = "level_1"
        levelRunning.RunsTarget = 5
        levelRunning.DistanceTarget = 10
    }
    private fun initMedals(){
        medalsListSportSelectedDistance = arrayListOf()
        medalsListSportSelectedAvgSpeed = arrayListOf()
        medalsListSportSelectedMaxSpeed = arrayListOf()
        medalsListSportSelectedDistance.clear()
        medalsListSportSelectedAvgSpeed.clear()
        medalsListSportSelectedMaxSpeed.clear()

        medalsListBikeDistance = arrayListOf()
        medalsListBikeAvgSpeed = arrayListOf()
        medalsListBikeMaxSpeed = arrayListOf()
        medalsListBikeDistance.clear()
        medalsListBikeAvgSpeed.clear()
        medalsListBikeMaxSpeed.clear()

        medalsListRollerSkateDistance = arrayListOf()
        medalsListRollerSkateAvgSpeed = arrayListOf()
        medalsListRollerSkateMaxSpeed = arrayListOf()
        medalsListRollerSkateDistance.clear()
        medalsListRollerSkateAvgSpeed.clear()
        medalsListRollerSkateMaxSpeed.clear()

        medalsListRunningDistance = arrayListOf()
        medalsListRunningAvgSpeed = arrayListOf()
        medalsListRunningMaxSpeed = arrayListOf()
        medalsListRunningDistance.clear()
        medalsListRunningAvgSpeed.clear()
        medalsListRunningMaxSpeed.clear()
    }
    private fun resetMedals(){
        recDistanceGold = false
        recDistanceSilver = false
        recDistanceBronze = false
        recAvgSpeedGold = false
        recAvgSpeedSilver = false
        recAvgSpeedBronze = false
        recMaxSpeedGold = false
        recMaxSpeedSilver = false
        recMaxSpeedBronze = false
    }
    private fun loadFromDB(){
        loadTotalsUser()
        loadMedalsUser()
    }
    private fun loadTotalsUser() {
        loadTotalSport("Bike")
        loadTotalSport("RollerSkate")
        loadTotalSport("Running")
    }
    private fun loadTotalSport(sport: String){
        var collection = "totals$sport"
        var dbTotalsUser = FirebaseFirestore.getInstance()
        dbTotalsUser.collection(collection).document(usermail)
            .get()
            .addOnSuccessListener { document ->
                if (document.data?.size != null){
                    var total = document.toObject(Totals::class.java)
                    when (sport){
                        "Bike" -> totalsBike = total!!
                        "RollerSkate" -> totalsRollerSkate = total!!
                        "Running" -> totalsRunning = total!!
                    }

                }
                else{
                    val dbTotal: FirebaseFirestore = FirebaseFirestore.getInstance()
                    dbTotal.collection(collection).document(usermail).set(hashMapOf(
                        "recordAvgSpeed" to 0.0,
                        "recordDistance" to 0.0,
                        "recordSpeed" to 0.0,
                        "totalDistance" to 0.0,
                        "totalRuns" to 0,
                        "totalTime" to 0
                    ))
                }
                sportsLoaded++
                setLevelSport(sport)
                if (sportsLoaded == 3) selectSport(sportSelected)

            }
            .addOnFailureListener { exception ->
                Log.d("ERROR loadTotalsUser", "get failed with ", exception)
            }

    }
    private fun setLevelSport(sport: String){
        val dbLevels: FirebaseFirestore = FirebaseFirestore.getInstance()
        dbLevels.collection("levels$sport")
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents){
                    when (sport){
                        "Bike" -> levelsListBike.add(document.toObject(Level::class.java))
                        "RollerSkate" -> levelsListRollerSkate.add(document.toObject(Level::class.java))
                        "Running" -> levelsListRunning.add(document.toObject(Level::class.java))
                    }

                }
                when (sport){
                    "Bike" -> setLevelBike()
                    "RollerSkate" -> setLevelRollerSkate()
                    "Running" -> setLevelRunning()
                }

            }
            .addOnFailureListener { exception ->
                Log.w(ContentValues.TAG, "Error getting documents: ", exception)
            }
    }
    private fun setLevelBike(){
        var lyNavLevelBike = findViewById<LinearLayout>(id.lyNav_LevelBike)
        if (totalsBike.totalTime!! == 0) setHeightLinearLayout(lyNavLevelBike, 0)
        else{
            setHeightLinearLayout(lyNavLevelBike, 300)
            for (level in levelsListBike){
                if (totalsBike.totalRuns!! < level.RunsTarget!!
                    || totalsBike.totalDistance!! < level.DistanceTarget!!){

                    levelBike.name = level.name!!
                    levelBike.image = level.image!!
                    levelBike.RunsTarget = level.RunsTarget!!
                    levelBike.DistanceTarget = level.DistanceTarget!!

                    break
                }
            }

            var ivLevelBike = findViewById<ImageView>(id.ivLevelBike)
            var tvTotalTimeBike = findViewById<TextView>(id.tvTotalTimeBike)
            var tvTotalRunsBike = findViewById<TextView>(id.tvTotalRunsBike)
            var tvTotalDistanceBike = findViewById<TextView>(id.tvTotalDistanceBike)
            var tvNumberLevelBike = findViewById<TextView>(id.tvNumberLevelBike)

            var levelText = "${getString(string.level)} ${levelBike.image!!.subSequence(6,7).toString()}"

            tvNumberLevelBike.text = levelText

            var tt = getFormattedTotalTime(totalsBike.totalTime!!.toLong())
            tvTotalTimeBike.text = tt

            when (levelBike.image){
                "level_1" -> ivLevelBike.setImageResource(drawable.level_1)
                "level_2" -> ivLevelBike.setImageResource(drawable.level_2)
                "level_3" -> ivLevelBike.setImageResource(drawable.level_3)
                "level_4" -> ivLevelBike.setImageResource(drawable.level_4)
                "level_5" -> ivLevelBike.setImageResource(drawable.level_5)
                "level_6" -> ivLevelBike.setImageResource(drawable.level_6)
                "level_7" -> ivLevelBike.setImageResource(drawable.level_7)
            }
            tvTotalRunsBike.text = "${totalsBike.totalRuns}/${levelBike.RunsTarget}"
            var porcent = totalsBike.totalDistance!!.toInt() * 100 / levelBike.DistanceTarget!!.toInt()
            tvTotalDistanceBike.text = "${porcent.toInt()}%"

            var csbDistanceBike = findViewById<CircularSeekBar>(id.csbDistanceBike)
            csbDistanceBike.max = levelBike.DistanceTarget!!.toFloat()
            if (totalsBike.totalDistance!! >= levelBike.DistanceTarget!!.toDouble())
                csbDistanceBike.progress = csbDistanceBike.max
            else
                csbDistanceBike.progress = totalsBike.totalDistance!!.toFloat()

            var csbRunsBike = findViewById<CircularSeekBar>(id.csbRunsBike)
            csbRunsBike.max = levelBike.RunsTarget!!.toFloat()
            if (totalsBike.totalRuns!! >= levelBike.RunsTarget!!.toInt())
                csbRunsBike.progress = csbRunsBike.max
            else
                csbRunsBike.progress = totalsBike.totalRuns!!.toFloat()

        }
    }
    private fun setLevelRollerSkate(){

        var lyNavLevelRollerSkate = findViewById<LinearLayout>(id.lyNavLevelRollerSkate)
        if (totalsRollerSkate.totalTime!! == 0) setHeightLinearLayout(lyNavLevelRollerSkate, 0)
        else{

            setHeightLinearLayout(lyNavLevelRollerSkate, 300)
            for (level in levelsListRollerSkate){
                if (totalsRollerSkate.totalRuns!! < level.RunsTarget!!.toInt()
                    || totalsRollerSkate.totalDistance!! < level.DistanceTarget!!.toDouble()){

                    levelRollerSkate.name = level.name!!
                    levelRollerSkate.image = level.image!!
                    levelRollerSkate.RunsTarget = level.RunsTarget!!
                    levelRollerSkate.DistanceTarget = level.DistanceTarget!!

                    break
                }
            }

            var ivLevelRollerSkate = findViewById<ImageView>(id.ivLevelRollerSkate)
            var tvTotalTimeRollerSkate = findViewById<TextView>(id.tvTotalTimeRollerSkate)
            var tvTotalRunsRollerSkate = findViewById<TextView>(id.tvTotalRunsRollerSkate)
            var tvTotalDistanceRollerSkate = findViewById<TextView>(id.tvTotalDistanceRollerSkate)

            var tvNumberLevelRollerSkate = findViewById<TextView>(id.tvNumberLevelRollerSkate)
            var levelText = "${getString(string.level)} ${levelRollerSkate.image!!.subSequence(6,7).toString()}"
            tvNumberLevelRollerSkate.text = levelText

            var tt = getFormattedTotalTime(totalsRollerSkate.totalTime!!.toLong())
            tvTotalTimeRollerSkate.text = tt

            when (levelRollerSkate.image){
                "level_1" -> ivLevelRollerSkate.setImageResource(drawable.level_1)
                "level_2" -> ivLevelRollerSkate.setImageResource(drawable.level_2)
                "level_3" -> ivLevelRollerSkate.setImageResource(drawable.level_3)
                "level_4" -> ivLevelRollerSkate.setImageResource(drawable.level_4)
                "level_5" -> ivLevelRollerSkate.setImageResource(drawable.level_5)
                "level_6" -> ivLevelRollerSkate.setImageResource(drawable.level_6)
                "level_7" -> ivLevelRollerSkate.setImageResource(drawable.level_7)
            }


            tvTotalRunsRollerSkate.text = "${totalsRollerSkate.totalRuns}/${levelRollerSkate.RunsTarget}"

            var porcent = totalsRollerSkate.totalDistance!!.toInt() * 100 / levelRollerSkate.DistanceTarget!!.toInt()
            tvTotalDistanceRollerSkate.text = "${porcent.toInt()}%"

            var csbDistanceRollerSkate = findViewById<CircularSeekBar>(id.csbDistanceRollerSkate)
            csbDistanceRollerSkate.max = levelRollerSkate.DistanceTarget!!.toFloat()
            if (totalsRollerSkate.totalDistance!! >= levelRollerSkate.DistanceTarget!!.toDouble())
                csbDistanceRollerSkate.progress = csbDistanceRollerSkate.max
            else
                csbDistanceRollerSkate.progress = totalsRollerSkate.totalDistance!!.toFloat()

            var csbRunsRollerSkate = findViewById<CircularSeekBar>(id.csbRunsRollerSkate)
            csbRunsRollerSkate.max = levelRollerSkate.RunsTarget!!.toFloat()
            if (totalsRollerSkate.totalRuns!! >= levelRollerSkate.RunsTarget!!.toInt())
                csbRunsRollerSkate.progress = csbRunsRollerSkate.max
            else
                csbRunsRollerSkate.progress = totalsRollerSkate.totalRuns!!.toFloat()
        }
    }
    private fun setLevelRunning(){
        var lyNavLevelRunning = findViewById<LinearLayout>(id.lyNavLevelRunning)
        if (totalsRunning.totalTime!! == 0) setHeightLinearLayout(lyNavLevelRunning, 0)
        else{

            setHeightLinearLayout(lyNavLevelRunning, 300)
            for (level in levelsListRunning){
                if (totalsRunning.totalRuns!! < level.RunsTarget!!.toInt()
                    || totalsRunning.totalDistance!! < level.DistanceTarget!!.toDouble()){

                    levelRunning.name = level.name!!
                    levelRunning.image = level.image!!
                    levelRunning.RunsTarget = level.RunsTarget!!
                    levelRunning.DistanceTarget = level.DistanceTarget!!

                    break
                }
            }

            var ivLevelRunning = findViewById<ImageView>(id.ivLevelRunning)
            var tvTotalTimeRunning = findViewById<TextView>(id.tvTotalTimeRunning)
            var tvTotalRunsRunning = findViewById<TextView>(id.tvTotalRunsRunning)
            var tvTotalDistanceRunning = findViewById<TextView>(id.tvTotalDistanceRunning)


            var tvNumberLevelRunning = findViewById<TextView>(id.tvNumberLevelRunning)
            var levelText = "${getString(string.level)} ${levelRunning.image!!.subSequence(6,7).toString()}"
            tvNumberLevelRunning.text = levelText

            var tt = getFormattedTotalTime(totalsRunning.totalTime!!.toLong())
            tvTotalTimeRunning.text = tt

            when (levelRunning.image){
                "level_1" -> ivLevelRunning.setImageResource(drawable.level_1)
                "level_2" -> ivLevelRunning.setImageResource(drawable.level_2)
                "level_3" -> ivLevelRunning.setImageResource(drawable.level_3)
                "level_4" -> ivLevelRunning.setImageResource(drawable.level_4)
                "level_5" -> ivLevelRunning.setImageResource(drawable.level_5)
                "level_6" -> ivLevelRunning.setImageResource(drawable.level_6)
                "level_7" -> ivLevelRunning.setImageResource(drawable.level_7)
            }

            tvTotalRunsRunning.text = "${totalsRunning.totalRuns}/${levelRunning.RunsTarget}"
            var porcent = totalsRunning.totalDistance!!.toInt() * 100 / levelRunning.DistanceTarget!!.toInt()
            tvTotalDistanceRunning.text = "${porcent.toInt()}%"

            var csbDistanceRunning = findViewById<CircularSeekBar>(id.csbDistanceRunning)
            csbDistanceRunning.max = levelRunning.DistanceTarget!!.toFloat()
            if (totalsRunning.totalDistance!! >= levelRunning.DistanceTarget!!.toDouble())
                csbDistanceRunning.progress = csbDistanceRunning.max
            else
                csbDistanceRunning.progress = totalsRunning.totalDistance!!.toFloat()

            var csbRunsRunning = findViewById<CircularSeekBar>(id.csbRunsRunning)
            csbRunsRunning.max = levelRunning.RunsTarget!!.toFloat()
            if (totalsRunning.totalRuns!! >= levelRunning.RunsTarget!!.toInt())
                csbRunsRunning.progress = csbRunsRunning.max
            else
                csbRunsRunning.progress = totalsRunning.totalRuns!!.toFloat()

        }
    }
    private fun loadMedalsUser(){
        loadMedalsBike()
        loadMedalsRollerSkate()
        loadMedalsRunning()
    }
    private fun loadMedalsBike(){
        var dbRecords = FirebaseFirestore.getInstance()
        dbRecords.collection("runsBike")
            .orderBy("distance", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents){
                    if (document["user"] == usermail)
                        medalsListBikeDistance.add (document["distance"].toString().toDouble())
                    if (medalsListBikeDistance.size == 3) break
                }
                while (medalsListBikeDistance.size < 3) medalsListBikeDistance.add(0.0)

            }
            .addOnFailureListener { exception ->
                Log.w(ContentValues.TAG, "Error getting documents: ", exception)
            }

        dbRecords.collection("runsBike")
            .orderBy("avgSpeed", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    if (document["user"] == usermail)
                        medalsListBikeAvgSpeed.add (document["avgSpeed"].toString().toDouble())
                    if (medalsListBikeAvgSpeed.size == 3) break
                }
                while (medalsListBikeAvgSpeed.size < 3) medalsListBikeAvgSpeed.add(0.0)

            }
            .addOnFailureListener { exception ->
                Log.w(ContentValues.TAG, "Error getting documents: ", exception)
            }

        dbRecords.collection("runsBike")
            .orderBy("maxSpeed", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    if (document["user"] == usermail)
                        medalsListBikeMaxSpeed.add (document["maxSpeed"].toString().toDouble())
                    if (medalsListBikeMaxSpeed.size == 3) break
                }
                while (medalsListBikeMaxSpeed.size < 3) medalsListBikeMaxSpeed.add(0.0)

            }
            .addOnFailureListener { exception ->
                Log.w(ContentValues.TAG, "Error getting documents: ", exception)
            }
    }
    private fun loadMedalsRollerSkate(){
        var dbRecords = FirebaseFirestore.getInstance()
        dbRecords.collection("runsRollerSkate")
            .orderBy("distance", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents){
                    if (document["user"] == usermail)
                        medalsListRollerSkateDistance.add (document["distance"].toString().toDouble())
                    if (medalsListRollerSkateDistance.size == 3) break
                }
                while (medalsListRollerSkateDistance.size < 3) medalsListRollerSkateDistance.add(0.0)

            }
            .addOnFailureListener { exception ->
                Log.w(ContentValues.TAG, "Error getting documents: ", exception)
            }

        dbRecords.collection("runsRollerSkate")
            .orderBy("avgSpeed", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    if (document["user"] == usermail)
                        medalsListRollerSkateAvgSpeed.add (document["avgSpeed"].toString().toDouble())
                    if (medalsListRollerSkateAvgSpeed.size == 3) break
                }
                while (medalsListRollerSkateAvgSpeed.size < 3) medalsListRollerSkateAvgSpeed.add(0.0)

            }
            .addOnFailureListener { exception ->
                Log.w(ContentValues.TAG, "Error getting documents: ", exception)
            }

        dbRecords.collection("runsRollerSkate")
            .orderBy("maxSpeed", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    if (document["user"] == usermail)
                        medalsListRollerSkateMaxSpeed.add (document["maxSpeed"].toString().toDouble())
                    if (medalsListRollerSkateMaxSpeed.size == 3) break
                }
                while (medalsListRollerSkateMaxSpeed.size < 3) medalsListRollerSkateMaxSpeed.add(0.0)

            }
            .addOnFailureListener { exception ->
                Log.w(ContentValues.TAG, "Error getting documents: ", exception)
            }
    }
    private fun loadMedalsRunning(){
        var dbRecords = FirebaseFirestore.getInstance()
        dbRecords.collection("runsRunning")
            .orderBy("distance", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents){
                    if (document["user"] == usermail)
                        medalsListRunningDistance.add (document["distance"].toString().toDouble())
                    if (medalsListRunningDistance.size == 3) break
                }
                while (medalsListRunningDistance.size < 3) medalsListRunningDistance.add(0.0)

            }
            .addOnFailureListener { exception ->
                Log.w(ContentValues.TAG, "Error getting documents: ", exception)
            }

        dbRecords.collection("runsRunning")
            .orderBy("avgSpeed", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    if (document["user"] == usermail)
                        medalsListRunningAvgSpeed.add (document["avgSpeed"].toString().toDouble())
                    if (medalsListRunningAvgSpeed.size == 3) break
                }
                while (medalsListRunningAvgSpeed.size < 3) medalsListRunningAvgSpeed.add(0.0)

            }
            .addOnFailureListener { exception ->
                Log.w(ContentValues.TAG, "Error getting documents: ", exception)
            }

        dbRecords.collection("runsRunning")
            .orderBy("maxSpeed", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    if (document["user"] == usermail)
                        medalsListRunningMaxSpeed.add (document["maxSpeed"].toString().toDouble())
                    if (medalsListRunningMaxSpeed.size == 3) break
                }
                while (medalsListRunningMaxSpeed.size < 3) medalsListRunningMaxSpeed.add(0.0)

            }
            .addOnFailureListener { exception ->
                Log.w(ContentValues.TAG, "Error getting documents: ", exception)
            }
    }
    private fun initPreferences(){
        sharedPreferences = getSharedPreferences("sharedPrefs_$usermail", MODE_PRIVATE)
        editor = sharedPreferences.edit()
    }
    private fun recoveryPreferences(){
        if (sharedPreferences.getString(key_userApp, "null") == usermail) {
            sportSelected = sharedPreferences.getString(key_selectedSport, "Running").toString()

            swIntervalMode.isChecked = sharedPreferences.getBoolean(key_modeInterval, false)
            if (swIntervalMode.isChecked){
                npDurationInterval.value = sharedPreferences.getInt(key_intervalDuration, 5)
                ROUND_INTERVAL = npDurationInterval.value*60
                csbRunWalk.progress = sharedPreferences.getFloat(key_progressCircularSeekBar, 150.0f)
                csbRunWalk.max = sharedPreferences.getFloat(key_maxCircularSeekBar, 300.0f)
                tvRunningTime.text = sharedPreferences.getString(key_runningTime, "2:30")
                tvWalkingTime.text = sharedPreferences.getString(key_walkingTime, "2:30")
                swIntervalMode.callOnClick()
            }

            swChallenges.isChecked = sharedPreferences.getBoolean(key_modeChallenge, false)
            if (swChallenges.isChecked) {
                swChallenges.callOnClick()
                if (sharedPreferences.getBoolean(key_modeChallengeDuration, false)) {
                    npChallengeDurationHH.value =
                        sharedPreferences.getInt(key_challengeDurationHH, 1)
                    npChallengeDurationMM.value =
                        sharedPreferences.getInt(key_challengeDurationMM, 0)
                    npChallengeDurationSS.value =
                        sharedPreferences.getInt(key_challengeDurationSS, 0)
                    getChallengeDuration(
                        npChallengeDurationHH.value,
                        npChallengeDurationMM.value,
                        npChallengeDurationSS.value
                    )
                    challengeDistance = 0f

                    showChallenge("duration")
                }
                if (sharedPreferences.getBoolean(key_modeChallengeDistance, false)) {
                    npChallengeDistance.value = sharedPreferences.getInt(key_challengeDistance, 10)
                    challengeDistance = npChallengeDistance.value.toFloat()
                    challengeDuration = 0

                    showChallenge("distance")
                }
            }
            cbNotify.isChecked = sharedPreferences.getBoolean(key_challengeNofify, true)
            cbAutoFinish.isChecked = sharedPreferences.getBoolean(key_challengeAutofinish, false)

            sbHardVolume.progress = sharedPreferences.getInt(key_hardVol, 100)
            sbSoftVolume.progress = sharedPreferences.getInt(key_softVol, 100)
            sbNotifyVolume.progress = sharedPreferences.getInt(key_notifyVol, 100)

        }
        else sportSelected = "Running"
    }
    private fun savePreferences(){
        editor.clear()
        editor.apply{

            putString(key_userApp, usermail)
            putString(key_provider, providerSession)

            putString(key_selectedSport, sportSelected)

            putBoolean(key_modeInterval, swIntervalMode.isChecked)
            putInt(key_intervalDuration, npDurationInterval.value)
            putFloat(key_progressCircularSeekBar, csbRunWalk.progress)
            putFloat(key_maxCircularSeekBar, csbRunWalk.max)
            putString(key_runningTime, tvRunningTime.text.toString())
            putString(key_walkingTime, tvWalkingTime.text.toString())

            putBoolean(key_modeChallenge, swChallenges.isChecked)
            putBoolean(key_modeChallengeDuration, !(challengeDuration == 0))
            putInt(key_challengeDurationHH, npChallengeDurationHH.value)
            putInt(key_challengeDurationMM, npChallengeDurationMM.value)
            putInt(key_challengeDurationSS, npChallengeDurationSS.value)
            putBoolean(key_modeChallengeDistance, !(challengeDistance == 0f))
            putInt(key_challengeDistance, npChallengeDistance.value)


            putBoolean(key_challengeNofify, cbNotify.isChecked)
            putBoolean(key_challengeAutofinish, cbAutoFinish.isChecked)

            putInt(key_hardVol, sbHardVolume.progress)
            putInt(key_softVol, sbSoftVolume.progress)
            putInt(key_notifyVol, sbNotifyVolume.progress)

        }.apply()
    }
    private fun callClearPreferences(){
        editor.clear().apply()
        Toast.makeText(this, "Tus ajustes han sido reestablecidos :)", Toast.LENGTH_SHORT).show()
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
            id.nav_item_clearpreferences -> alertClearPreferences()
            id.nav_item_signout -> alertSignOut()

        }
        drawer.closeDrawer(GravityCompat.START)
        return true
    }
    private fun callRecordActivity(){

        if (startButtonClicked) manageStartStop()

        val intent = Intent(this, RecordActivity::class.java)
        startActivity(intent)
    }
    fun inflateIntervalMode(v: View){
        inflateintervalMode()
    }
    private fun inflateintervalMode() {
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
            var tvRunnigTime = findViewById<TextView>(id.tvRunningTime)
            TIME_RUNNING = getSecFromWatch(tvRunnigTime.text.toString())

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
        if (timeInSeconds == 0L) showChallenge("duration")
    }
    fun showDistance(v:View){
        if (timeInSeconds == 0L) showChallenge("distance")
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
    private fun initMap(){
        listPoints = arrayListOf()
        (listPoints as ArrayList<LatLng>).clear()

        createMapFragment()
        var lyOpenerButton = findViewById<LinearLayout>(id.lyOpenerButton)
        if (allPermissionsGrantedGPS()) lyOpenerButton.isEnabled = true
        else lyOpenerButton.isEnabled = false

    }
    override fun onMyLocationButtonClick(): Boolean {
        return false
    }
    override fun onMyLocationClick(p0: Location) {

    }
    private fun createMapFragment(){
        val mapFragment = supportFragmentManager.findFragmentById(id.fragmentMap) as SupportMapFragment?
        mapFragment?.getMapAsync(this)
    }
    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap

        googleMap.mapType = GoogleMap.MAP_TYPE_HYBRID
        enableMyLocation()
        map.setOnMyLocationButtonClickListener(this)
        map.setOnMyLocationClickListener(this)
        map.setOnMapLongClickListener { mapCentered = false }
        map.setOnMapClickListener { mapCentered = false }

        manageLocation()

        centerMap(init_lt, init_ln)

    }
    override fun onRequestPermissionsResult(requestCode: Int,permissions: Array<out String>,grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when (requestCode){
            LOCATION_PERMISSION_REQ_CODE -> {
                var lyOpenerButton = findViewById<LinearLayout>(id.lyOpenerButton)

                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                    lyOpenerButton.isEnabled = true
                else{
                    var lyMap = findViewById<LinearLayout>(id.lyMap)
                    if (lyMap.height > 0){
                        setHeightLinearLayout(lyMap, 0)

                        var lyFragmentMap = findViewById<LinearLayout>(id.lyFragmentMap)
                        lyFragmentMap.translationY= -300f

                        var ivOpenClose = findViewById<ImageView>(id.ivOpenClose)
                        ivOpenClose.setRotation(0f)
                    }

                    lyOpenerButton.isEnabled = false

                }
            }
        }
    }
    private fun enableMyLocation(){
        if (!::map.isInitialized)return
        if (ActivityCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED

            && ActivityCompat.checkSelfPermission(this,Manifest.permission.ACCESS_COARSE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {
            requestPermissionLocation()
            return
        }
        else map.isMyLocationEnabled = true

    }
    private fun centerMap(lt: Double, ln: Double){
        val posMap = LatLng(lt,ln)
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(posMap, 16f),1000, null)
    }
    fun changeTypeMap(v: View){
        var ivTypeMap = findViewById<ImageView>(id.ivTypeMap)
        if (map.mapType == GoogleMap.MAP_TYPE_HYBRID){
            map.mapType = GoogleMap.MAP_TYPE_NORMAL
            ivTypeMap.setImageResource(drawable.map_type_hybrid)
        }
        else{
            map.mapType = GoogleMap.MAP_TYPE_HYBRID
            ivTypeMap.setImageResource(drawable.map_type_normal)
        }
    }
    fun callCenterMap(v: View){
        mapCentered = true
        if (latitude == 0.0) centerMap(init_lt, init_ln)
        else centerMap(latitude, longitude)
    }
    fun callShowHideMap(v: View){
        if (allPermissionsGrantedGPS()){
            var lyMap = findViewById<LinearLayout>(id.lyMap)
            var lyFragmentMap = findViewById<LinearLayout>(id.lyFragmentMap)
            var ivOpenClose = findViewById<ImageView>(id.ivOpenClose)

            if (lyMap.height == 0){
                setHeightLinearLayout(lyMap, 1300)
                animateViewofFloat(lyFragmentMap, "translationY", 0f, 0)
                ivOpenClose.setRotation(180f)
            }
            else{
                setHeightLinearLayout(lyMap, 0)
                lyFragmentMap.translationY= -300f
                ivOpenClose.setRotation(0f)
            }

        }
        else requestPermissionLocation()
    }
    private fun initPermissionsGPS(){
        if (allPermissionsGrantedGPS())
            fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        else
            requestPermissionLocation()
    }
    private fun requestPermissionLocation(){
        ActivityCompat.requestPermissions(this, arrayOf(
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION), PERMISSION_ID)
    }
    private fun allPermissionsGrantedGPS() = REQUIRED_PERMISSIONS_GPS.all{
        ContextCompat.checkSelfPermission(baseContext, it) == PackageManager.PERMISSION_GRANTED
    }
    private fun islocationEnabled():Boolean{
        var locationManager: LocationManager
                = getSystemService(LOCATION_SERVICE) as LocationManager
        return  locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }
    private fun activationLocation(){
        val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
        startActivity(intent)
    }
    private fun checkPermission(): Boolean{
        return ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED
    }
    private fun manageLocation(){
        if (checkPermission()){

            if (islocationEnabled()){
                if (ActivityCompat.checkSelfPermission(this,
                        Manifest.permission.ACCESS_FINE_LOCATION) ==
                    PackageManager.PERMISSION_GRANTED
                    &&  ActivityCompat.checkSelfPermission(this,
                        Manifest.permission.ACCESS_COARSE_LOCATION) ==
                    PackageManager.PERMISSION_GRANTED) {


                    fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                        requestNewLocationData()
                    }
                }
            }
            else activationLocation()
        }
        else requestPermissionLocation()
    }
    @SuppressLint("MissingPermission")
    private fun requestNewLocationData(){
        var mLocationRequest = LocationRequest()
        mLocationRequest.priority = PRIORITY_HIGH_ACCURACY
        mLocationRequest.interval = 0
        mLocationRequest.fastestInterval = 0
        mLocationRequest.numUpdates = 1
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        fusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallBack, Looper.myLooper())


    }
    private val mLocationCallBack = object: LocationCallback(){
        override fun onLocationResult(locationResult: LocationResult) {
            var mLastLocation : Location = locationResult.lastLocation

            init_lt = mLastLocation.latitude
            init_ln = mLastLocation.longitude

            if (timeInSeconds > 0L) registerNewLocation(mLastLocation)
        }
    }
    private fun registerNewLocation(location: Location){
        var new_latitude: Double = location.latitude
        var new_longitude: Double = location.longitude

        if (flagSavedLocation){
            if (timeInSeconds >= INTERVAL_LOCATION){
                var distanceInterval = calculateDistance(new_latitude, new_longitude)

                if ( distanceInterval <= LIMIT_DISTANCE_ACCEPTED){
                    updateSpeeds(distanceInterval)
                    refreshInterfaceData()

                    saveLocation(location)

                    var newPos = LatLng (new_latitude, new_longitude)
                    (listPoints as ArrayList<LatLng>).add(newPos)
                    createPolylines(listPoints)

                    checkMedals(distance, avgSpeed, maxSpeed)
                }


            }
        }
        latitude = new_latitude
        longitude = new_longitude

        if (mapCentered == true) centerMap(latitude, longitude)
        if (minLatitude == null){
            minLatitude = latitude
            maxLatitude = latitude
            minLongitude = longitude
            maxLongitude = longitude
        }
        if (latitude < minLatitude!!) minLatitude = latitude
        if (latitude > maxLatitude!!) maxLatitude = latitude
        if (longitude < minLongitude!!) minLongitude = longitude
        if (longitude > maxLongitude!!) maxLongitude = longitude

        if (location.hasAltitude()){
            if (maxAltitude == null){
                maxAltitude = location.altitude
                minAltitude = location.altitude
            }
            if (location.latitude > maxAltitude!!) maxAltitude = location.altitude
            if (location.latitude < minAltitude!!) minAltitude = location.altitude
        }

    }
    private fun checkMedals(d: Double, aS: Double, mS: Double){
        if (d>0){
            if (d >= medalsListSportSelectedDistance.get(0)){
                recDistanceGold = true; recDistanceSilver = false; recDistanceBronze = false
                notifyMedal("distance", "gold", "PERSONAL")
            }
            else{
                if (d >= medalsListSportSelectedDistance.get(1)){
                    recDistanceGold = false; recDistanceSilver = true; recDistanceBronze = false
                    notifyMedal("distance", "silver", "PERSONAL")
                }
                else{
                    if (d >= medalsListSportSelectedDistance.get(2)){
                        recDistanceGold = false; recDistanceSilver = false; recDistanceBronze = true
                        notifyMedal("distance", "bronze", "PERSONAL")
                    }
                }
            }
        }

        if (aS > 0){
            if (aS >= medalsListSportSelectedAvgSpeed.get(0)){
                recAvgSpeedGold = true; recAvgSpeedSilver = false; recAvgSpeedBronze = false
                notifyMedal("avgSpeed", "gold", "PERSONAL")
            }
            else{
                if (aS >= medalsListSportSelectedAvgSpeed.get(1)){
                    recAvgSpeedGold = false; recAvgSpeedSilver = true; recAvgSpeedBronze = false
                    notifyMedal("avgSpeed", "silver", "PERSONAL")
                }
                else{
                    if (aS >= medalsListSportSelectedAvgSpeed.get(2)){
                        recAvgSpeedGold = false; recAvgSpeedSilver = false; recAvgSpeedBronze = true
                        notifyMedal("avgSpeed", "bronze", "PERSONAL")
                    }
                }
            }
        }

        if (mS > 0){
            if (mS >= medalsListSportSelectedMaxSpeed.get(0)){
                recMaxSpeedGold = true; recMaxSpeedSilver = false; recMaxSpeedBronze = false
                notifyMedal("maxSpeed", "gold", "PERSONAL")
            }
            else{
                if (mS >= medalsListSportSelectedMaxSpeed.get(1)){
                    recMaxSpeedGold = false; recMaxSpeedSilver = true; recMaxSpeedBronze = false
                    notifyMedal("maxSpeed", "silver", "PERSONAL")
                }
                else{
                    if (mS >= medalsListSportSelectedMaxSpeed.get(2)){
                        recMaxSpeedGold = false; recMaxSpeedSilver = false; recMaxSpeedBronze = true
                        notifyMedal("maxSpeed", "bronze", "PERSONAL")
                    }
                }
            }
        }

    }
    @SuppressLint("MissingPermission")
    private fun notifyMedal(category: String, metal: String, scope: String) {

        val CHANNEL_NAME = "notifyMedal"
        val IMPORTANCE = NotificationManager.IMPORTANCE_HIGH
        var CHANNEL_ID = "NEW $scope RECORD - $sportSelected"

        var textNotification = ""
        when (metal) {
            "gold" -> textNotification = "1ª "
            "silver" -> textNotification = "2ª "
            "bronze" -> textNotification = "3ª "
        }
        textNotification += "mejor marca personal en "
        when (category) {
            "distance" -> textNotification += "distancia recorrida"
            "avgSpeed" -> textNotification += " velocidad promedio"
            "maxSpeed" -> textNotification += " velocidad máxima alcanzada"
        }

        //Guardamos las medallas en una variable
        var iconNotificacion: Int = 0
        when (metal) {
            "gold" -> iconNotificacion = R.drawable.medalgold
            "silver" -> iconNotificacion = R.drawable.medalsilver
            "bronze" -> iconNotificacion = R.drawable.medalbronze
        }

        //Constructor del Canal
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, IMPORTANCE)
            val notificationManager: NotificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)

            //Constructor de la Notificacion
            var builder = NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(iconNotificacion)
                .setContentTitle(CHANNEL_ID)
                .setContentText(textNotification)
                .setPriority(NotificationCompat.PRIORITY_HIGH)

            var notificationId: Int = 0
            when (category) {
                "distance" ->
                    when (metal) {
                        "gold" -> notificationId = 11
                        "silver" -> notificationId = 12
                        "bronze" -> notificationId = 13
                    }
                "avgSpeed" ->
                    when (metal) {
                        "gold" -> notificationId = 21
                        "silver" -> notificationId = 22
                        "bronze" -> notificationId = 23
                    }
                "maxSpeed" ->
                    when (metal) {
                        "gold" -> notificationId = 31
                        "silver" -> notificationId = 32
                        "bronze" -> notificationId = 33
                    }
            }

            with(NotificationManagerCompat.from(this)) {
                notify(notificationId, builder.build())
            }
        }
    }
    private fun calculateDistance(n_lt: Double, n_lg: Double): Double{
        val radioTierra = 6371.0 //en kilómetros

        val dLat = Math.toRadians(n_lt - latitude)
        val dLng = Math.toRadians(n_lg - longitude)
        val sindLat = Math.sin(dLat / 2)
        val sindLng = Math.sin(dLng / 2)
        val va1 =
            Math.pow(sindLat, 2.0) + (Math.pow(sindLng, 2.0)
                    * Math.cos(Math.toRadians(latitude)) * Math.cos(
                Math.toRadians( n_lt  )
            ))
        val va2 = 2 * Math.atan2(Math.sqrt(va1), Math.sqrt(1 - va1))
        var n_distance =  radioTierra * va2

        if (n_distance < LIMIT_DISTANCE_ACCEPTED) distance += n_distance

        return n_distance
    }
    private fun updateSpeeds(d: Double) {
        //la distancia se calcula en km, asi que la pasamos a metros para el calculo de velocidad
        //convertirmos m/s a km/h multiplicando por 3.6
        speed = ((d * 1000) / INTERVAL_LOCATION) * 3.6
        if (speed > maxSpeed) maxSpeed = speed
        avgSpeed = ((distance * 1000) / timeInSeconds) * 3.6
    }
    private fun refreshInterfaceData(){
        var tvCurrentDistance = findViewById<TextView>(id.tvCurrentDistance)
        var tvCurrentAvgSpeed = findViewById<TextView>(id.tvCurrentAvgSpeed)
        var tvCurrentSpeed = findViewById<TextView>(id.tvCurrentSpeed)
        tvCurrentDistance.text = roundNumber(distance.toString(), 2)
        tvCurrentAvgSpeed.text = roundNumber(avgSpeed.toString(), 1)
        tvCurrentSpeed.text = roundNumber(speed.toString(), 1)


        csbCurrentDistance.progress = distance.toFloat()
        if (distance > totalsSelectedSport.recordDistance!!){
            tvDistanceRecord.text = roundNumber(distance.toString(), 1)
            tvDistanceRecord.setTextColor(ContextCompat.getColor(this, color.salmon_dark))

            csbCurrentDistance.max = distance.toFloat()
            csbCurrentDistance.progress = distance.toFloat()

            totalsSelectedSport.recordDistance = distance
        }

        csbCurrentAvgSpeed.progress = avgSpeed.toFloat()
        if (avgSpeed > totalsSelectedSport.recordAvgSpeed!!){
            tvAvgSpeedRecord.text = roundNumber(avgSpeed.toString(), 1)
            tvAvgSpeedRecord.setTextColor(ContextCompat.getColor(this, color.salmon_dark))

            csbRecordAvgSpeed.max = avgSpeed.toFloat()
            csbRecordAvgSpeed.progress = avgSpeed.toFloat()
            csbCurrentAvgSpeed.max = avgSpeed.toFloat()

            totalsSelectedSport.recordAvgSpeed = avgSpeed
        }


        if (speed > totalsSelectedSport.recordSpeed!!){
            tvMaxSpeedRecord.text = roundNumber(speed.toString(), 1)
            tvMaxSpeedRecord.setTextColor(ContextCompat.getColor(this, color.salmon_dark))

            csbRecordSpeed.max = speed.toFloat()
            csbRecordSpeed.progress = speed.toFloat()

            csbCurrentMaxSpeed.max = speed.toFloat()
            csbCurrentMaxSpeed.progress = speed.toFloat()

            csbCurrentSpeed.max = speed.toFloat()

            totalsSelectedSport.recordSpeed = speed
        }
        else{
            if (speed == maxSpeed){
                csbCurrentMaxSpeed.max = csbRecordSpeed.max
                csbCurrentMaxSpeed.progress = speed.toFloat()

                csbCurrentSpeed.max = csbRecordSpeed.max
            }
        }

        csbCurrentSpeed.progress = speed.toFloat()

    }
    private fun createPolylines(listPosition: Iterable<LatLng>){
        val polylineOptions = PolylineOptions()
            .width(25f)
            .color(ContextCompat.getColor(this, color.salmon_dark))
            .addAll(listPosition)

        val polyline = map.addPolyline(polylineOptions)
        polyline.startCap = RoundCap()

    }
    private fun saveLocation(location: Location){
        var dirName = dateRun + startTimeRun
        dirName = dirName.replace("/", "")
        dirName = dirName.replace(":", "")

        var docName = timeInSeconds.toString()
        while (docName.length < 4) docName = "0" + docName

        var ms: Boolean
        ms = speed == maxSpeed && speed > 0


        var dbLocation = FirebaseFirestore.getInstance()
        dbLocation.collection("locations/$usermail/$dirName").document(docName).set(hashMapOf(
            "time" to SimpleDateFormat("HH:mm:ss").format(Date()),
            "latitude" to location.latitude,
            "longitude" to location.longitude,
            "altitude" to location.altitude,
            "hasAltitude" to location.hasAltitude(),
            "speedFromGoogle" to location.speed,
            "speedFromMe" to speed,
            "maxSpeed" to ms,
            "color" to tvChrono.currentTextColor
        ))
    }
    fun selectBike(v: View){
        if (timeInSeconds.toInt() == 0) selectSport("Bike")
    }
    fun selectRollerSkate(v: View){
        if (timeInSeconds.toInt() == 0) selectSport("RollerSkate")
    }
    fun selectRunning(v: View){
        if (timeInSeconds.toInt() == 0) selectSport("Running")
    }
    private fun selectSport(sport: String){

        sportSelected = sport

        var lySportBike = findViewById<LinearLayout>(id.lySportBike)
        var lySportRollerSkate = findViewById<LinearLayout>(id.lySportRollerSkate)
        var lySportRunning = findViewById<LinearLayout>(id.lySportRunning)

        when (sport){
            "Bike"->{
                LIMIT_DISTANCE_ACCEPTED = LIMIT_DISTANCE_ACCEPTED_BIKE

                lySportBike.setBackgroundColor(ContextCompat.getColor(mainContext, color.orange))
                lySportRollerSkate.setBackgroundColor(ContextCompat.getColor(mainContext, color.gray_medium))
                lySportRunning.setBackgroundColor(ContextCompat.getColor(mainContext, color.gray_medium))

                levelSelectedSport = levelBike
                totalsSelectedSport = totalsBike

                medalsListSportSelectedDistance = medalsListBikeDistance
                medalsListSportSelectedAvgSpeed = medalsListBikeAvgSpeed
                medalsListSportSelectedMaxSpeed = medalsListBikeMaxSpeed
            }
            "RollerSkate"->{
                LIMIT_DISTANCE_ACCEPTED = LIMIT_DISTANCE_ACCEPTED_ROLLERSKATE

                lySportBike.setBackgroundColor(ContextCompat.getColor(mainContext, color.gray_medium))
                lySportRollerSkate.setBackgroundColor(ContextCompat.getColor(mainContext, color.orange))
                lySportRunning.setBackgroundColor(ContextCompat.getColor(mainContext, color.gray_medium))

                levelSelectedSport = levelRollerSkate
                totalsSelectedSport = totalsRollerSkate

                medalsListSportSelectedDistance = medalsListRollerSkateDistance
                medalsListSportSelectedAvgSpeed = medalsListRollerSkateAvgSpeed
                medalsListSportSelectedMaxSpeed = medalsListRollerSkateMaxSpeed
            }
            "Running"->{
                LIMIT_DISTANCE_ACCEPTED = LIMIT_DISTANCE_ACCEPTED_RUNNING

                lySportBike.setBackgroundColor(ContextCompat.getColor(mainContext, color.gray_medium))
                lySportRollerSkate.setBackgroundColor(ContextCompat.getColor(mainContext, color.gray_medium))
                lySportRunning.setBackgroundColor(ContextCompat.getColor(mainContext, color.orange))

                levelSelectedSport = levelRunning
                totalsSelectedSport = totalsRunning

                medalsListSportSelectedDistance = medalsListRunningDistance
                medalsListSportSelectedAvgSpeed = medalsListRunningAvgSpeed
                medalsListSportSelectedMaxSpeed = medalsListRunningMaxSpeed
            }
        }
        refreshCBSsSport()
        refreshRecords()
    }
    private fun refreshCBSsSport(){
        csbRecordDistance.max = totalsSelectedSport.recordDistance?.toFloat()!!
        csbRecordDistance.progress = totalsSelectedSport.recordDistance?.toFloat()!!

        csbRecordAvgSpeed.max = totalsSelectedSport.recordAvgSpeed?.toFloat()!!
        csbRecordAvgSpeed.progress = totalsSelectedSport.recordAvgSpeed?.toFloat()!!

        csbRecordSpeed.max = totalsSelectedSport.recordSpeed?.toFloat()!!
        csbRecordSpeed.progress = totalsSelectedSport.recordSpeed?.toFloat()!!

        csbCurrentDistance.max = csbRecordDistance.max
        csbCurrentAvgSpeed.max = csbRecordAvgSpeed.max
        csbCurrentSpeed.max = csbRecordSpeed.max
        csbCurrentMaxSpeed.max = csbRecordSpeed.max
        csbCurrentMaxSpeed.progress = 0f
    }
    private fun refreshRecords(){
        if (totalsSelectedSport.recordDistance!! > 0)
            tvDistanceRecord.text = totalsSelectedSport.recordDistance.toString()
        else
            tvDistanceRecord.text = ""
        if (totalsSelectedSport.recordAvgSpeed!! > 0)
            tvAvgSpeedRecord.text = totalsSelectedSport.recordAvgSpeed.toString()
        else
            tvAvgSpeedRecord.text = ""
        if (totalsSelectedSport.recordSpeed!! > 0)
            tvMaxSpeedRecord.text = totalsSelectedSport.recordSpeed.toString()
        else
            tvMaxSpeedRecord.text = ""
    }
    private fun updateTotalsUser(){
        totalsSelectedSport.totalRuns = totalsSelectedSport.totalRuns!! + 1
        totalsSelectedSport.totalDistance = totalsSelectedSport.totalDistance!! + distance
        totalsSelectedSport.totalTime = totalsSelectedSport.totalTime!! + timeInSeconds.toInt()

        if (distance > totalsSelectedSport.recordDistance!!){
            totalsSelectedSport.recordDistance = distance
        }
        if (maxSpeed > totalsSelectedSport.recordSpeed!!){
            totalsSelectedSport.recordSpeed = maxSpeed
        }
        if (avgSpeed > totalsSelectedSport.recordAvgSpeed!!){
            totalsSelectedSport.recordAvgSpeed = avgSpeed
        }

        totalsSelectedSport.totalDistance = roundNumber(totalsSelectedSport.totalDistance.toString(),1).toDouble()
        totalsSelectedSport.recordDistance = roundNumber(totalsSelectedSport.recordDistance.toString(),1).toDouble()
        totalsSelectedSport.recordSpeed = roundNumber(totalsSelectedSport.recordSpeed.toString(),1).toDouble()
        totalsSelectedSport.recordAvgSpeed = roundNumber(totalsSelectedSport.recordAvgSpeed.toString(),1).toDouble()

        var collection = "totals$sportSelected"
        var dbUpdateTotals = FirebaseFirestore.getInstance()
        dbUpdateTotals.collection(collection).document(usermail)
            .update("recordAvgSpeed", totalsSelectedSport.recordAvgSpeed)
        dbUpdateTotals.collection(collection).document(usermail)
            .update("recordDistance", totalsSelectedSport.recordDistance)
        dbUpdateTotals.collection(collection).document(usermail)
            .update("recordSpeed", totalsSelectedSport.recordSpeed)
        dbUpdateTotals.collection(collection).document(usermail)
            .update("totalDistance", totalsSelectedSport.totalDistance)
        dbUpdateTotals.collection(collection).document(usermail)
            .update("totalRuns", totalsSelectedSport.totalRuns)
        dbUpdateTotals.collection(collection).document(usermail)
            .update("totalTime", totalsSelectedSport.totalTime)

        when (sportSelected){
            "Bike" -> {
                totalsBike = totalsSelectedSport
                medalsListBikeDistance = medalsListSportSelectedDistance
                medalsListBikeAvgSpeed = medalsListSportSelectedAvgSpeed
                medalsListBikeMaxSpeed = medalsListSportSelectedMaxSpeed
            }
            "RollerSkate" -> {
                totalsRollerSkate = totalsSelectedSport
                medalsListRollerSkateDistance = medalsListSportSelectedDistance
                medalsListRollerSkateAvgSpeed = medalsListSportSelectedAvgSpeed
                medalsListRollerSkateMaxSpeed = medalsListSportSelectedMaxSpeed
            }
            "Running" -> {
                totalsRunning = totalsSelectedSport
                medalsListRunningDistance = medalsListSportSelectedDistance
                medalsListRunningAvgSpeed = medalsListSportSelectedAvgSpeed
                medalsListRunningMaxSpeed = medalsListSportSelectedMaxSpeed
            }
        }
    }
    fun startOrStopButtonClicked(view: View){
        manageStarStop()
    }
    private fun manageStarStop(){
        if (timeInSeconds == 0L && islocationEnabled() == false){
            AlertDialog.Builder(this)
                .setTitle(getString(string.alertActivationGPSTitle))
                .setMessage(getString(string.alertActivationGPSDescription))
                .setPositiveButton(
                    string.aceptActivationGPS,
                    DialogInterface.OnClickListener{dialog, which ->
                        activationLocation()
                    })
                .setNegativeButton(
                    string.ignoreActivationGPS,
                    DialogInterface.OnClickListener{dialog, which ->
                        activatedGPS = false
                        manageRun()
                    })
                .setCancelable(true)
                .show()
        }
        else manageRun()
    }
    private fun manageRun(){

        if (timeInSeconds.toInt() ==0){

            dateRun = SimpleDateFormat("yyyy/MM/dd").format(Date())
            startTimeRun = SimpleDateFormat("HH:mm:ss").format(Date())

            fbCamara.isVisible = true

            swIntervalMode.isClickable = false
            npDurationInterval.isEnabled = false
            csbRunWalk.isEnabled = false

            swChallenges.isClickable = false
            npChallengeDistance.isEnabled = false
            npChallengeDurationHH.isEnabled = false
            npChallengeDurationMM.isEnabled = false
            npChallengeDurationSS.isEnabled = false

            tvChrono.setTextColor(ContextCompat.getColor(this, color.chrono_running))

            sbHardTrack.isEnabled = true
            sbSoftTrack.isEnabled = true

            mpHard?.start()

            if (activatedGPS){
                flagSavedLocation = false
                manageLocation()
                flagSavedLocation = true
                manageLocation()
            }
        }

        if(!startButtonClicked){
            startButtonClicked = true
            starTime()
            manageEnableButtonsRun(false, true)

            if (hardTime) mpHard?.start()
            else mpSoft?.start()
            /*
            if (tvChrono.getCurrentTextColor() == ContextCompat.getColor(this, R.color.chrono_running))
                mpHard?.start()
            if (tvChrono.getCurrentTextColor() == ContextCompat.getColor(this, R.color.chrono_walking))
                mpSoft?.start()*/

        }
        else{
            startButtonClicked = false
            stoptime()
            manageEnableButtonsRun(true, true)

            if (hardTime) mpHard?.pause()
            else mpSoft?.pause()
            /*
            if (tvChrono.getCurrentTextColor() == ContextCompat.getColor(this, R.color.chrono_running))
                mpHard?.pause()
            if (tvChrono.getCurrentTextColor()  == ContextCompat.getColor(this, R.color.chrono_walking))
                mpSoft?.pause()*/
        }
    }
    private fun manageEnableButtonsRun(e_reset: Boolean, e_run: Boolean){
        val tvReset = findViewById<TextView>(id.tvReset)
        val btStart = findViewById<LinearLayout>(id.btStart)
        val btStartLabel = findViewById<TextView>(id.btStartLabel)
        tvReset.isEnabled = e_reset
        btStart.isEnabled = e_run

        if(e_reset){
            tvReset.setBackgroundColor(ContextCompat.getColor(this, color.green))
            animateViewofFloat(tvReset, "translationY", 0f, 500)
        }
        else{
            tvReset.setBackgroundColor(ContextCompat.getColor(this, color.gray))
            animateViewofFloat(tvReset, "translationY", 150f, 500)
        }

        if(e_run) {
            if (startButtonClicked) {
                btStart.background = getDrawable(drawable.circle_background_topause)
                btStartLabel.setText(string.stop)
            }
            else{
                btStart.background = getDrawable(drawable.circle_background_toplay)
                btStartLabel.setText(string.start)
            }
        }
        else btStart.background = getDrawable(drawable.circle_background_todisable)

    }
    private fun starTime(){
        mHandler = Handler(Looper.getMainLooper())
        chronometer.run()
    }
    private fun stoptime(){
        mHandler?.removeCallbacks(chronometer)
    }

    private var chronometer: Runnable = object : Runnable{
        override fun run() {
            try {

                if(mpHard!!.isPlaying){
                    val sbHardTrack: SeekBar = findViewById(id.sbHardTrack)
                    sbHardTrack.progress = mpHard!!.currentPosition
                }
                if(mpSoft!!.isPlaying) {
                    val sbSoftTrack: SeekBar = findViewById(id.sbSoftTrack)
                    sbSoftTrack.progress = mpSoft!!.currentPosition
                }
                updateTimesTrack(true, true)

                if (activatedGPS && timeInSeconds.toInt() % INTERVAL_LOCATION == 0) manageLocation()

                if (swIntervalMode.isChecked){
                    checkStopRun(timeInSeconds)
                    checkNewRound(timeInSeconds)
                }
                timeInSeconds += 1
                updateStopWatchView()
            }finally {
                mHandler!!.postDelayed(this, mInterval.toLong())
            }
        }
    }
    private fun updateStopWatchView(){
        tvChrono.text = getFormattedStopWatch(timeInSeconds * 1000)
    }
    private fun resetClicked(){
        savePreferences()
        saveDataRun()
        updateTotalsUser()
        setLevelSport(sportSelected)

        showPopUp()

        resetTimeView()
        resetInterface()
        resetMedals()
    }
    private fun saveDataRun(){
        var id:String = usermail + dateRun + startTimeRun
        id = id.replace(":", "")
        id = id.replace("/", "")

        var saveDuration = tvChrono.text.toString()

        var saveDistance = roundNumber(distance.toString(),1)
        var saveAvgSpeed = roundNumber(avgSpeed.toString(),1)
        var saveMaxSpeed = roundNumber(maxSpeed.toString(),1)

        var centerLatitude = (minLatitude!! + maxLatitude!!) / 2
        var centerLongitude = (minLongitude!! + maxLongitude!!) / 2

        var medalDistance = "none"
        var medalAvgSpeed = "none"
        var medalMaxSpeed = "none"

        if (recDistanceGold) medalDistance = "gold"
        if (recDistanceSilver) medalDistance = "silver"
        if (recDistanceBronze) medalDistance = "bronze"

        if (recAvgSpeedGold) medalAvgSpeed = "gold"
        if (recAvgSpeedSilver) medalAvgSpeed = "silver"
        if (recAvgSpeedBronze) medalAvgSpeed = "bronze"

        if (recMaxSpeedGold) medalMaxSpeed = "gold"
        if (recMaxSpeedSilver) medalMaxSpeed = "silver"
        if (recMaxSpeedBronze) medalMaxSpeed = "bronze"

        var collection = "runs$sportSelected"
        var dbRun = FirebaseFirestore.getInstance()
        dbRun.collection(collection).document(id).set(hashMapOf(
            "user" to usermail,
            "date" to dateRun,
            "startTime" to startTimeRun,
            "sport" to sportSelected,
            "activatedGPS" to activatedGPS,
            "duration" to saveDuration,
            "distance" to saveDistance.toDouble(),
            "avgSpeed" to saveAvgSpeed.toDouble(),
            "maxSpeed" to saveMaxSpeed.toDouble(),
            "minAltitude" to minAltitude,
            "maxAltitude" to maxAltitude,
            "minLatitude" to minLatitude,
            "maxLatitude" to maxLatitude,
            "minLongitude" to minLongitude,
            "maxLongitude" to maxLongitude,
            "centerLatitude" to centerLatitude,
            "centerLongitude" to centerLongitude,
            "medalDistance" to medalDistance,
            "medalAvgSpeed" to medalAvgSpeed,
            "medalMaxSpeed" to medalMaxSpeed,
        ))

        if (swIntervalMode.isChecked){
            dbRun.collection(collection).document(id).update("intervalMode", true)
            dbRun.collection(collection).document(id).update("intervalDuration", npDurationInterval.value)
            dbRun.collection(collection).document(id).update("runningTime", tvRunningTime.text.toString())
            dbRun.collection(collection).document(id).update("walkingTime", tvWalkingTime.text.toString())
        }

        if (swChallenges.isChecked){
            if (challengeDistance > 0f)
                dbRun.collection(collection).document(id).update("challengeDistance", roundNumber(challengeDistance.toString(), 1).toDouble())
            if (challengeDuration > 0)
                dbRun.collection(collection).document(id).update("challengeDuration", getFormattedStopWatch(challengeDuration.toLong()))
        }
    }
    private fun resetVariablesRun(){

        timeInSeconds = 0
        rounds = 1
        hardTime = true

        distance = 0.0
        maxSpeed = 0.0
        avgSpeed = 0.0

        minAltitude = null
        maxAltitude = null
        minLatitude = null
        maxLatitude = null
        minLongitude = null
        maxLongitude = null

        (listPoints as ArrayList<LatLng>).clear()

        challengeDistance = 0f
        challengeDuration = 0

        activatedGPS = true
        flagSavedLocation = false

        initStopWatch()
    }
    private fun resetTimeView(){

        manageEnableButtonsRun(false, true)

        //val btStart: LinearLayout = findViewById(R.id.btStart)
        //btStart.background = getDrawable(R.drawable.circle_background_toplay)
        tvChrono.setTextColor(ContextCompat.getColor(this, color.white))

    }
    private fun resetInterface(){

        fbCamara.isVisible = false

        val tvCurrentDistance: TextView = findViewById(id.tvCurrentDistance)
        val tvCurrentAvgSpeed: TextView = findViewById(id.tvCurrentAvgSpeed)
        val tvCurrentSpeed: TextView = findViewById(id.tvCurrentSpeed)
        tvCurrentDistance.text = "0.0"
        tvCurrentAvgSpeed.text = "0.0"
        tvCurrentSpeed.text = "0.0"


        tvDistanceRecord.setTextColor(ContextCompat.getColor(this, color.gray_dark))
        tvAvgSpeedRecord.setTextColor(ContextCompat.getColor(this, color.gray_dark))
        tvMaxSpeedRecord.setTextColor(ContextCompat.getColor(this, color.gray_dark))

        csbCurrentDistance.progress = 0f
        csbCurrentAvgSpeed.progress = 0f
        csbCurrentSpeed.progress = 0f
        csbCurrentMaxSpeed.progress = 0f

        val tvRounds: TextView = findViewById(id.tvRounds) as TextView
        tvRounds.text = getString(string.rounds)

        val lyChronoProgressBg = findViewById<LinearLayout>(id.lyChronoProgressBg)
        val lyRoundProgressBg = findViewById<LinearLayout>(id.lyRoundProgressBg)
        lyChronoProgressBg.translationX = -widthAnimations.toFloat()
        lyRoundProgressBg.translationX = -widthAnimations.toFloat()

        swIntervalMode.isClickable = true
        npDurationInterval.isEnabled = true
        csbRunWalk.isEnabled = true
        inflateintervalMode()

        swChallenges.isClickable = true
        npChallengeDistance.isEnabled = true
        npChallengeDurationHH.isEnabled = true
        npChallengeDurationMM.isEnabled = true
        npChallengeDurationSS.isEnabled = true


        sbHardTrack.isEnabled = false
        sbSoftTrack.isEnabled = false

    }
    private fun uptadeProgressBarRound(secs: Long){
        var s = secs.toInt()
        while (s>=ROUND_INTERVAL) s-=ROUND_INTERVAL
        s++

        var lyRoundProgressBg = findViewById<LinearLayout>(id.lyRoundProgressBg)
        if (tvChrono.getCurrentTextColor() == ContextCompat.getColor(this, color.chrono_running)){

            var movement = -1 * (widthAnimations-(s*widthAnimations/TIME_RUNNING)).toFloat()
            animateViewofFloat(lyRoundProgressBg, "translationX", movement, 1000L)
        }
        if (tvChrono.getCurrentTextColor() == ContextCompat.getColor(this, color.chrono_walking)){
            s-= TIME_RUNNING
            var movement = -1 * (widthAnimations-(s*widthAnimations/(ROUND_INTERVAL-TIME_RUNNING))).toFloat()
            animateViewofFloat(lyRoundProgressBg, "translationX", movement, 1000L)

        }
    }
    private fun checkStopRun(Secs: Long){
        var secAux : Long = Secs
        while (secAux.toInt() > ROUND_INTERVAL) secAux -= ROUND_INTERVAL

        if (secAux.toInt() == TIME_RUNNING) {
            tvChrono.setTextColor(ContextCompat.getColor(this, color.chrono_walking))

            val lyRoundProgressBg = findViewById<LinearLayout>(id.lyRoundProgressBg)
            lyRoundProgressBg.setBackgroundColor(ContextCompat.getColor(this, color.chrono_walking))
            lyRoundProgressBg.translationX = -widthAnimations.toFloat()

            mpHard?.pause()
            notifySound()
            mpSoft?.start()

        }
        else uptadeProgressBarRound(Secs)
    }
    private fun checkNewRound(Secs: Long) {
        if (Secs.toInt() % ROUND_INTERVAL == 0 && Secs.toInt() > 0) {
            val tvRounds: TextView = findViewById(id.tvRounds) as TextView
            rounds++
            tvRounds.text = "Round $rounds"

            tvChrono.setTextColor(ContextCompat.getColor( this, color.chrono_running))
            val lyRoundProgressBg = findViewById<LinearLayout>(id.lyRoundProgressBg)
            lyRoundProgressBg.setBackgroundColor(ContextCompat.getColor(this, color.chrono_running))
            lyRoundProgressBg.translationX = -widthAnimations.toFloat()

            mpSoft?.pause()
            notifySound()
            mpHard?.start()

        }
        else uptadeProgressBarRound(Secs)
    }
    private fun showPopUp(){
        var rlMain = findViewById<RelativeLayout>(id.rlMain)
        rlMain.isEnabled = false

        lyPopupRun.isVisible = true

        var lyWindow = findViewById<LinearLayout>(id.lyWindow)
        ObjectAnimator.ofFloat(lyWindow, "translationX", 0f ).apply {
            duration = 200L
            start()
        }
        loadDataPopUp()
    }
    private fun loadDataPopUp(){
        showHeaderPopUp()
        showMedals()
        showDataRun()
    }
    private fun showHeaderPopUp(){
        var csbRunsLevel = findViewById<CircularSeekBar>(id.csbRunsLevel)
        var csbDistanceLevel = findViewById<CircularSeekBar>(id.csbDistanceLevel)
        var tvTotalRunsLevel = findViewById<TextView>(id.tvTotalRunsLevel)
        var tvTotalDistanceLevel = findViewById<TextView>(id.tvTotalDistanceLevel)


        var ivSportSelected = findViewById<ImageView>(id.ivSportSelected)
        var ivCurrentLevel = findViewById<ImageView>(id.ivCurrentLevel)
        var tvTotalDistance = findViewById<TextView>(id.tvTotalDistance)
        var tvTotalTime = findViewById<TextView>(id.tvTotalTime)

        when (sportSelected){
            "Bike" ->{
                levelSelectedSport = levelBike
                setLevelBike()
                ivSportSelected.setImageResource(mipmap.bike)
            }
            "RollerSkate" -> {
                levelSelectedSport = levelRollerSkate
                setLevelRollerSkate()
                ivSportSelected.setImageResource(mipmap.rollerskate)
            }
            "Running" -> {
                levelSelectedSport = levelRunning
                setLevelRunning()
                ivSportSelected.setImageResource(mipmap.running)
            }
        }

        var tvNumberLevel = findViewById<TextView>(id.tvNumberLevel)
        var levelText = "${getString(string.level)} ${levelSelectedSport.image!!.subSequence(6,7).toString()}"
        tvNumberLevel.text = levelText

        csbRunsLevel.max = levelSelectedSport.RunsTarget!!.toFloat()
        csbRunsLevel.progress = totalsSelectedSport.totalRuns!!.toFloat()
        if (totalsSelectedSport.totalRuns!! > levelSelectedSport.RunsTarget!!.toInt()){
            csbRunsLevel.max = levelSelectedSport.RunsTarget!!.toFloat()
            csbRunsLevel.progress = csbRunsLevel.max
        }

        csbDistanceLevel.max = levelSelectedSport.DistanceTarget!!.toFloat()
        csbDistanceLevel.progress = totalsSelectedSport.totalDistance!!.toFloat()
        if (totalsSelectedSport.totalDistance!! > levelSelectedSport.DistanceTarget!!.toInt()){
            csbDistanceLevel.max = levelSelectedSport.DistanceTarget!!.toFloat()
            csbDistanceLevel.progress = csbDistanceLevel.max
        }

        tvTotalRunsLevel.text = "${totalsSelectedSport.totalRuns!!}/${levelSelectedSport.RunsTarget!!}"

        var td = totalsSelectedSport.totalDistance!!
        var td_k: String = td.toString()
        if (td > 1000) td_k = (td/1000).toInt().toString() + "K"
        var ld = levelSelectedSport.DistanceTarget!!.toDouble()
        var ld_k: String = ld.toInt().toString()
        if (ld > 1000) ld_k = (ld/1000).toInt().toString() + "K"
        tvTotalDistance.text = "${td_k}/${ld_k} kms"

        var porcent = (totalsSelectedSport.totalDistance!!.toDouble() *100 / levelSelectedSport.DistanceTarget!!.toDouble()).toInt()
        tvTotalDistanceLevel.text = "$porcent%"

        when (levelSelectedSport.image){
            "level_1" -> ivCurrentLevel.setImageResource(drawable.level_1)
            "level_2" -> ivCurrentLevel.setImageResource(drawable.level_2)
            "level_3" -> ivCurrentLevel.setImageResource(drawable.level_3)
            "level_4" -> ivCurrentLevel.setImageResource(drawable.level_4)
            "level_5" -> ivCurrentLevel.setImageResource(drawable.level_5)
            "level_6" -> ivCurrentLevel.setImageResource(drawable.level_6)
            "level_7" -> ivCurrentLevel.setImageResource(drawable.level_7)
        }

        var formatedTime = getFormattedTotalTime(totalsSelectedSport.totalTime!!.toLong())
        tvTotalTime.text = getString(string.PopUpTotalTime) + formatedTime
    }
    private fun showMedals(){
        val ivMedalDistance = findViewById<ImageView>(id.ivMedalDistance)
        val ivMedalAvgSpeed = findViewById<ImageView>(id.ivMedalAvgSpeed)
        val ivMedalMaxSpeed = findViewById<ImageView>(id.ivMedalMaxSpeed)

        val tvMedalDistanceTitle = findViewById<TextView>(id.tvMedalDistanceTitle)
        val tvMedalAvgSpeedTitle = findViewById<TextView>(id.tvMedalAvgSpeedTitle)
        val tvMedalMaxSpeedTitle = findViewById<TextView>(id.tvMedalMaxSpeedTitle)

        if (recDistanceGold) ivMedalDistance.setImageResource(drawable.medalgold)
        if (recDistanceSilver) ivMedalDistance.setImageResource(drawable.medalsilver)
        if (recDistanceBronze) ivMedalDistance.setImageResource(drawable.medalbronze)
        if (recDistanceGold || recDistanceSilver || recDistanceBronze)
            tvMedalDistanceTitle.setText(string.medalDistanceDescription)

        if (recAvgSpeedGold) ivMedalAvgSpeed.setImageResource(drawable.medalgold)
        if (recAvgSpeedSilver) ivMedalAvgSpeed.setImageResource(drawable.medalsilver)
        if (recAvgSpeedBronze) ivMedalAvgSpeed.setImageResource(drawable.medalbronze)
        if (recAvgSpeedGold || recAvgSpeedSilver || recAvgSpeedBronze)
            tvMedalAvgSpeedTitle.setText(string.medalAvgSpeedDescription)

        if (recMaxSpeedGold) ivMedalMaxSpeed.setImageResource(drawable.medalgold)
        if (recMaxSpeedSilver) ivMedalMaxSpeed.setImageResource(drawable.medalsilver)
        if (recMaxSpeedBronze) ivMedalMaxSpeed.setImageResource(drawable.medalbronze)
        if (recMaxSpeedGold || recMaxSpeedSilver || recMaxSpeedBronze)
            tvMedalMaxSpeedTitle.setText(string.medalMaxSpeedDescription)
    }
    private fun showDataRun(){
        var tvDurationRun = findViewById<TextView>(id.tvDurationRun)
        var lyChallengeDurationRun = findViewById<LinearLayout>(id.lyChallengeDurationRun)
        var tvChallengeDurationRun = findViewById<TextView>(id.tvChallengeDurationRun)
        var lyIntervalRun = findViewById<LinearLayout>(id.lyIntervalRun)
        var tvIntervalRun = findViewById<TextView>(id.tvIntervalRun)

        var lyCurrentDistance = findViewById<LinearLayout>(id.lyCurrentDistance)
        var tvDistanceRun = findViewById<TextView>(id.tvDistanceRun)
        var lyChallengeDistancePopUp = findViewById<LinearLayout>(id.lyChallengeDistancePopUp)
        var tvChallengeDistanceRun = findViewById<TextView>(id.tvChallengeDistanceRun)
        var lyUnevennessRun = findViewById<LinearLayout>(id.lyUnevennessRun)
        var tvMaxUnevennessRun = findViewById<TextView>(id.tvMaxUnevennessRun)
        var tvMinUnevennessRun = findViewById<TextView>(id.tvMinUnevennessRun)

        var lyCurrentSpeeds = findViewById<LinearLayout>(id.lyCurrentSpeeds)
        var tvAvgSpeedRun = findViewById<TextView>(id.tvAvgSpeedRun)
        var tvMaxSpeedRun = findViewById<TextView>(id.tvMaxSpeedRun)

        //Duration
        tvDurationRun.setText(tvChrono.text)
        if (challengeDuration > 0){
            setHeightLinearLayout(lyChallengeDurationRun, 130)
            tvChallengeDurationRun.setText(getFormattedStopWatch((challengeDuration*1000).toLong()))
        }
        else  setHeightLinearLayout(lyChallengeDurationRun, 0)

        if (swIntervalMode.isChecked){
            setHeightLinearLayout(lyIntervalRun, 130)
            var details: String = "${npDurationInterval.value}mins. ("
            details += "${tvRunningTime.text} / ${tvWalkingTime.text})"

            tvIntervalRun.setText(details)
        }
        else setHeightLinearLayout(lyIntervalRun, 0)

        //Challenge PopUp with GPS Show Distance/Speed
        if (activatedGPS){
            //Distance
            tvDistanceRun.setText(roundNumber(distance.toString(), 2))

            if (challengeDistance > 0f){
                setHeightLinearLayout(lyChallengeDistancePopUp, 130)
                tvChallengeDistanceRun.setText(challengeDistance.toString())
            }
            else setHeightLinearLayout(lyChallengeDistancePopUp, 0)

            if (maxAltitude == null) setHeightLinearLayout(lyUnevennessRun, 0)
            else{
                setHeightLinearLayout(lyUnevennessRun, 130)
                tvMaxUnevennessRun.setText(maxAltitude!!.toInt().toString())
                tvMinUnevennessRun.setText(minAltitude!!.toInt().toString())
            }

            //Speed
            tvAvgSpeedRun.setText(roundNumber(avgSpeed.toString(), 1))
            tvMaxSpeedRun.setText(roundNumber(maxSpeed.toString(), 1))

        }
        //Challenge PopUp without GPS Hide Distance/Speed
        else{
            //Distance
            setHeightLinearLayout(lyCurrentDistance, 0)
            //Speed
            setHeightLinearLayout(lyCurrentSpeeds, 0)

        }

    }
    fun closePopUp(v: View){
        closePopUpRun()

    }
    private fun closePopUpRun(){
        hidePopUpRun()

        var rlMain = findViewById<RelativeLayout>(id.rlMain)
        rlMain.isEnabled = true

        resetVariablesRun()
        selectSport(sportSelected)
    }
    private fun hidePopUpRun(){
        var lyWindow = findViewById<LinearLayout>(id.lyWindow)
        lyWindow.translationX = 400f
        lyPopupRun = findViewById(id.lyPopupRun)
        lyPopupRun.isVisible = false
    }
}
