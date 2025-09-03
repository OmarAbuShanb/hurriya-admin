package dev.anonymous.hurriya.admin.presentation.screens.main.prisoners

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import dev.anonymous.hurriya.admin.databinding.ItemPrisonerCardBinding
import dev.anonymous.hurriya.admin.domain.models.PrisonerCard
import dev.anonymous.hurriya.admin.utils.UtilsGeneral

class PrisonerCardAdapter : RecyclerView.Adapter<PrisonerCardAdapter.PrisonerCardViewHolder>() {
    private var prisonerCards: ArrayList<PrisonerCard>
    private var prisonersCardsListListener: PrisonersCardsListListener? = null

    fun setNewsListCallback(prisonersCardsListListener: PrisonersCardsListListener) {
        this.prisonersCardsListListener = prisonersCardsListListener
    }

    init {
        this.prisonerCards = ArrayList<PrisonerCard>()
    }

    fun setPrisonerCards(prisonerCards: ArrayList<PrisonerCard>) {
        this.prisonerCards = prisonerCards
        notifyItemRangeInserted(0, prisonerCards.size)
    }

    fun removeItem(id: String) {
        for (i in prisonerCards.indices) {
            if (prisonerCards[i].id.equals(id)) {
                prisonerCards.removeAt(i)
                notifyItemRemoved(i)
                break
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PrisonerCardViewHolder {
        val binding = ItemPrisonerCardBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return PrisonerCardViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PrisonerCardViewHolder, position: Int) {
        val model = prisonerCards[position]
        holder.bind(model)

        holder.setNewsListCallback(prisonersCardsListListener)
    }

    override fun getItemCount(): Int {
        return prisonerCards.size
    }

    class PrisonerCardViewHolder(private val binding: ItemPrisonerCardBinding) :
        RecyclerView.ViewHolder(binding.getRoot()) {
        private val context: Context = binding.getRoot().context

        private var prisonersCardsListListener: PrisonersCardsListListener? = null

        fun setNewsListCallback(prisonersCardsListListener: PrisonersCardsListListener?) {
            this.prisonersCardsListListener = prisonersCardsListListener
        }

        fun bind(model: PrisonerCard) {
            binding.prisonerName.text = model.name

            UtilsGeneral.Companion.instance!!
                .loadImage(context, model.imageUrl!!)
                .into(binding.prisonerImage)

            binding.prisonerCard.setOnClickListener{
                prisonersCardsListListener!!.onClickItemListener(
                    model
                )
            }

            binding.btnDeletePrisonerCard.setOnClickListener{
                prisonersCardsListListener?.onClickDeleteListener(
                    model.id!!,
                    model.imageUrl!!
                )
            }
        }
    }

    interface PrisonersCardsListListener {
        fun onClickItemListener(model: PrisonerCard)

        fun onClickDeleteListener(prisonerCardId: String, imageUrl: String)
    }
}