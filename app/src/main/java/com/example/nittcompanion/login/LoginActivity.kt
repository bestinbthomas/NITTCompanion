package com.example.nittcompanion.login

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.AdapterView
import android.widget.Spinner
import android.widget.Switch
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.nittcompanion.R
import com.example.nittcompanion.common.*
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : AppCompatActivity() {

    private val TAG = "LoginActivity"
    private lateinit var classView: View
    var dep: String? = null
    var year: String? = null
    var sec: String? = null
    var cr: Boolean = false


    //move to model
    lateinit var googleSignInClient: GoogleSignInClient
    lateinit var auth: FirebaseAuth
    val firestoreInstance = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
        setContentView(R.layout.activity_login)
        supportActionBar?.hide()
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)
        auth = FirebaseAuth.getInstance()
    }

    override fun onStart() {
        super.onStart()

        classView = layoutInflater.inflate(R.layout.class_layout, null)
        val departmentSpinner = classView.findViewById<Spinner>(R.id.DepartmentSpin)
        val yearSpinner = classView.findViewById<Spinner>(R.id.YearSpin)
        val sectionSpinner = classView.findViewById<Spinner>(R.id.SectionSpin)
        val crSwitch = classView.findViewById<Switch>(R.id.CrSwitch)



        departmentSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
                dep = null
            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                sectionSpinner.visibility = View.VISIBLE
                dep = when (position) {
                    0 -> null
                    1 -> "CSE"
                    2 -> "ECE"
                    3 -> "EEE"
                    4 -> "MECH"
                    5 -> "ICE"
                    6 -> "CIVIL"
                    7 -> {
                        sectionSpinner.visibility = View.GONE
                        sec = "A"
                        "MME"
                    }
                    8 -> "PROD"
                    9 -> {
                        sectionSpinner.visibility = View.GONE
                        sec = "A"
                        "CHEM"
                    }
                    else -> null
                }

                Log.e("Login","dep set to $dep")
            }

        }

        yearSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
                year = null
            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                year = when (position) {
                    0 -> null
                    1 -> "1"
                    2 -> "2"
                    3 -> "3"
                    4 -> "4"
                    else -> null
                }

                Log.e("Login","year set to $year")
            }
        }

        sectionSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                sec = when (position) {
                    0 -> null
                    1 -> "A"
                    2 -> "B"
                    else -> null
                }
                Log.e("Login","sec set to $sec")
            }
        }
        LoginBtn.setOnClickListener {
            val diaog = AlertDialog.Builder(this)
                .setView(classView)
                .setPositiveButton("Login") { _,_->}
                .setNegativeButton("Cancell"){_,_->}
                .create()
            diaog.setOnShowListener {
                val btn = diaog.getButton(AlertDialog.BUTTON_POSITIVE)
                btn.setOnClickListener {
                    when {
                        dep == null -> {
                            createSnackbar("Select Department",Snackbar.LENGTH_SHORT)
                        }
                        year == null -> {
                            createSnackbar("Select Year",Snackbar.LENGTH_SHORT)
                        }
                        sec == null -> {
                            createSnackbar("Select Section",Snackbar.LENGTH_SHORT)
                        }
                        else -> {
                            cr = crSwitch.isChecked
                            signIn()
                            diaog.dismiss()
                        }
                    }
                }
            }
            diaog.show()

        }
    }

    private fun signIn() {
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                // Google Sign In was successful, authenticate with Firebase
                val account = task.getResult(ApiException::class.java)
                firebaseAuthWithGoogle(account!!)
            } catch (e: ApiException) {
                // Google Sign In failed, update UI appropriately
                createSnackbar("Login Failed ", Snackbar.LENGTH_INDEFINITE, "RETRY") { signIn() }
                Log.w(TAG, "Google sign in failed", e)
                // ...
            }
        }
    }

    private fun firebaseAuthWithGoogle(acct: GoogleSignInAccount) {
        Log.d(TAG, "firebaseAuthWithGoogle:" + acct.id!!)

        val credential = GoogleAuthProvider.getCredential(acct.idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    val intent = Intent(this, MainActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK and Intent.FLAG_ACTIVITY_CLEAR_TASK)

                    addUserData()
                    startActivity(intent)
                    Log.d(TAG, "signInWithCredential:success")
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w(TAG, "signInWithCredential:failure", task.exception)
                    createSnackbar("Authentication failed ", Snackbar.LENGTH_SHORT)
                }

            }
    }

    private fun addUserData() {
        val userData = hashMapOf<String, Any>(
            "class" to "${dep}_${year}_$sec",
            "cr" to cr
        )

        val sharedPreference = getSharedPreferences(SHARED_PREF, Context.MODE_PRIVATE)
        val editor  = sharedPreference.edit()
        editor.putString(KEY_CLASS,"${dep}_${year}_$sec")
        editor.putBoolean(KEY_CR,cr)
        editor.apply()
        firestoreInstance.collection("user").document(auth.currentUser!!.uid).set(userData)
            .addOnSuccessListener { Log.e("Login","setting user data success") }
            .addOnFailureListener{ Log.e("Login","setting user data failed",it) }

    }


}


