package dev.anonymous.hurriya.admin.presentation.screens.main.whatsapp

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import dev.anonymous.hurriya.admin.databinding.ItemWhatsappTweetBinding
import dev.anonymous.hurriya.admin.domain.models.WhatsappTweet

class WhatsAppTweetsAdapter :
    RecyclerView.Adapter<WhatsAppTweetsAdapter.WhatsappTweetsViewHolder>() {
    private var whatsappTweets: ArrayList<WhatsappTweet>

    init {
        this.whatsappTweets = ArrayList<WhatsappTweet>()
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setData(whatsappTweets: ArrayList<WhatsappTweet>) {
        this.whatsappTweets = whatsappTweets
        notifyDataSetChanged()
    }

    fun addWhatsAppMessage(message: WhatsappTweet) {
        whatsappTweets.add(message)
        notifyItemInserted(whatsappTweets.size - 1)
    }

    fun removeWhatsAppMessage(position: Int): String {
        val id = whatsappTweets[position].id!!
        whatsappTweets.removeAt(position)
        notifyItemRemoved(position)
        return id
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WhatsappTweetsViewHolder {
        val binding = ItemWhatsappTweetBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return WhatsappTweetsViewHolder(binding)
    }

    override fun onBindViewHolder(holder: WhatsappTweetsViewHolder, position: Int) {
        val whatsAppTweet: WhatsappTweet = whatsappTweets[position]
        holder.bind(whatsAppTweet)
    }

    override fun getItemCount(): Int {
        return whatsappTweets.size
    }

    class WhatsappTweetsViewHolder(private val binding: ItemWhatsappTweetBinding) :
        RecyclerView.ViewHolder(binding.getRoot()) {
        fun bind(whatsAppTweet: WhatsappTweet) {
            binding.whatsAppTweet.text = whatsAppTweet.message
        }
    }
}