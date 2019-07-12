package com.example.nittcompanion.common

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

internal fun logOutUser() {
    FirebaseAuth.getInstance().signOut()
}

fun getCurrentUser(): Result<Exception, User?> {
    val fireUser = FirebaseAuth.getInstance().currentUser
    return if (fireUser == null)
        Result.build { null }
    else Result.build {
        User(
            fireUser.uid,
            fireUser.displayName ?: ""
        )
    }
}