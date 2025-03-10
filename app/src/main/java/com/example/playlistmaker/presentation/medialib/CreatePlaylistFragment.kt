package com.example.playlistmaker.presentation.medialib

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.setFragmentResult
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
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

    private var coverPath: String? = null
    private var playlistName: String = ""
    private var playlistDescription: String = ""

    companion object {
        private const val PLAYLIST_CREATED_KEY = "PLAYLIST_CREATED"
        fun newInstance() = CreatePlaylistFragment()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.ThemeOverlay_FullScreenDialog)
        isCancelable = false
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
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
            playlistName = name
            viewModel.onNameChanged(name)
        }

        editTextDescriptionPlaylist.addTextChangedListener {
            playlistDescription = it.toString()
        }

        createPlaylistButton.setOnClickListener {
            val name = editTextNamePlaylist.text.toString().trim()
            val description = editTextDescriptionPlaylist.text.toString().trim()

            viewModel.savePlaylist(name, description)
            Toast.makeText(
                requireContext(),
                getString(R.string.playlist_created_notify, name),
                Toast.LENGTH_SHORT
            ).show()

            setFragmentResult(PLAYLIST_CREATED_KEY, Bundle())
            dismiss() // вместо dismissAllowingStateLoss()
        }

        addPlaylistImage.setOnClickListener {
            galleryLauncher.launch("image/*")
        }

        viewModel.state.observe(viewLifecycleOwner) { state ->
            createPlaylistButton.isEnabled = state.isCreateButtonEnabled
            state.coverFilePath?.let {
                addPlaylistImage.setImageURI(Uri.parse(it))
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString("COVER_PATH", coverPath)
        outState.putString("PLAYLIST_NAME", playlistName)
        outState.putString("PLAYLIST_DESC", playlistDescription)
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        if (savedInstanceState != null) {
            coverPath = savedInstanceState.getString("COVER_PATH")
            playlistName = savedInstanceState.getString("PLAYLIST_NAME", "")
            playlistDescription = savedInstanceState.getString("PLAYLIST_DESC", "")

            if (!coverPath.isNullOrEmpty()) {
                viewModel.onCoverPicked(coverPath)
                addPlaylistImage.setImageURI(Uri.parse(coverPath))
            }
            if (playlistName.isNotEmpty()) {
                editTextNamePlaylist.setText(playlistName)
                viewModel.onNameChanged(playlistName)
            }
            if (playlistDescription.isNotEmpty()) {
                editTextDescriptionPlaylist.setText(playlistDescription)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        dialog?.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
    }

    override fun onCancel(dialog: DialogInterface) {
    }

    private val galleryLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            if (uri != null) {
                val filePath = copyUriToInternalStorage(uri)
                coverPath = filePath
                viewModel.onCoverPicked(filePath)
                addPlaylistImage.scaleType = ImageView.ScaleType.CENTER_CROP
                Glide.with(this)
                    .load(filePath)
                    .transform(
                        CenterCrop(),
                        RoundedCorners(resources.getDimensionPixelSize(R.dimen.corner_radius))
                    )
                    .into(addPlaylistImage)
            }
        }

    private fun copyUriToInternalStorage(uri: Uri): String? {
        try {
            val bitmap = decodeSampledBitmapFromUri(uri, 1024, 1024)
            if (bitmap != null) {
                val fileName = getFileNameFromUri(uri) ?: System.currentTimeMillis().toString()
                val file = File(requireContext().filesDir, fileName)
                FileOutputStream(file).use { out ->
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
                }
                bitmap.recycle()
                return file.absolutePath
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    private fun decodeSampledBitmapFromUri(uri: Uri, reqWidth: Int, reqHeight: Int): Bitmap? {
        val options = BitmapFactory.Options()
        options.inJustDecodeBounds = true
        requireContext().contentResolver.openInputStream(uri)?.use { s ->
            BitmapFactory.decodeStream(s, null, options)
        }
        val (width, height) = options.outWidth to options.outHeight
        var inSampleSize = 1
        if (height > reqHeight || width > reqWidth) {
            while ((height / inSampleSize) >= reqHeight && (width / inSampleSize) >= reqWidth) {
                inSampleSize *= 2
            }
        }
        options.inSampleSize = inSampleSize
        options.inJustDecodeBounds = false
        return requireContext().contentResolver.openInputStream(uri)?.use { s2 ->
            BitmapFactory.decodeStream(s2, null, options)
        }
    }

    private fun getFileNameFromUri(uri: Uri): String? {
        var fileName: String? = null
        val returnCursor = requireContext().contentResolver.query(uri, null, null, null, null)
        returnCursor?.use { cursor ->
            val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            cursor.moveToFirst()
            fileName = cursor.getString(nameIndex)
        }
        return fileName
    }

    fun handleClose() {
        val name = editTextNamePlaylist.text?.toString().orEmpty()
        val description = editTextDescriptionPlaylist.text?.toString().orEmpty()
        if (name.isBlank() && description.isBlank() && !viewModel.hasCover()) {
            dismiss()
        } else {
            showConfirmationDialog()
        }
    }

    private fun showConfirmationDialog() {
        AlertDialog.Builder(requireActivity())
            .setTitle(R.string.finishPlaylistCreating)
            .setMessage(R.string.dataWillLost)
            .setPositiveButton(R.string.complete) { dialog, _ ->
                dialog.dismiss()
                dismiss()
            }
            .setNegativeButton(R.string.cancel) { dialog, _ ->
                dialog.dismiss()
            }
            .create()
            .show()
    }
}
