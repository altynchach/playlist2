package com.example.playlistmaker.presentation.medialib

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import com.example.playlistmaker.R
import com.example.playlistmaker.presentation.medialib.view.CreatePlaylistViewModel
import com.google.android.material.textfield.TextInputEditText
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.io.File
import java.io.FileOutputStream

class CreatePlaylistFragment : DialogFragment() {

    private val viewModel: CreatePlaylistViewModel by viewModel()

    private lateinit var backFromCreatePlaylist: ImageView
    private lateinit var addPlaylistImage: ImageView
    private lateinit var editTextNamePlaylist: TextInputEditText
    private lateinit var editTextDescriptionPlaylist: TextInputEditText
    private lateinit var createPlaylistButton: Button

    private var hasUnsavedData = false

    companion object {
        fun newInstance() = CreatePlaylistFragment()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        dialog?.setCanceledOnTouchOutside(false)
        return inflater.inflate(R.layout.fragment_create_playlist, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        backFromCreatePlaylist = view.findViewById(R.id.backFromCreatePlaylist)
        addPlaylistImage = view.findViewById(R.id.addPlaylistImage)
        editTextNamePlaylist = view.findViewById(R.id.editTextNamePlaylist)
        editTextDescriptionPlaylist = view.findViewById(R.id.editTextDescriptionPlaylist)
        createPlaylistButton = view.findViewById(R.id.createPlaylistButton)

        createPlaylistButton.isEnabled = false
        backFromCreatePlaylist.setOnClickListener {
            handleClose()
        }

        editTextNamePlaylist.addTextChangedListener {
            val name = it.toString()
            viewModel.onNameChanged(name)
            hasUnsavedData = hasUnsavedData || name.isNotEmpty()
        }
        editTextDescriptionPlaylist.addTextChangedListener {
            hasUnsavedData = true
        }

        createPlaylistButton.setOnClickListener {
            val name = editTextNamePlaylist.text.toString()
            val description = editTextDescriptionPlaylist.text.toString()
            viewModel.savePlaylist(name, description)
        }

        addPlaylistImage.setOnClickListener {
            pickImageFromGallery()
        }

        viewModel.state.observe(viewLifecycleOwner) { state ->
            createPlaylistButton.isEnabled = state.isCreateButtonEnabled
            state.coverFilePath?.let {
                addPlaylistImage.setImageURI(Uri.parse(it))
            }
            if (state.isPlaylistCreated) {
                val parentActivity = requireActivity()
                val data = parentActivity.intent.extras ?: Bundle()
                data.putBoolean(PlaylistsFragment.PLAYLIST_CREATED_KEY, true)
                data.putString(PlaylistsFragment.PLAYLIST_NAME_KEY, state.createdPlaylistName)
                parentActivity.intent.putExtras(data)

                dismissAllowingStateLoss()
            }
        }
    }

    private fun pickImageFromGallery() {
        galleryLauncher.launch("image/*")
    }

    private val galleryLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            if (uri != null) {
                val filePath = copyUriToInternalStorage(uri)
                viewModel.onCoverPicked(filePath)
                hasUnsavedData = true
            }
        }


    private fun copyUriToInternalStorage(uri: Uri): String? {
        try {
            val inputStream = requireContext().contentResolver.openInputStream(uri) ?: return null
            val fileName = getFileNameFromUri(uri) ?: System.currentTimeMillis().toString()
            val file = File(requireContext().filesDir, fileName)

            FileOutputStream(file).use { output ->
                inputStream.copyTo(output)
            }

            inputStream.close()
            return file.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    private fun getFileNameFromUri(uri: Uri): String? {
        var fileName: String? = null
        val returnCursor = requireContext().contentResolver.query(uri, null, null, null, null)
        if (returnCursor != null) {
            val nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            returnCursor.moveToFirst()
            fileName = returnCursor.getString(nameIndex)
            returnCursor.close()
        }
        return fileName
    }

    private fun handleClose() {
        val name = editTextNamePlaylist.text?.toString().orEmpty()
        val description = editTextDescriptionPlaylist.text?.toString().orEmpty()

        if (name.isBlank() && description.isBlank() && !viewModel.hasCover()) {
            dismissAllowingStateLoss()
        } else {
            showConfirmationDialog()
        }
    }

    private fun showConfirmationDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.finishPlaylistCreating)
            .setMessage(R.string.dataWillLost)
            .setPositiveButton(R.string.complete) { dialog, _ ->
                dialog.dismiss()
                dismissAllowingStateLoss()
            }
            .setNegativeButton(R.string.cancel) { dialog, _ ->
                dialog.dismiss()
            }
            .create()
            .show()
    }

    override fun onCancel(dialog: DialogInterface) {
        super.onCancel(dialog)
        handleClose()
    }

    override fun onResume() {
        super.onResume()
        dialog?.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
    }
}
