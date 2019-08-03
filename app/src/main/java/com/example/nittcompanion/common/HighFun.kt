package com.example.nittcompanion.common

import com.example.nittcompanion.common.objects.Attendence
import com.example.nittcompanion.common.objects.Course
import com.example.nittcompanion.common.objects.FireCourse
import com.example.nittcompanion.model.User
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

internal suspend fun <T> awaitTaskCompletable(task: Task<T>): Unit = suspendCoroutine { continuation ->
    task.addOnCompleteListener { task ->
        if(task.isSuccessful){
            continuation.resume(Unit)
        }else{
            continuation.resumeWithException(task.exception!!)
        }
    }
}

internal suspend fun <T> awaitTaskResult(task: Task<T>): T = suspendCoroutine { continuation ->
    task.addOnCompleteListener { task ->
        if(task.isSuccessful){
            continuation.resume(task.result!!)
        }else{
            continuation.resumeWithException(task.exception!!)
        }
    }
}

internal fun logOutUser() = FirebaseAuth.getInstance().signOut()

fun getCurrentUser() = when (val fireUser = FirebaseAuth.getInstance().currentUser) {
    null -> Result.build { null }
    else -> Result.build {
        User(
            fireUser.uid,
            fireUser.displayName ?: "",
            fireUser.email?:"",
            fireUser.photoUrl
        )
    }
}

fun fireCoursetoCourse(fireCourse: FireCourse,attendence: Attendence) : Course = Course(fireCourse.name,fireCourse.credit,fireCourse.classEvent,fireCourse.ID,fireCourse.lastEventCreated,attendence.attended,attendence.notAttended)