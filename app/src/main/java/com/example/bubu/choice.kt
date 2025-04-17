package com.example.bubu

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth


class choice : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_choice)

        findViewById<Button>(R.id.general).setOnClickListener {
            startActivity(Intent(this,dashboard::class.java))
        }

        findViewById<Button>(R.id.gyro).setOnClickListener {
            startActivity(Intent(this,selectCSV::class.java))
        }
        findViewById<Button>(R.id.ml).setOnClickListener {
            val intent = Intent(this, MLSelectCSV::class.java)
            startActivity(intent)
        }




        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("845748176892-16n2fttmjlbroonpfng6p8738luhln1n.apps.googleusercontent.com")
            .requestEmail()
            .build()

        findViewById<ImageView>(R.id.signout).setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            GoogleSignIn.getClient(this, gso).signOut().addOnCompleteListener {
                // Optionally, update the UI to reflect the sign-out state
                startActivity(Intent(this,MainActivity::class.java))
                finish()
            }

        }
    }
}