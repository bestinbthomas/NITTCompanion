package com.example.nittcompanion.notes

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.MimeTypeMap
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.nittcompanion.R
import kotlinx.android.synthetic.main.fragment_notes.*

class NotesFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_notes, container, false)
    }

    override fun onStart() {
        super.onStart()

        SearchFiles.setOnClickListener {
            //openFileChooser()
        }
    }
}

// private fun openFileChooser() {
// val intent = Intent()
// intent.type = "image/*"
// intent.action = Intent.ACTION_GET_CONTENT
// startActivityForResult(intent,1)
// }
//
// override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
// super.onActivityResult(requestCode, resultCode, data)
//
// if (requestCode == 1 && resultCode == Activity.RESULT_OK && data!=null &&data.data!=null){
// mfileuri = data.data
// val ext = getFileExtention(data.data)
// Toast.makeText(requireContext(),"fileselected with uri $mfileuri with extention $ext",Toast.LENGTH_SHORT).show()
// }
// }
//
// private fun getFileExtention(uri : Uri) : String? {
// val cr = requireContext().contentResolver
// val mime = MimeTypeMap.getSingleton()
// return  mime.getExtensionFromMimeType(cr.getType(uri))
//
// }
// *