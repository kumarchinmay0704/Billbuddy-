package com.example.billbudddy

data class UserData(
    val id: String? = null,
    val name: String? = null,
    val email: String? = null,
    val password: String? = null
) {
    constructor() : this(null, null, null, null)
}
