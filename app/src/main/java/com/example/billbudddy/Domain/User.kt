package com.example.billbudddy.Domain

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class User(
    var uid: String = "",
    val name: String = "",
    val email: String = "",
    val photoUrl: String = ""
) : Parcelable {

    constructor() : this("", "", "", "")
} 