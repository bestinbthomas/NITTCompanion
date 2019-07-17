package com.example.nittcompanion.notes

import android.net.Uri
import com.example.nittcompanion.common.NOTE_UPLOADED_SUCESS
import com.google.firebase.firestore.Exclude

data class Note(var name : String = "",
                var Link : String = "",
                @set:Exclude @get:Exclude var added:Int = NOTE_UPLOADED_SUCESS,
                @set:Exclude @get:Exclude var uri: Uri? = null,
                @set:Exclude @get:Exclude var iD : String = "")