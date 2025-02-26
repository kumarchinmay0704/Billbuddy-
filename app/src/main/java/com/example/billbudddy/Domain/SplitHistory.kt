package com.example.billbudddy.Domain

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class SplitHistory(
    val title: String = "",
    val type: String = "",
    val date: Long = 0,
    val amount: Float = 0f,
    val participants: List<User> = listOf(),
    val amounts: Map<String, Float> = mapOf(),
    val splitId: String = ""
) : Parcelable {

    constructor() : this("", "", 0, 0f, listOf(), mapOf(), "")
} 