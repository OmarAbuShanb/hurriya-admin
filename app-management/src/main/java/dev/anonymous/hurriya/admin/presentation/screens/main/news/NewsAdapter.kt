package dev.anonymous.hurriya.admin.presentation.screens.main.news

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import dev.anonymous.hurriya.admin.databinding.ItemNewsBinding
import dev.anonymous.hurriya.admin.domain.models.News
import dev.anonymous.hurriya.admin.utils.UtilsGeneral

class NewsAdapter : RecyclerView.Adapter<NewsAdapter.NewsViewHolder>() {
    private var news: ArrayList<News>
    private var newsListListener: NewsListListener? = null

    fun setNewsListCallback(newsListListener: NewsListListener) {
        this.newsListListener = newsListListener
    }

    init {
        this.news = ArrayList<News>()
    }

    fun setData(news: ArrayList<News>) {
        this.news = news
        notifyDataSetChanged()
    }

    fun removeItem(id: String?) {
        for (i in news.indices) {
            if (news[i].id == id) {
                news.removeAt(i)
                notifyItemRemoved(i)
                break
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NewsViewHolder {
        val binding = ItemNewsBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return NewsViewHolder(binding)
    }

    override fun onBindViewHolder(holder: NewsViewHolder, position: Int) {
        val model = news[position]
        holder.bind(model)

        holder.setNewsListCallback(newsListListener!!)
    }

    override fun getItemCount(): Int {
        return news.size
    }

    class NewsViewHolder(private val binding: ItemNewsBinding) :
        RecyclerView.ViewHolder(binding.getRoot()) {
        private val context: Context = binding.getRoot().context
        private var newsListListener: NewsListListener? = null

        fun setNewsListCallback(newsListListener: NewsListListener) {
            this.newsListListener = newsListListener
        }

        fun bind(model: News) {
            binding.newsTitle.text = model.title

            UtilsGeneral.Companion.instance!!
                .loadImage(context, model.imageUrl!!)
                .into(binding.ivNews)

            binding.buNewsCard.setOnClickListener {
                newsListListener?.onClickItemListener(
                    model
                )
            }

            binding.btnDeleteNews.setOnClickListener {
                newsListListener?.onClickDeleteListener(
                    model.id!!,
                    model.imageUrl!!
                )
            }
        }
    }

    interface NewsListListener {
        fun onClickItemListener(model: News)

        fun onClickDeleteListener(newsId: String, imageUrl: String)
    }
}