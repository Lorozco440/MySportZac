package com.umg.mysportzac

import android.annotation.SuppressLint
import android.app.DownloadManager
import android.content.ContentValues
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.Query
import com.google.firebase.firestore.FirebaseFirestore
import com.umg.mysportzac.LoginActivity.Companion.usermail
import com.umg.mysportzac.MainActivity.Companion.mainContext

class RecordActivity : AppCompatActivity() {

    private var sportSelected : String = "Running"

    /*
    private lateinit var ivBike : ImageView
    private lateinit var ivRollerSkate: ImageView
    private lateinit var ivRunning: ImageView
    */

    private lateinit var recyclerView: RecyclerView
    private lateinit var runsArrayList : ArrayList<Runs>
    private lateinit var myAdapter: RunsAdapter

    @SuppressLint("SuspiciousIndentation")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_record)

    val toolbar: androidx.appcompat.widget.Toolbar = findViewById(R.id.toolbar_record)
        setSupportActionBar(toolbar)

        toolbar.title = getString(R.string.bar_title_record)
        toolbar.setTitleTextColor(ContextCompat.getColor(this, R.color.gray_dark))
        toolbar.setBackgroundColor(ContextCompat.getColor(this, R.color.gray_light))

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeButtonEnabled(true)

        recyclerView = findViewById(R.id.rvRecords)
        recyclerView.layoutManager = LinearLayoutManager (this)
        recyclerView.setHasFixedSize(true)

        runsArrayList = arrayListOf()
        myAdapter = RunsAdapter(runsArrayList)
        recyclerView.adapter = myAdapter


    }

    override fun onResume() {
        super.onResume()
        loadRecyclerView("date", com.google.firebase.firestore.Query.Direction.DESCENDING)
    }
    override fun onPause() {
        super.onPause()
        runsArrayList.clear()
    }
    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
       // return super.onSupportNavigateUp()
        return true//super.onSupportNavigateUp()
    }
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.order_records_by, menu)
        return true //super.onCreateOptionsMenu(menu)
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        var order: com.google.firebase.firestore.Query.Direction = com.google.firebase.firestore.Query.Direction.DESCENDING

        when (item. itemId) {
            R.id.orderby_date -> {
                if (item.title == getString(R.string.orderby_dateZA)) {
                    item.title = getString(R.string.orderby_dateAZ)
                    order = com.google.firebase.firestore.Query.Direction.DESCENDING
                } else {
                    item.title = getString(R.string.orderby_dateZA)
                    order = com.google.firebase.firestore.Query.Direction.ASCENDING
                }
                loadRecyclerView("date", order)
                return true
            }

            R.id.orderby_duration -> {
                var option = getString(R.string.orderby_durationZA)
                if (item.title == getString(R.string.orderby_durationZA)) {
                    item.title = getString(R.string.orderby_durationAZ)
                    order = com.google.firebase.firestore.Query.Direction.DESCENDING
                } else {
                    item.title = getString(R.string.orderby_durationZA)
                    order = com.google.firebase.firestore.Query.Direction.ASCENDING
                }
                loadRecyclerView("duration", order)
                return true
            }

            R.id.orderby_distance -> {
                var option = getString(R.string.orderby_distanceZA)
                if (item.title == option) {
                    item.title = getString(R.string.orderby_distanceAZ)
                    order = com.google.firebase.firestore.Query.Direction.ASCENDING
                } else {
                    item.title = getString(R.string.orderby_distanceZA)
                    order = com.google.firebase.firestore.Query.Direction.DESCENDING
                }
                loadRecyclerView("distance", order)
                return true
            }

            R.id.orderby_avgspeed -> {
                var option = getString(R.string.orderby_avqspeedZA)
                if (item.title == getString(R.string.orderby_avqspeedZA)) {
                    item.title = getString(R.string.orderby_avgspeedAZ)
                    order = com.google.firebase.firestore.Query.Direction.ASCENDING
                } else {
                    item.title = getString(R.string.orderby_avqspeedZA)
                    order = com.google.firebase.firestore.Query.Direction.DESCENDING
                }
                loadRecyclerView("avgSpeed", order)
                return true
            }

            /*R.id.orderby_avgspeed -> {
                var option = getString(R.string.orderby_avqspeedZA)
                if (item.title == getString(R.string.orderby_avqspeedZA)) {
                    item.title = getString(R.string.orderby_avgspeedAZ)
                } else {
                    item.title = getString(R.string.orderby_avqspeedZA)
                }
                return true
            }
             */

            R.id.orderby_maxspeed -> {
                var option = getString(R.string.orderby_maxspeedZA)
                if (item.title == getString(R.string.orderby_maxspeedZA)) {
                    item.title = getString(R.string.orderby_maxspeedAZ)
                    order = com.google.firebase.firestore.Query.Direction.ASCENDING
                } else {
                    item.title = getString(R.string.orderby_maxspeedZA)
                    order = com.google.firebase.firestore.Query.Direction.DESCENDING
                }
                loadRecyclerView("maxSpeed", order)
                return true
            }
        }
        return super.onOptionsItemSelected(item)
        }
    private fun loadRecyclerView(field: String, order: com.google.firebase.firestore.Query.Direction) {

        runsArrayList.clear()

        var dbRuns = FirebaseFirestore.getInstance()
        dbRuns.collection("runs$sportSelected").orderBy(field, order)
            .whereEqualTo("user", usermail)
            .get()
            .addOnSuccessListener { documents ->
                for (run in documents)
                    runsArrayList.add(run.toObject(Runs::class.java))

                myAdapter.notifyDataSetChanged()
            }
            .addOnFailureListener { exception ->
                Log.w(ContentValues.TAG, "Error getting documents WHERE EQUAL TO: ", exception)
            }
    }

    fun loadRunsBike(v: View){
        sportSelected = "Bike"
        /*
        ivBike.setBackgroundColor(ContextCompat.getColor(mainContext, R.color.orange))
        ivRollerSkate.setBackgroundColor(ContextCompat.getColor(mainContext, R.color.gray_medium))
        ivRunning.setBackgroundColor(ContextCompat.getColor(mainContext, R.color.gray_medium))
        */

        loadRecyclerView("date", com.google.firebase.firestore.Query.Direction.DESCENDING)
    }
    fun loadRunsRollerSkate(v: View){
        sportSelected = "RollerSkate"
        /*
        ivBike.setBackgroundColor(ContextCompat.getColor(mainContext, R.color.gray_medium))
        ivRollerSkate.setBackgroundColor(ContextCompat.getColor(mainContext, R.color.orange))
        ivRunning.setBackgroundColor(ContextCompat.getColor(mainContext, R.color.gray_medium))
         */
        loadRecyclerView("date", com.google.firebase.firestore.Query.Direction.DESCENDING)
    }
    fun loadRunsRunning(v: View){
        sportSelected = "Running"
        /*
        ivBike.setBackgroundColor(ContextCompat.getColor(mainContext, R.color.gray_medium))
        ivRollerSkate.setBackgroundColor(ContextCompat.getColor(mainContext, R.color.gray_medium))
        ivRunning.setBackgroundColor(ContextCompat.getColor(mainContext, R.color.orange))
         */

        loadRecyclerView("date", com.google.firebase.firestore.Query.Direction.DESCENDING)
    }
    fun callHome(v: View){
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

}






























