package dev.anonymous.hurriya.admin.presentation.screens.main.album

import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import dev.anonymous.hurriya.admin.presentation.screens.main.album.AlbumsAdapter
import dev.anonymous.hurriya.admin.presentation.screens.main.album.AlbumsAdapter.AlbumsListListener
import dev.anonymous.hurriya.admin.databinding.FragmentAlbumsBinding
import dev.anonymous.hurriya.admin.firebase.controller.FirebaseController
import dev.anonymous.hurriya.admin.firebase.controller.FirebaseController.FirebaseCallback
import dev.anonymous.hurriya.admin.firebase.controller.FirebaseController.GetDataCallback
import dev.anonymous.hurriya.admin.domain.models.Album
import dev.anonymous.hurriya.admin.presentation.components.BaseFragment

class AlbumsFragment : BaseFragment<FragmentAlbumsBinding>(FragmentAlbumsBinding::inflate),
    AlbumsListListener {
    private var albumsAdapter: AlbumsAdapter? = null

    private lateinit var firebaseController: FirebaseController

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        init()
    }

    private fun init() {
        firebaseController = FirebaseController.instance!!

        setUpListeners()
        setUpAlbumsAdapter()
        this.albums
    }

    private fun setUpListeners() {
        binding.floatAddAlbums.setOnClickListener {
            navigateTo(
                AlbumsFragmentDirections.actionAlbumsFragmentToAddAlbumFragment(null)
            )
        }
    }

    private fun setUpAlbumsAdapter() {
        albumsAdapter = AlbumsAdapter()
        binding.albumsRecyclerView.setLayoutManager(LinearLayoutManager(requireContext()))
        binding.albumsRecyclerView.setAdapter(albumsAdapter)
        binding.albumsRecyclerView.setHasFixedSize(true)

        albumsAdapter?.setAlbumsListListener(this)
    }

    private val albums: Unit
        get() {
            binding.progressAlbums.visibility = View.VISIBLE
            firebaseController.getAlbums(object : GetDataCallback<Album> {
                override fun onSuccess(data: ArrayList<Album>) {
                    binding.progressAlbums.visibility = View.GONE
                    albumsAdapter!!.setData(data)
                }

                override fun onFailure(errorMessage: String) {
                }
            })
        }

    private fun deleteAlbumImagesCollection(album: Album) {
        firebaseController.deleteAlbumImages(album.id!!, object : FirebaseCallback {
            override fun onSuccess() {
                deleteAlbumDocument(album)
            }

            override fun onFailure(errorMessage: String) {
                dismissLoadingDialog()
            }
        })
    }

    private fun deleteAlbumDocument(album: Album) {
        showLoadingDialog()
        firebaseController.deleteAlbum(album.id!!, object : FirebaseCallback {
            override fun onSuccess() {
                albumsAdapter?.removeItem(album.id!!)
                deleteAlbumImage(album)
            }

            override fun onFailure(errorMessage: String) {
                dismissLoadingDialog()
            }
        })
    }

    private fun deleteAlbumImage(album: Album) {
        firebaseController.deleteFileUsingUrl(album.imageUrl!!, object : FirebaseCallback {
            override fun onSuccess() {
                deleteAlbumImages(album.id!!)
            }

            override fun onFailure(errorMessage: String) {
                dismissLoadingDialog()
            }
        })
    }

    private fun deleteAlbumImages(albumUuid: String) {
        firebaseController.deleteAlbumFiles(albumUuid, object : FirebaseCallback {
            override fun onSuccess() {
                dismissLoadingDialog()
            }

            override fun onFailure(errorMessage: String) {
                dismissLoadingDialog()
            }
        })
    }

    override fun onClickAlbumItemListener(albumId: String) {
        navigateTo(
            AlbumsFragmentDirections.actionAlbumsFragmentToAlbumImagesFragment(albumId)
        )
    }

    override fun onClickDeleteListener(album: Album) {
        deleteAlbumImagesCollection(album)
    }

    override fun onClickUpdateListener(model: Album) {
        navigateTo(
            AlbumsFragmentDirections.actionAlbumsFragmentToAddAlbumFragment(model)
        )
    }
}