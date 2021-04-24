package com.example.blaze.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.blaze.models.BlazeUser
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import androidx.navigation.NavController
import androidx.navigation.findNavController
import com.example.blaze.R
import com.example.blaze.ui.login.LoginFragmentDirections

class MainActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        auth = Firebase.auth
        val currentUser = auth.currentUser

        navController = findNavController(R.id.navHostFragment)

        if(currentUser != null){
            val user = BlazeUser(currentUser.uid, currentUser.email!!.toString(),
                    currentUser.displayName!!.toString(), currentUser.photoUrl!!.toString())

            if(navController.currentDestination?.label.toString().contains("login")){
                val action = LoginFragmentDirections.actionLoginFragmentToChannelFragment(user)
                navController.navigate(action)
            }
        }
    }
}