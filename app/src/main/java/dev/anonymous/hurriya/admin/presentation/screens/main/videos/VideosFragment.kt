package dev.anonymous.hurriya.admin.presentation.screens.main.videos

import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import dev.anonymous.hurriya.admin.presentation.screens.main.videos.VideosAdapter
import dev.anonymous.hurriya.admin.presentation.screens.main.videos.VideosAdapter.VideosListListener
import dev.anonymous.hurriya.admin.databinding.FragmentVideosBinding
import dev.anonymous.hurriya.admin.firebase.controller.FirebaseController
import dev.anonymous.hurriya.admin.firebase.controller.FirebaseController.FirebaseCallback
import dev.anonymous.hurriya.admin.firebase.controller.FirebaseController.GetDataCallback
import dev.anonymous.hurriya.admin.domain.models.Video
import dev.anonymous.hurriya.admin.presentation.components.BaseFragment

class VideosFragment : BaseFragment<FragmentVideosBinding>(FragmentVideosBinding::inflate),
    VideosListListener {
    private lateinit var firebaseController: FirebaseController

    private var videosAdapter: VideosAdapter? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        init()
    }

    private fun init() {
        firebaseController = FirebaseController.instance!!

        setupListeners()
        setupVideosAdapter()
        this.videos
    }

    private fun setupListeners() {
        binding.floatAddVideos.setOnClickListener {
            navigateTo(
                VideosFragmentDirections.actionVideosFragmentToAddVideoFragment(null)
            )
        }
    }

    private fun setupVideosAdapter() {
        videosAdapter = VideosAdapter()
        binding.videosRecyclerView.setLayoutManager(LinearLayoutManager(requireContext()))
        binding.videosRecyclerView.setAdapter(videosAdapter)
        binding.videosRecyclerView.setHasFixedSize(true)

        videosAdapter!!.setVideosListCallback(this)
    }

    private val videos: Unit
        get() {
            binding.progressVideos.visibility = View.VISIBLE

            firebaseController.getVideos(object : GetDataCallback<Video> {
                override fun onSuccess(data: ArrayList<Video>) {
                    binding.progressVideos.visibility = View.GONE

                    videosAdapter!!.setData(data)
                }

                override fun onFailure(errorMessage: String) {
                }
            })
        }

    private fun deleteVideo(id: String) {
        showLoadingDialog()
        firebaseController.deleteVideo(id, object : FirebaseCallback {
            override fun onSuccess() {
                videosAdapter!!.removeItem(id)
                deleteVideoFiles(id)
            }

            override fun onFailure(errorMessage: String) {
                dismissLoadingDialog()
            }
        })
    }

    private fun deleteVideoFiles(uuid: String) {
        firebaseController.deleteVideoFiles(uuid, object : FirebaseCallback {
            override fun onSuccess() {
                dismissLoadingDialog()
            }

            override fun onFailure(errorMessage: String) {
                dismissLoadingDialog()
            }
        })
    }

    override fun onClickVideoItemListener(model: Video) {
        navigateTo(
            VideosFragmentDirections.actionVideosFragmentToAddVideoFragment(model)
        )
    }

    override fun onClickDeleteListener(id: String) {
        deleteVideo(id)
    }
}