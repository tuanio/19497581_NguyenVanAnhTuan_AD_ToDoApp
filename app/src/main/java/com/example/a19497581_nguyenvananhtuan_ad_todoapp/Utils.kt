package com.example.a19497581_nguyenvananhtuan_ad_todoapp

object Utils {

    fun getStatus(status: Boolean) : String {
        return when (status) {
            true -> "Completed"
            else -> "Not Completed"
        }
    }

}