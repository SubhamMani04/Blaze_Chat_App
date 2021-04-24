package com.example.blaze.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class BlazeUser(
        val uid : String,
        val email : String,
        val username : String,
        val displayPhotoUrl : String
) : Parcelable