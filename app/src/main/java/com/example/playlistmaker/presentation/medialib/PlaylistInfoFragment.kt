package com.example.playlistmaker.presentation.medialib

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.core.view.isVisible
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.playlistmaker.R
import com.example.playlistmaker.domain.models.Track
import com.example.playlistmaker.presentation.medialib.adapter.PlaylistTracksAdapter
import com.example.playlistmaker.presentation.medialib.view.PlaylistInfoScreenState
import com.example.playlistmaker.presentation.medialib.view.PlaylistInfoViewModel
import com.example.playlistmaker.presentation.player.PlayerActivity
import com.google.android.material.bottomsheet.BottomSheetBehavior
import org.koin.androidx.viewmodel.ext.android.viewModel

class PlaylistInfoFragment : DialogFragment() {

    private val viewModel: PlaylistInfoViewModel by viewModel()

    private lateinit var backFromPlaylistInfo: ImageView
    private lateinit var playlistImage: ImageView
    private lateinit var playlistName: TextView
    private lateinit var playlistDescription: TextView
    private lateinit var sumLength: TextView
    private lateinit var tracksCount: TextView
    private lateinit var sharePlaylist: ImageView
    private lateinit var editPlaylist: ImageView
    private lateinit var buttonsLayout: LinearLayout

    private lateinit var bottomSheetPlaylistInfo: LinearLayout
    private lateinit var bottomSheetEditPlaylistInfo: LinearLayout
    private lateinit var playlistTracksRecyclerBS: RecyclerView
    private lateinit var darkFrame: View

    private lateinit var playlistTracksAdapter: PlaylistTracksAdapter
    private var playlistIdArg: Long = 0L

    // Защита от двойных нажатий
    private var isClickAllowed = true
    private fun clickDebounce(): Boolean {
        val current = isClickAllowed
        if (isClickAllowed) {
            isClickAllowed = false
            view?.postDelayed({ isClickAllowed = true }, 1000L)
        }
        return current
    }

    companion object {
        private const val PLAYLIST_ID_ARG = "PLAYLIST_ID_ARG"

        fun newInstance(playlistId: Long): PlaylistInfoFragment {
            val fragment = PlaylistInfoFragment()
            val args = Bundle()
            args.putLong(PLAYLIST_ID_ARG, playlistId)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // DialogFragment: фуллскрин + отсутствие системных баров
        setStyle(STYLE_NORMAL, R.style.ThemeOverlay_FullScreenDialog)
        isCancelable = false

        playlistIdArg = arguments?.getLong(PLAYLIST_ID_ARG, 0L) ?: 0L
        viewModel.loadPlaylist(playlistIdArg)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        // Макет: смотрите, чтобы 100% соответствовал имени и содержал bottom_sheet_playlist_info
        return inflater.inflate(R.layout.playlist_info_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        backFromPlaylistInfo = view.findViewById(R.id.backFromPlaylistInfo)
        playlistImage = view.findViewById(R.id.PlaylistImage)
        playlistName = view.findViewById(R.id.PlaylistName)
        playlistDescription = view.findViewById(R.id.PlaylistDescription)
        sumLength = view.findViewById(R.id.sumLength)
        tracksCount = view.findViewById(R.id.tracksCount)
        sharePlaylist = view.findViewById(R.id.sharePlaylist)
        editPlaylist = view.findViewById(R.id.editPlaylist)

        buttonsLayout = view.findViewById(R.id.buttonsLayout)

        bottomSheetPlaylistInfo = view.findViewById(R.id.bottom_sheet_playlist_info)
        bottomSheetEditPlaylistInfo = view.findViewById(R.id.bottom_sheet_edit_playlist_info)
        darkFrame = view.findViewById(R.id.darkFrame)
        playlistTracksRecyclerBS = view.findViewById(R.id.playlistTracksRecyclerBS)

        // Инициализируем адаптер списка треков
        playlistTracksAdapter = PlaylistTracksAdapter { track ->
            if (clickDebounce()) {
                openPlayerActivity(track)
            }
        }
        // Лонг-клик (удаление трека)
        playlistTracksAdapter.setOnTrackLongClickListener { track ->
            if (clickDebounce()) {
                showRemoveTrackDialog(track)
            }
        }
        playlistTracksRecyclerBS.layoutManager = LinearLayoutManager(requireContext())
        playlistTracksRecyclerBS.adapter = playlistTracksAdapter

        // Подключаем BottomSheetBehavior к нашей LinearLayout
        val playlistSheetBehavior = BottomSheetBehavior.from(bottomSheetPlaylistInfo)
        bottomSheetPlaylistInfo.visibility = View.VISIBLE
        playlistSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED

        // ==== ВАЖНО: Вычисляем и ставим peekHeight, чтобы шторка была под кнопками ====
        val location = IntArray(2)
        buttonsLayout.getLocationOnScreen(location)
        val buttonsLayoutY = location[1]
        val screenHeightPx = resources.displayMetrics.heightPixels
        // Расстояние от низа экрана до низа buttonsLayout
        val distanceFromBottomPx = screenHeightPx - buttonsLayoutY - buttonsLayout.height

        // Устанавливаем стартовую высоту шторки
        playlistSheetBehavior.peekHeight = distanceFromBottomPx

        // Отслеживаем свайпы. Если пользователь тянет вниз (slideOffset < 0),
        // заново выставляем peekHeight, чтобы BottomSheet не уехал выше кнопок
        playlistSheetBehavior.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(sheet: View, newState: Int) {
                // Можем при свертывании скрывать darkFrame, если нужно
                if (newState == BottomSheetBehavior.STATE_EXPANDED ||
                    newState == BottomSheetBehavior.STATE_COLLAPSED
                ) {
                    darkFrame.visibility = View.GONE
                }
            }
            override fun onSlide(sheet: View, slideOffset: Float) {
                if (slideOffset < 0) {
                    playlistSheetBehavior.peekHeight = distanceFromBottomPx
                }
            }
        })

        // Вторая шторка (меню: «Поделиться», «Редакт.», «Удалить»)
        val editSheetBehavior = BottomSheetBehavior.from(bottomSheetEditPlaylistInfo)
        editSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
        editSheetBehavior.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(sheet: View, newState: Int) {
                if (newState == BottomSheetBehavior.STATE_HIDDEN) {
                    darkFrame.visibility = View.INVISIBLE
                } else if (newState == BottomSheetBehavior.STATE_EXPANDED
                    || newState == BottomSheetBehavior.STATE_HALF_EXPANDED
                ) {
                    darkFrame.visibility = View.VISIBLE
                }
            }
            override fun onSlide(sheet: View, slideOffset: Float) {}
        })

        // Кнопка «Назад»
        backFromPlaylistInfo.setOnClickListener {
            if (clickDebounce()) {
                findNavController().popBackStack()
            }
        }

        // Кнопка «Поделиться» (прямая, над треками)
        sharePlaylist.setOnClickListener {
            if (clickDebounce()) {
                viewModel.sharePlaylist { shareText ->
                    if (shareText.isEmpty()) {
                        Toast.makeText(
                            requireContext(),
                            getString(R.string.no_tracks_in_playlist_to_share),
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        val intent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_TEXT, shareText)
                        }
                        startActivity(Intent.createChooser(intent, getString(R.string.share_playlist)))
                    }
                }
            }
        }

        // Кнопка «Меню» (три точки) -> открывает вторую шторку
        editPlaylist.setOnClickListener {
            if (clickDebounce()) {
                darkFrame.visibility = View.VISIBLE
                editSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
            }
        }

        // Тап по затемнению скрывает вторую шторку
        darkFrame.setOnClickListener {
            darkFrame.visibility = View.INVISIBLE
            editSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
        }

        // Кнопки внутри второй шторки: shareLayout, editLayout, deleteLayout
        val shareLayout = bottomSheetEditPlaylistInfo.findViewById<View>(R.id.share)
        val editLayout = bottomSheetEditPlaylistInfo.findViewById<View>(R.id.edit)
        val deleteLayout = bottomSheetEditPlaylistInfo.findViewById<View>(R.id.delete)

        shareLayout.setOnClickListener {
            if (clickDebounce()) {
                editSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
                viewModel.sharePlaylist { shareText ->
                    if (shareText.isEmpty()) {
                        Toast.makeText(
                            requireContext(),
                            getString(R.string.no_tracks_in_playlist_to_share),
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        val intent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_TEXT, shareText)
                        }
                        startActivity(Intent.createChooser(intent, getString(R.string.share_playlist)))
                    }
                }
            }
        }

        editLayout.setOnClickListener {
            if (clickDebounce()) {
                editSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
                val playlist = viewModel.state.value?.playlist ?: return@setOnClickListener
                openEditPlaylistScreen(playlist.playlistId)
            }
        }

        deleteLayout.setOnClickListener {
            if (clickDebounce()) {
                editSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
                showDeletePlaylistDialog()
            }
        }

        // Подписываемся на LiveData viewModel
        viewModel.state.observe(viewLifecycleOwner, Observer { state ->
            renderState(state)
        })
    }

    override fun onResume() {
        super.onResume()
        // Обновим плейлист при возврате, чтобы показать свежие данные
        viewModel.loadPlaylist(playlistIdArg)
        dialog?.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
    }

    private fun renderState(state: PlaylistInfoScreenState) {
        val playlist = state.playlist ?: return

        // Обложка
        if (playlist.coverFilePath.isNullOrEmpty()) {
            playlistImage.setImageResource(R.drawable.placeholder)
        } else {
            Glide.with(this)
                .load(playlist.coverFilePath)
                .placeholder(R.drawable.placeholder)
                .into(playlistImage)
        }

        // Название, описание
        playlistName.text = playlist.name
        playlistDescription.isVisible = playlist.description.isNotBlank()
        playlistDescription.text = playlist.description

        // Кол-во треков
        if (playlist.trackIds.isEmpty()) {
            tracksCount.text = getString(R.string.no_tracks_in_playlist)
        } else {
            val countText = resources.getQuantityString(
                R.plurals.playlist_tracks_count,
                playlist.trackIds.size,
                playlist.trackIds.size
            )
            tracksCount.text = countText
        }

        // Общая длительность
        sumLength.text = state.totalDuration

        // Обновить адаптер
        playlistTracksAdapter.updateTracks(state.tracks)
    }

    // Диалог подтверждения удаления одного трека
    private fun showRemoveTrackDialog(track: Track) {
        AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.delete_track))
            .setMessage(getString(R.string.delete_track_question))
            .setPositiveButton(getString(R.string.delete)) { dialog, _ ->
                dialog.dismiss()
                viewModel.removeTrackFromPlaylist(track)
            }
            .setNegativeButton(getString(R.string.cancel)) { dialog, _ ->
                dialog.dismiss()
            }
            .create()
            .show()
    }

    // Диалог подтверждения удаления всего плейлиста
    private fun showDeletePlaylistDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.delete_playlist))
            .setMessage(getString(R.string.delete_playlist_question))
            .setPositiveButton(getString(R.string.delete)) { dialog, _ ->
                dialog.dismiss()
                viewModel.deletePlaylist()
                // Уходим назад, чтобы вернуться на список плейлистов
                findNavController().popBackStack()
            }
            .setNegativeButton(getString(R.string.cancel)) { dialog, _ ->
                dialog.dismiss()
            }
            .create()
            .show()
    }

    // Открываем фрагмент/диалог для редактирования
    private fun openEditPlaylistScreen(playlistId: Long) {
        val dialog = CreatePlaylistFragment.newInstance(playlistId)
        dialog.show(parentFragmentManager, "EditPlaylistDialog")
    }

    // Открываем экран плеера
    private fun openPlayerActivity(track: Track) {
        val ctx = context ?: return
        val intent = Intent(ctx, PlayerActivity::class.java)
        intent.putExtra(PlayerActivity.NAME_TRACK, com.google.gson.Gson().toJson(track))
        startActivity(intent)
    }
}
