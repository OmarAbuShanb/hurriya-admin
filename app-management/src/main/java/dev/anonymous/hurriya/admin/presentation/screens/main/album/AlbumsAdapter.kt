package dev.anonymous.hurriya.admin.presentation.screens.main.album

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import dev.anonymous.hurriya.admin.databinding.ItemAlbumBinding
import dev.anonymous.hurriya.admin.domain.models.Album
import dev.anonymous.hurriya.admin.utils.UtilsGeneral

class AlbumsAdapter : RecyclerView.Adapter<AlbumsAdapter.AlbumsViewHolder>() {
    private var albums: ArrayList<Album>
    private var albumsListListener: AlbumsListListener? = null

    fun setAlbumsListListener(albumsListListener: AlbumsListListener) {
        this.albumsListListener = albumsListListener
    }

    init {
        albums = ArrayList<Album>()
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setData(albums: ArrayList<Album>) {
        this.albums = albums
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlbumsViewHolder {
        val binding = ItemAlbumBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return AlbumsViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AlbumsViewHolder, position: Int) {
        val album = albums[position]
        holder.bind(album)
        holder.setAlbumsListListener(albumsListListener!!)
    }

    override fun getItemCount(): Int {
        return albums.size
    }

    fun removeItem(uuid: String) {
        for (i in albums.indices) {
            if (albums[i].id == uuid) {
                albums.removeAt(i)
                notifyItemRemoved(i)
                break
            }
        }
    }

    class AlbumsViewHolder(private val binding: ItemAlbumBinding) :
        RecyclerView.ViewHolder(binding.getRoot()) {
        private val context: Context = binding.getRoot().context
        private var albumsListListener: AlbumsListListener? = null

        fun setAlbumsListListener(albumsListListener: AlbumsListListener) {
            this.albumsListListener = albumsListListener
        }

        fun bind(model: Album) {
            binding.albumTitle.text = model.title

            UtilsGeneral.Companion.instance!!
                .loadImage(context, model.imageUrl!!)
                .into(binding.ivAlbum)

            binding.buAlbumsCard.setOnClickListener {
                albumsListListener?.onClickAlbumItemListener(
                    model.id!!
                )
            }

            binding.btnDeleteAlbum.setOnClickListener {
                albumsListListener?.onClickDeleteListener(
                    model
                )
            }

            binding.btnUpdateAlbum.setOnClickListener {
                albumsListListener?.onClickUpdateListener(
                    model
                )
            }
        }
    }

    interface AlbumsListListener {
        fun onClickAlbumItemListener(albumId: String)

        fun onClickDeleteListener(album: Album)

        fun onClickUpdateListener(model: Album)
    }
}