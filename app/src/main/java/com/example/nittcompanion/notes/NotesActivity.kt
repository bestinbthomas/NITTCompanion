package com.example.nittcompanion.notes

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.OpenableColumns
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.Observer
import androidx.navigation.navArgs
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.nittcompanion.BuildConfig
import com.example.nittcompanion.R
import com.example.nittcompanion.common.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.android.synthetic.main.activity_notes.*
import java.io.File


class NotesActivity : AppCompatActivity() {
    private lateinit var adapter: NotesRecyclerAdapter
    private lateinit var coursename: String
    private lateinit var courseid: String
    private val notesList: MutableList<Note> = mutableListOf()
    private val args : NotesActivityArgs by navArgs()
    private val user = FirebaseAuth.getInstance().currentUser
    private val storageRootReference = FirebaseStorage.getInstance().reference.child("class")
    private lateinit var storageReference : StorageReference
    private val firestoreRootReference = FirebaseFirestore.getInstance().collection("class")
    private lateinit var firestoreReference : CollectionReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notes)
        title = "Notes"
        setupRecycler()

    }

    private fun setupRecycler() {
        adapter = NotesRecyclerAdapter(this, notesList)
        adapter.noteSelectLiveData.observe(
            this,
            Observer {

                if (notesList[it].uri != null) {
                    uploadFile(notesList[it].uri!!, notesList[it].name)
                }
                if (notesList[it].Link.isNotEmpty())
                    openNote(notesList[it])
            }
        )
        adapter.noteLongPressLiveData.observe(
            this,
            Observer {
                AlertDialog.Builder(this)
                    .setTitle("Confirm Delete")
                    .setMessage("Do you want to delete ${notesList[it].name}")
                    .setPositiveButton("Yes"){ _,_ ->
                        deleteNote(notesList[it])
                    }
                    .setNegativeButton("No"){ _,_ ->}
                    .create()
                    .show()
            }
        )
        NotesListRec.adapter = adapter
        NotesListRec.itemAnimator = DefaultItemAnimator()
        NotesListRec.layoutManager = LinearLayoutManager(this)
    }

    private fun deleteNote(note: Note) {
        notesList.remove(note)
        adapter.notifyDataSetChanged()
        if(getSharedPreferences(SHARED_PREF, Context.MODE_PRIVATE).getBoolean(KEY_CR,false)) {
            firestoreReference.document(courseid).collection(FIREBASE_COLLECTION_NOTES).document(note.iD).delete()
                .addOnFailureListener {
                    Log.e("NOtes","",it)
                }
                .addOnSuccessListener {
                    Log.e("NOtes","successfully deleted")
                }
            storageReference.child(courseid).child(note.name).delete()
        }

        val state = Environment.getExternalStorageState()
        if (Environment.MEDIA_MOUNTED == state) {
            val root = Environment.getExternalStorageDirectory()
            val dir = File(root.absolutePath + "/" + Environment.DIRECTORY_DOCUMENTS + FILE_DIR_EXTENTION)
            if (!dir.exists()) dir.mkdir()

            val file = File(dir, note.name)
            if(file.exists()){
                file.delete()
            }
        }
    }

    override fun onStart() {
        super.onStart()

        val sharedPreference = getSharedPreferences(SHARED_PREF, Context.MODE_PRIVATE)
        val mClass = sharedPreference.getString(KEY_CLASS,"")
        storageReference = storageRootReference.child(mClass!!)
        firestoreReference = firestoreRootReference.document(mClass).collection(FIREBASE_COLLECTION_NOTES)

        courseid = args.courseID
        coursename = args.courseName

        NotesHead.text = coursename

        fetchList()

        addFilesFAB.setOnClickListener {
            requestFile()
            Log.d("Notes", "add File Called")
        }
    }

    private fun requestFile() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            openFileChooser()
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                REQ_STORAGE_PERMISSION
            )
        }

    }


    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode == REQ_STORAGE_PERMISSION && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            openFileChooser()
        }
    }

    private fun openFileChooser() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "*/*"
        if (intent.resolveActivity(packageManager) != null) {
            startActivityForResult(intent, REQ_LOAD_FILE)
            Log.d("Notes", "add activity result called")
        } else
            Log.d("Notes", "add intent not resolved")
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQ_LOAD_FILE && resultCode == Activity.RESULT_OK && data != null) {
            if (data.data != null) {
                val file: Uri = data.data!!
                val fileName = getFileName(file)
                uploadFile(file, fileName)
            }
        }
    }

    private fun uploadFile(file: Uri, fileName: String) {
        if (notesList.none { it.name == fileName }) {
            notesList.add(Note(name = fileName, added = NOTE_UNLOADING, uri = file))
            adapter.notifyDataSetChanged()
            storageReference.child(courseid).child(fileName).putFile(file)
                .addOnSuccessListener {
                    //uploadcomplete
                    Log.e("Notes", "file $fileName uploaded")
                    storageReference.child(courseid).child(fileName).downloadUrl
                        .addOnSuccessListener {
                            val url = it.toString()
                            val note = notesList.filter { note ->
                                note.name == fileName
                            }
                            if (note.isNotEmpty()) {
                                val index = notesList.indexOf(note[0])
                                notesList.removeAt(index)
                                notesList.add(index, Note(fileName, url, NOTE_UPLOADED_SUCESS))
                            } else notesList.add(Note(fileName, url, NOTE_UPLOADED_SUCESS))
                            adapter.notifyDataSetChanged()
                            addtoFirestore(fileName, url)
                        }

                }
                .addOnFailureListener {
                    Log.e("Notes", "failed to uplpoad note",it)
                    val note = notesList.filter { note ->
                        note.name == fileName
                    }
                    if (note.isNotEmpty()) {
                        val index = notesList.indexOf(note[0])
                        notesList.removeAt(index)
                        notesList.add(index, Note(name = fileName, added = NOTE_UPLOADED_FAIL, uri = note[0].uri))
                    } else notesList.add(Note(name = fileName, added = NOTE_UPLOADED_FAIL, uri = note[0].uri))
                    adapter.notifyDataSetChanged()
                }
        }

    }

    private fun addtoFirestore(name: String, link: String) {
        firestoreReference.document(courseid).collection(FIREBASE_COLLECTION_NOTES).add(Note(name, link))
            .addOnSuccessListener { Docref ->
                val note = notesList.filter {
                    it.name == name
                }
                if (note.isNotEmpty()) {
                    val index = notesList.indexOf(note[0])
                    notesList[index].iD = Docref.id
                    adapter.notifyDataSetChanged()
                }

                Log.e("Notes", "data uploaded")
            }
            .addOnFailureListener {
                Log.e("Notes", "data not uploaded",it)
            }
    }

    private fun fetchList() {
        firestoreReference.document(courseid).collection(FIREBASE_COLLECTION_NOTES).get()
            .addOnSuccessListener { querrySnapshot ->
                querrySnapshot.forEach { documentSnapshot ->
                    val newNote = documentSnapshot.toObject(Note::class.java)
                    newNote.iD = documentSnapshot.id
                    val index = notesList.indexOf(newNote)
                    if (index == -1) notesList.add(newNote)
                    adapter.notifyDataSetChanged()
                }
            }
    }

    private fun openNote(note: Note) {
        val state = Environment.getExternalStorageState()
        if (Environment.MEDIA_MOUNTED == state) {
            val root = Environment.getExternalStorageDirectory()
            val dir = File(root.absolutePath + "/" + Environment.DIRECTORY_DOCUMENTS + FILE_DIR_EXTENTION)
            if (!dir.exists()) dir.mkdir()

            val file = File(dir, note.name)
            Log.e("NOte", "file uri ${Uri.parse(file.path)}")

            if (file.exists()) {
                val path = FileProvider.getUriForFile(
                    this,
                    BuildConfig.APPLICATION_ID + ".provider",
                    file
                )
                val intent = Intent(Intent.ACTION_VIEW, path)
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                startActivity(intent)

            } else
                downloadFile(note)

        } else
            Log.e("notes", "storage not mounted")
    }

    private fun downloadFile(note: Note) {
        val downloadManager: DownloadManager = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val uri = Uri.parse(note.Link)
        Log.d("Notes", "uri : $uri")
        val request = DownloadManager.Request(uri)
        request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI or DownloadManager.Request.NETWORK_MOBILE)
        request.setAllowedOverRoaming(true)
        request.setTitle("NITTCompanion")
        request.setDescription("Downloading ${note.name}")
        request.setVisibleInDownloadsUi(true)
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOCUMENTS, FILE_DIR_EXTENTION + "/" + note.name)

        downloadManager.enqueue(request)


    }


    private fun getFileName(uri: Uri): String {
        var name = ""
        val cursor = uri.let { returnUri ->
            contentResolver.query(returnUri, null, null, null, null)
        }
        cursor.use { mCursor ->
            if (mCursor != null && mCursor.moveToFirst()) {
                val nameIndex = mCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                name = mCursor.getString(nameIndex)
            }
        }

        if (name == "") {
            name = uri.path!!
            val cut = name.lastIndexOf('/')
            if (cut != -1) {
                name = name.substring(cut + 1)
            }
        }
        Log.d("Notes", "filename : $name")
        return name
    }

}

