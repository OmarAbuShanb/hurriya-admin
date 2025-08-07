package dev.anonymous.hurriya.admin.presentation.screens.main.posters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import dev.anonymous.hurriya.admin.databinding.ItemPosterBinding
import dev.anonymous.hurriya.admin.domain.models.Poster
import dev.anonymous.hurriya.admin.utils.UtilsGeneral

class PrisonerPostersAdapter : RecyclerView.Adapter<PrisonerPostersAdapter.PosterViewHolder?>() {
    private var posters: ArrayList<Poster>

    private var deletePostersListListener: DeletePostersListListener? = null

    fun setDeletePrisonerPostersListListener(deletePostersListListener: DeletePostersListListener) {
        this.deletePostersListListener = deletePostersListListener
    }

    init {
        this.posters = ArrayList<Poster>()
    }

    fun setData(posters: ArrayList<Poster>) {
        this.posters = posters
        notifyDataSetChanged()
    }

    fun removeItem(id: String) {
        for (i in posters.indices) {
            if (posters[i].id.equals(id)) {
                posters.removeAt(i)
                notifyItemRemoved(i)
                break
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PosterViewHolder {
        val binding = ItemPosterBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return PosterViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PosterViewHolder, position: Int) {
        val poster = posters[position]
        holder.bind(poster)
        holder.setDeletePrisonerPostersListListener(deletePostersListListener)
    }

    override fun getItemCount(): Int {
        return posters.size
    }

    class PosterViewHolder(private val binding: ItemPosterBinding) :
        RecyclerView.ViewHolder(
            binding.getRoot()
        ) {
        private val context: Context = binding.getRoot().context

        private var deletePostersListListener: DeletePostersListListener? = null

        fun setDeletePrisonerPostersListListener(deletePostersListListener: DeletePostersListListener?) {
            this.deletePostersListListener = deletePostersListListener
        }

        fun bind(poster: Poster) {
            UtilsGeneral.Companion.instance!!
                .loadImage(context, poster.imageUrl!!)
                .fitCenter()
                .into(binding.ivPoster)

            binding.btnDeletePoster.setOnClickListener {
                deletePostersListListener?.onClickDeleteListener(
                    poster.id!!,
                    poster.imageUrl!!
                )
            }
        }
    }

    interface DeletePostersListListener {
        fun onClickDeleteListener(prisonerPosterId: String, imageUrl: String)
    }
}