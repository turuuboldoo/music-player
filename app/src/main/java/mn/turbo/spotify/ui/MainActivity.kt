package mn.turbo.spotify.ui

import android.os.Bundle
import android.support.v4.media.session.PlaybackStateCompat
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.navigation.fragment.NavHostFragment
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.RequestManager
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import mn.turbo.spotify.R
import mn.turbo.spotify.common.Resource
import mn.turbo.spotify.data.entity.Song
import mn.turbo.spotify.databinding.ActivityMainBinding
import mn.turbo.spotify.player.isPlaying
import mn.turbo.spotify.player.toSong
import mn.turbo.spotify.ui.adapter.SwipeAdapter
import mn.turbo.spotify.ui.viewmodel.MainViewModel
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private val mainViewModel: MainViewModel by viewModels()

    @Inject
    lateinit var swipeAdapter: SwipeAdapter

    @Inject
    lateinit var glide: RequestManager

    private var currentPlayingSong: Song? = null

    private var playbackState: PlaybackStateCompat? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        subscribeToObservers()

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.navHostFragment) as NavHostFragment
        val navController = navHostFragment.navController

        binding.apply {
            with(songViewPager) {
                adapter = swipeAdapter
                registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                    override fun onPageSelected(position: Int) {
                        super.onPageSelected(position)
                        if (playbackState?.isPlaying == true) {
                            mainViewModel.playOrToggleSong(swipeAdapter.currentList[position])
                        } else {
                            currentPlayingSong = swipeAdapter.currentList[position]
                        }
                    }
                })
            }

            imageViewPlay.setOnClickListener {
                currentPlayingSong?.let {
                    mainViewModel.playOrToggleSong(it, true)
                }
            }

            navController
                .addOnDestinationChangedListener { _, destination, _ ->
                    when (destination.id) {
                        R.id.songFragment -> hideBottomPlayer()
                        R.id.homeFragment -> showBottomPlayer()
                        else -> showBottomPlayer()
                    }
                }

            swipeAdapter.setItemClickListener {
                navController.navigate(R.id.globalActionToSongFragment)
            }
        }
    }

    private fun hideBottomPlayer() {
        binding.apply {
            imageViewPlay.isVisible = false
            songViewPager.isVisible = false
            imageViewCover.isVisible = false
        }
    }

    private fun showBottomPlayer() {
        binding.apply {
            imageViewPlay.isVisible = true
            songViewPager.isVisible = true
            imageViewCover.isVisible = true
        }
    }

    private fun switchAdapterSongs(song: Song) {
        val index = swipeAdapter.currentList.indexOf(song)

        if (index != -1) {
            binding.songViewPager.currentItem = index
            currentPlayingSong = song
        }
    }

    private fun subscribeToObservers() {
        mainViewModel.mediaItems.observe(this) {
            it?.let { result ->
                when (result) {
                    is Resource.Success -> {
                        result.data?.let { songs ->
                            swipeAdapter.submitList(songs)
                            if (songs.isNotEmpty()) {
                                glide.load((currentPlayingSong ?: songs[0]).imageUrl)
                                    .into(binding.imageViewCover)
                            }
                            switchAdapterSongs(currentPlayingSong ?: return@observe)
                        }
                    }
                    is Resource.Loading -> Unit
                    is Resource.Error -> Unit
                }
            }
        }

        mainViewModel.currentPlayingSong.observe(this) {
            if (it == null) return@observe

            currentPlayingSong = it.toSong()
            glide.load(currentPlayingSong?.imageUrl).into(binding.imageViewCover)
            switchAdapterSongs(currentPlayingSong ?: return@observe)
        }

        mainViewModel.playbackState.observe(this) {
            playbackState = it
            binding.imageViewPlay.setImageResource(
                if (playbackState?.isPlaying == true) {
                    R.drawable.ic_pause
                } else {
                    R.drawable.ic_play
                }
            )
        }

        mainViewModel.isConnected.observe(this) {
            it?.getContentIfNotHandled()?.let { result ->
                when (result) {
                    is Resource.Error -> {
                        Snackbar.make(
                            binding.rootConstraintLayout,
                            result.message ?: "An unknown error occured",
                            Snackbar.LENGTH_LONG
                        ).show()
                    }
                    else -> Unit
                }
            }
        }

        mainViewModel.networkError.observe(this) {
            it?.getContentIfNotHandled()?.let { result ->
                when (result) {
                    is Resource.Error -> {
                        Snackbar.make(
                            binding.rootConstraintLayout,
                            result.message ?: "An unknown error occured",
                            Snackbar.LENGTH_LONG
                        ).show()
                    }
                    else -> Unit
                }
            }
        }
    }
}
