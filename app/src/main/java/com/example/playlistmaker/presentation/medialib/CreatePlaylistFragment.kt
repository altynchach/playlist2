package com.example.playlistmaker.presentation.medialib

import android.app.AlertDialog
import android.content.DialogInterface
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.view.*
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.setFragmentResult
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.example.playlistmaker.R
import com.example.playlistmaker.domain.models.Playlist
import com.example.playlistmaker.presentation.medialib.view.CreatePlaylistViewModel
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.io.File
import java.io.FileOutputStream

class CreatePlaylistFragment : DialogFragment() {

    private val viewModel: CreatePlaylistViewModel by viewModel()

    companion object {
        const val PLAYLIST_CREATED_KEY = "PLAYLIST_CREATED"

        fun newInstance(): CreatePlaylistFragment {
            return CreatePlaylistFragment()
        }

        fun newInstance(playlistId: Long): CreatePlaylistFragment {
            val fragment = CreatePlaylistFragment()
            val args = Bundle()
            args.putLong("EXTRA_PLAYLIST_ID", playlistId)
            fragment.arguments = args
            return fragment
        }
    }

    private lateinit var backFromCreatePlaylist: ImageView
    private lateinit var addPlaylistImage: ImageView
    private lateinit var editTextNamePlaylist: TextInputEditText
    private lateinit var editTextDescriptionPlaylist: TextInputEditText
    private lateinit var createPlaylistButton: Button
    private lateinit var titleTextView: TextView

    private var editingPlaylistId: Long = 0L
    private var coverPath: String? = null
    private var playlistName: String = ""
    private var playlistDescription: String = ""

    private var isNameNotBlank = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.ThemeOverlay_FullScreenDialog)
        isCancelable = false
        editingPlaylistId = arguments?.getLong("EXTRA_PLAYLIST_ID", 0L) ?: 0L
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        return inflater.inflate(R.layout.fragment_new_playlist, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        backFromCreatePlaylist = view.findViewById(R.id.backFromCreatePlaylist)
        addPlaylistImage = view.findViewById(R.id.addPlaylistImage)
        editTextNamePlaylist = view.findViewById(R.id.editTextNamePlaylist)
        editTextDescriptionPlaylist = view.findViewById(R.id.editTextDescriptionPlaylist)
        createPlaylistButton = view.findViewById(R.id.createPlaylistButton)
        titleTextView = view.findViewById(R.id.title)

        createPlaylistButton.isEnabled = false

        if (editingPlaylistId == 0L) {
            titleTextView.text = getString(R.string.newPlaylist)
            createPlaylistButton.text = getString(R.string.create)
        } else {
            titleTextView.text = getString(R.string.edit_playlist)
            createPlaylistButton.text = getString(R.string.save_changes)
        }

        backFromCreatePlaylist.setOnClickListener {
            handleClose()
        }

        editTextNamePlaylist.addTextChangedListener {
            playlistName = it.toString()
            isNameNotBlank = playlistName.isNotBlank()
            updateButtonState()
        }

        editTextDescriptionPlaylist.addTextChangedListener {
            playlistDescription = it.toString()
        }

        createPlaylistButton.setOnClickListener {
            val name = playlistName.trim()
            val description = playlistDescription.trim()

            if (editingPlaylistId == 0L) {
                lifecycleScope.launch {
                    viewModel.createPlaylist(name, description, coverPath)
                    Toast.makeText(
                        requireContext(),
                        getString(R.string.playlist_created_notify, name),
                        Toast.LENGTH_SHORT
                    ).show()
                    setFragmentResult(PLAYLIST_CREATED_KEY, Bundle())
                    dismiss()
                }
            } else {
                lifecycleScope.launch {
                    viewModel.updatePlaylist(
                        playlistId = editingPlaylistId,
                        name = name,
                        description = description,
                        newCoverPath = coverPath
                    )
                    Toast.makeText(
                        requireContext(),
                        getString(R.string.playlist_updated),
                        Toast.LENGTH_SHORT
                    ).show()
                    setFragmentResult(PLAYLIST_CREATED_KEY, Bundle())
                    dismiss()
                }
            }
        }

        addPlaylistImage.setOnClickListener {
            galleryLauncher.launch("image/*")
        }

        if (savedInstanceState == null) {
            if (editingPlaylistId != 0L) {
                lifecycleScope.launch {
                    val playlist = viewModel.getPlaylistById(editingPlaylistId)
                    if (playlist != null) {
                        fillExistingData(playlist)
                    }
                }
            }
        } else {
            coverPath = savedInstanceState.getString("COVER_PATH")
            playlistName = savedInstanceState.getString("PLAYLIST_NAME", "")
            playlistDescription = savedInstanceState.getString("PLAYLIST_DESC", "")
            isNameNotBlank = playlistName.isNotBlank()
            updateButtonState()

            if (!coverPath.isNullOrEmpty()) {
                addPlaylistImage.scaleType = ImageView.ScaleType.CENTER_CROP
            }
            editTextNamePlaylist.setText(playlistName)
            editTextDescriptionPlaylist.setText(playlistDescription)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putString("COVER_PATH", coverPath)
        outState.putString("PLAYLIST_NAME", playlistName)
        outState.putString("PLAYLIST_DESC", playlistDescription)
        outState.putLong("EDITING_ID", editingPlaylistId)
        super.onSaveInstanceState(outState)
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


    private fun fillExistingData(playlist: Playlist) {
        playlistName = playlist.name
        playlistDescription = playlist.description
        coverPath = playlist.coverFilePath

        isNameNotBlank = playlistName.isNotBlank()
        updateButtonState()

        editTextNamePlaylist.setText(playlistName)
        editTextDescriptionPlaylist.setText(playlistDescription)

        if (!coverPath.isNullOrEmpty()) {
            addPlaylistImage.setImageURI(Uri.parse(coverPath))
            addPlaylistImage.scaleType = ImageView.ScaleType.CENTER_CROP
        }
    }


    private fun updateButtonState() {
        createPlaylistButton.isEnabled = isNameNotBlank
    }

    private val galleryLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            if (uri != null) {
                val filePath = copyUriToInternalStorage(uri)
                coverPath = filePath
                if (!filePath.isNullOrEmpty()) {
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
        val opts = BitmapFactory.Options()
        opts.inJustDecodeBounds = true
        requireContext().contentResolver.openInputStream(uri)?.use { s ->
            BitmapFactory.decodeStream(s, null, opts)
        }
        val (width, height) = opts.outWidth to opts.outHeight
        var inSampleSize = 1
        if (height > reqHeight || width > reqWidth) {
            while ((height / inSampleSize) >= reqHeight && (width / inSampleSize) >= reqWidth) {
                inSampleSize *= 2
            }
        }
        opts.inSampleSize = inSampleSize
        opts.inJustDecodeBounds = false
        return requireContext().contentResolver.openInputStream(uri)?.use { s2 ->
            BitmapFactory.decodeStream(s2, null, opts)
        }
    }

    private fun getFileNameFromUri(uri: Uri): String? {
        var fileName: String? = null
        val returnCursor = requireContext().contentResolver.query(uri, null, null, null, null)
        returnCursor?.use { cursor ->
            val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (nameIndex >= 0) {
                cursor.moveToFirst()
                fileName = cursor.getString(nameIndex)
            }
        }
        return fileName
    }


    fun handleClose() {
        if (playlistName.isBlank() && playlistDescription.isBlank() && coverPath.isNullOrEmpty()) {
            dismiss()
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
                dismiss()
            }
            .setNegativeButton(R.string.cancel) { d, _ ->
                d.dismiss()
            }
            .create()
            .show()
    }
}
