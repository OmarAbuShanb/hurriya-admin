package dev.anonymous.hurriya.admin.presentation.screens.main.videos

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import dev.anonymous.hurriya.admin.databinding.ItemVideoBinding
import dev.anonymous.hurriya.admin.domain.models.Video
import dev.anonymous.hurriya.admin.utils.UtilsGeneral

class VideosAdapter : RecyclerView.Adapter<VideosAdapter.NewsViewHolder>() {
    private var videos: ArrayList<Video>
    private var videosListListener: VideosListListener? = null

    fun setVideosListCallback(videosListListener: VideosListListener) {
        this.videosListListener = videosListListener
    }

    init {
        this.videos = ArrayList<Video>()
    }

    fun setData(videos: ArrayList<Video>) {
        this.videos = videos
        notifyDataSetChanged()
    }

    fun removeItem(id: String) {
        for (i in videos.indices) {
            if (videos[i].id == id) {
                videos.removeAt(i)
                notifyItemRemoved(i)
                break
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NewsViewHolder {
        val binding = ItemVideoBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return NewsViewHolder(binding)
    }

    override fun onBindViewHolder(holder: NewsViewHolder, position: Int) {
        val video = videos[position]
        holder.bind(video)

        holder.setVideosListCallback(videosListListener)
    }

    override fun getItemCount(): Int {
        return videos.size
    }

    class NewsViewHolder(private val binding: ItemVideoBinding) :
        RecyclerView.ViewHolder(binding.getRoot()) {
        private val context: Context = binding.getRoot().context
        private var videosListListener: VideosListListener? = null

        fun setVideosListCallback(videosListListener: VideosListListener?) {
            this.videosListListener = videosListListener
        }

        fun bind(model: Video) {
            binding.videoTitle.text = model.title

            UtilsGeneral.Companion.instance!!
                .loadImage(context, model.videoImageUrl!!)
                .into(binding.ivNews)

            binding.buVideoCard.setOnClickListener {
                videosListListener?.onClickVideoItemListener(
                    model
                )
            }

            binding.buDeleteVideo.setOnClickListener {
                videosListListener?.onClickDeleteListener(
                    model.id!!
                )
            }
        }
    }

    interface VideosListListener {
        fun onClickVideoItemListener(model: Video)

        fun onClickDeleteListener(id: String)
    }
}