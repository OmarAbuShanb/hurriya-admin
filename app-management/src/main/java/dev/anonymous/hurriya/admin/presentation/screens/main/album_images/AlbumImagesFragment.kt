package dev.anonymous.hurriya.admin.presentation.screens.main.album_images

import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts.GetMultipleContents
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.GridLayoutManager
import com.google.firebase.Timestamp
import dev.anonymous.hurriya.admin.R
import dev.anonymous.hurriya.admin.presentation.screens.main.album_images.AlbumImagesAdapter
import dev.anonymous.hurriya.admin.presentation.screens.main.album_images.AlbumImagesAdapter.AlbumImageListListener
import dev.anonymous.hurriya.admin.presentation.screens.main.album_images.AlbumImagesAdapter.BaseAddAlbumImagesListener
import dev.anonymous.hurriya.admin.databinding.FragmentAlbumImagesBinding
import dev.anonymous.hurriya.admin.firebase.controller.FirebaseController
import dev.anonymous.hurriya.admin.firebase.controller.FirebaseController.FirebaseCallback
import dev.anonymous.hurriya.admin.firebase.controller.FirebaseController.GetDataCallback
import dev.anonymous.hurriya.admin.firebase.controller.FirebaseController.UploadFileCallback
import dev.anonymous.hurriya.admin.domain.models.AlbumImage
import dev.anonymous.hurriya.admin.presentation.components.BaseFragment
import dev.anonymous.hurriya.admin.utils.UtilsGeneral
import java.util.Random

class AlbumImagesFragment :
    BaseFragment<FragmentAlbumImagesBinding>(FragmentAlbumImagesBinding::inflate),
    AlbumImageListListener,
    BaseAddAlbumImagesListener {
    private lateinit var firebaseController: FirebaseController

    private var albumImagesAdapter: AlbumImagesAdapter? = null

    private var newAlbumImagesUri: ArrayList<Uri> = ArrayList()
    private var deleteAlbumImages: ArrayList<AlbumImage> = ArrayList()

    private val args: AlbumImagesFragmentArgs by navArgs()
    private var albumId: String? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        init()
    }

    private fun init() {
        firebaseController = FirebaseController.instance!!

        setupListeners()
        setupAlbumImagesAdapter()
        putDataIfExist()
    }

    private fun setupListeners() {
        binding.floatAddAlbumsImages.setOnClickListener {
            if (newAlbumImagesUri.isEmpty() && deleteAlbumImages.isEmpty()) {
                UtilsGeneral.instance?.showToast(
                    requireContext(),
                    getString(R.string.there_is_nothing_to_save)
                )
            } else {
                showLoadingDialog()
                checkFollowingProcess()
            }
        }
    }

    private fun setupAlbumImagesAdapter() {
        albumImagesAdapter = AlbumImagesAdapter()
        val manager = GridLayoutManager(requireContext(), 2)
        binding.albumsImagesRecycler.setLayoutManager(manager)
        binding.albumsImagesRecycler.setHasFixedSize(true)
        binding.albumsImagesRecycler.setAdapter(albumImagesAdapter)

        albumImagesAdapter?.setAlbumImageListCallback(this)
        albumImagesAdapter?.setBaseAddAlbumImagesListener(this)
    }

    private fun putDataIfExist() {
        albumId = args.albumId
        this.albumImages
    }

    private val albumImages: Unit
        get() {
            binding.progressAlbumsImages.visibility = View.VISIBLE
            firebaseController.getAlbumImages(albumId!!, object : GetDataCallback<AlbumImage> {
                override fun onSuccess(data: ArrayList<AlbumImage>) {
                    binding.progressAlbumsImages.visibility = View.GONE
                    albumImagesAdapter!!.setData(data)
                }

                override fun onFailure(errorMessage: String) {
                    UtilsGeneral.instance
                        ?.showToast(requireContext(), getString(R.string.something_went_wrong))
                }
            })
        }

    private fun uploadAlbumImage(imageUri: Uri) {
        firebaseController.uploadAlbumImages(albumId!!, imageUri, object : UploadFileCallback {
            override fun onSuccess(fileUrl: String) {
                setAlbumImage(getAlbumImage(fileUrl), imageUri)
            }

            override fun onFailure(errorMessage: String) {
                dismissLoadingDialog()
            }
        })
    }

    private fun setAlbumImage(albumImage: AlbumImage, imageUri: Uri) {
        firebaseController.setAlbumImage(albumImage, albumId!!, object : FirebaseCallback {
            override fun onSuccess() {
                newAlbumImagesUri.remove(imageUri)
                checkFollowingProcess()
            }

            override fun onFailure(errorMessage: String) {
                dismissLoadingDialog()
            }
        })
    }

    private fun deleteAlbumImage(albumImage: AlbumImage, itemIndex: Int) {
        firebaseController.deleteAlbumImage(
            albumImage.id!!,
            albumId!!,
            object : FirebaseCallback {
                override fun onSuccess() {
                    deleteFileUsingUrl(albumImage.imageUrl!!, itemIndex)
                }

                override fun onFailure(errorMessage: String) {
                    dismissLoadingDialog()
                }
            })
    }

    private fun deleteFileUsingUrl(imageUrl: String, itemIndex: Int) {
        firebaseController.deleteFileUsingUrl(imageUrl, object : FirebaseCallback {
            override fun onSuccess() {
                deleteAlbumImages.removeAt(itemIndex)
                checkFollowingProcess()
            }

            override fun onFailure(errorMessage: String) {
                dismissLoadingDialog()
            }
        })
    }

    private fun checkFollowingProcess() {
        if (!deleteAlbumImages.isEmpty()) {
            val lastItemIndex = deleteAlbumImages.size - 1
            val albumImage = deleteAlbumImages[lastItemIndex]
            deleteAlbumImage(albumImage, lastItemIndex)
        } else if (!newAlbumImagesUri.isEmpty()) {
            val lastItemIndex = newAlbumImagesUri.size - 1
            val imageUri = newAlbumImagesUri[lastItemIndex]
            uploadAlbumImage(imageUri)
        } else {
            dismissLoadingDialog()
            closeCurrentFragment()
        }
    }

    private fun addNewAlbumImages(uriList: List<Uri>) {
        val albumImages = ArrayList<AlbumImage>()
        val random = Random()
        for (uri in uriList) {
            // randomNumberString => albumImagesAdapter.removeItem(model.getUuid());
            val randomNumberString = random.nextInt(10000).toString()
            albumImages.add(AlbumImage(randomNumberString, uri))
        }
        albumImagesAdapter!!.addItems(albumImages)
    }

    private val getAlbumImagesLauncher = registerForActivityResult(
        GetMultipleContents()
    ) { result: List<Uri> ->
        if (result.isNotEmpty()) {
            newAlbumImagesUri.addAll(result)
            addNewAlbumImages(result)
        }
    }


    private fun getAlbumImage(imageUrl: String): AlbumImage {
        return AlbumImage(imageUrl, Timestamp.now())
    }


    override fun onClickAddListener() {
        getAlbumImagesLauncher.launch("image/*")
    }

    override fun onClickDeleteListener(model: AlbumImage) {
        // delete new image
        if (model.imageUri != null) {
            newAlbumImagesUri.remove(model.imageUri)
        } else {
            // delete old image
            deleteAlbumImages.add(model)
        }
        // refresh recycler
        albumImagesAdapter?.removeItem(model.id)
    }
}