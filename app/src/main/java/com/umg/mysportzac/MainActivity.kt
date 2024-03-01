package com.umg.mysportzac

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.facebook.login.LoginManager
import com.google.firebase.auth.FirebaseAuth
import com.umg.mysportzac.LoginActivity.Companion.usermail
import com.umg.mysportzac.LoginActivity.Companion.providerSession

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        Toast.makeText(this, "Hola $usermail", Toast.LENGTH_SHORT).show()
    }
    fun cerrarSesion(view: View) {
     cerrar()
    }
    private fun cerrar(){
        usermail = " "

        if(providerSession == "Facebook") LoginManager.getInstance().logOut()
        else
        FirebaseAuth.getInstance().signOut()
        startActivity(Intent(this, LoginActivity::class.java))

    }
}