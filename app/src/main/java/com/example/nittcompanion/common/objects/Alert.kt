package com.example.nittcompanion.common.objects

import com.google.firebase.firestore.Exclude

data class Alert(var timeinmillis : Long, @set:Exclude @get:Exclude var eventId : String)