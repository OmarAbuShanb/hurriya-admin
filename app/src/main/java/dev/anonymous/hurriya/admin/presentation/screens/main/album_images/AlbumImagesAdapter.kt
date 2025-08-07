package dev.anonymous.hurriya.admin.presentation.screens.main.album_images

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import dev.anonymous.hurriya.admin.databinding.ItemAlbumImageBinding
import dev.anonymous.hurriya.admin.databinding.ItemBaseAddAlbumImageBinding
import dev.anonymous.hurriya.admin.domain.models.AlbumImage
import dev.anonymous.hurriya.admin.utils.UtilsGeneral

class AlbumImagesAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder?>() {
    private val albumImages: ArrayList<AlbumImage> = ArrayList()
    private var albumImagesListListener: AlbumImageListListener? = null
    private var baseAddAlbumImagesListener: BaseAddAlbumImagesListener? = null

    fun setBaseAddAlbumImagesListener(baseAddAlbumImagesListener: BaseAddAlbumImagesListener) {
        this.baseAddAlbumImagesListener = baseAddAlbumImagesListener
    }

    fun setAlbumImageListCallback(albumImagesListListener: AlbumImageListListener) {
        this.albumImagesListListener = albumImagesListListener
    }

    fun setData(albumImages: ArrayList<AlbumImage>) {
        // Base
        this.albumImages.add(AlbumImage(""))

        this.albumImages.addAll(albumImages)
        notifyDataSetChanged()
    }

    fun addItems(albumImages: ArrayList<AlbumImage>) {
        val positionStart = this.albumImages.size
        val itemCount = albumImages.size

        this.albumImages.addAll(albumImages)
        notifyItemRangeInserted(positionStart, itemCount)
    }

    fun removeItem(uuid: String?) {
        for (i in albumImages.indices) {
            if (albumImages[i].id.equals(uuid)) {
                albumImages.removeAt(i)
                notifyItemRemoved(i)
                break
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (position == 0) {
            BASE_HOLDER
        } else {
            IMAGE_HOLDER
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        if (viewType == BASE_HOLDER) {
            val binding: ItemBaseAddAlbumImageBinding =
                ItemBaseAddAlbumImageBinding.inflate(inflater, parent, false)
            return BaseAddAlbumImagesViewHolder(binding)
        } else {
            val binding: ItemAlbumImageBinding =
                ItemAlbumImageBinding.inflate(inflater, parent, false)
            return AlbumImageViewHolder(binding)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (getItemViewType(position)) {
            BASE_HOLDER -> {
                (holder as BaseAddAlbumImagesViewHolder).bind()

                holder.setBaseAddAlbumImagesListener(baseAddAlbumImagesListener!!)
            }

            IMAGE_HOLDER -> {
                val model = albumImages[position]
                (holder as AlbumImageViewHolder).bind(model)

                holder.setAlbumImageListCallback(albumImagesListListener!!)
            }
        }
    }

    override fun getItemCount(): Int {
        return albumImages.size
    }

    internal class AlbumImageViewHolder(private val binding: ItemAlbumImageBinding) :
        RecyclerView.ViewHolder(binding.getRoot()) {
        private val context: Context = binding.getRoot().context
        private var albumImagesListListener: AlbumImageListListener? = null

        fun setAlbumImageListCallback(albumImagesListListener: AlbumImageListListener) {
            this.albumImagesListListener = albumImagesListListener
        }

        fun bind(model: AlbumImage) {
            // show new image using uri
            if (model.imageUri != null) {
                binding.ivAlbumImage.setImageURI(model.imageUri)
            } else {
                // show old image using url
                UtilsGeneral.Companion.instance!!
                    .loadImage(context, model.imageUrl!!)
                    .into(binding.ivAlbumImage)
            }

            binding.btnDeleteAlbumImage.setOnClickListener {
                albumImagesListListener?.onClickDeleteListener(
                    model
                )
            }
        }
    }

    internal class BaseAddAlbumImagesViewHolder(private val binding: ItemBaseAddAlbumImageBinding) :
        RecyclerView.ViewHolder(binding.getRoot()) {
        private var listener: BaseAddAlbumImagesListener? = null

        fun setBaseAddAlbumImagesListener(listener: BaseAddAlbumImagesListener) {
            this.listener = listener
        }

        fun bind() {
            binding.buBaseAddAlbumImageCard.setOnClickListener { v ->
                listener!!.onClickAddListener()
            }
        }
    }

    interface AlbumImageListListener {
        fun onClickDeleteListener(model: AlbumImage)
    }

    interface BaseAddAlbumImagesListener {
        fun onClickAddListener()
    }

    companion object {
        private const val BASE_HOLDER = 0
        private const val IMAGE_HOLDER = 1
    }
}