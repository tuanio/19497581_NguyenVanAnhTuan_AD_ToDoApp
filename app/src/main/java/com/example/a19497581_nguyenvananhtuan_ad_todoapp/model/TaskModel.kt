package com.example.a19497581_nguyenvananhtuan_ad_todoapp.model

import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class TaskModel(
    val id: String? = null,
    val data: TaskItemModel? = null
)
