package edu.rutgers.knighttransporter.for_transloc

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Alert(
    val title: String,
    val guid: String,
    val pubDate: String,
    val description: String,
) : Parcelable
