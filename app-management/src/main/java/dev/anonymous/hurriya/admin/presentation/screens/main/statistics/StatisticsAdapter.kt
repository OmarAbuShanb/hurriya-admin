package dev.anonymous.hurriya.admin.presentation.screens.main.statistics

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import dev.anonymous.hurriya.admin.databinding.ItemStatisticsBinding
import dev.anonymous.hurriya.admin.domain.models.Statistic
import dev.anonymous.hurriya.admin.utils.UtilsGeneral

class StatisticsAdapter : RecyclerView.Adapter<StatisticsAdapter.StatisticsViewHolder>() {
    private var statistics: ArrayList<Statistic>
    private var statisticListListener: StatisticListListener? = null

    init {
        this.statistics = ArrayList<Statistic>()
    }

    fun setStatisticListCallback(statisticListListener: StatisticListListener) {
        this.statisticListListener = statisticListListener
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setData(statistics: ArrayList<Statistic>) {
        this.statistics = statistics
        notifyDataSetChanged()
    }

    fun removeStatistic(position: Int): String {
        val id = statistics[position].id!!
        statistics.removeAt(position)
        notifyItemRemoved(position)
        return id
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StatisticsViewHolder {
        val binding = ItemStatisticsBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return StatisticsViewHolder(binding)
    }

    override fun onBindViewHolder(holder: StatisticsViewHolder, position: Int) {
        val model = statistics[position]
        holder.bind(model)

        holder.setAlbumsListCallback(statisticListListener)
    }

    override fun getItemCount(): Int {
        return statistics.size
    }

    class StatisticsViewHolder(private val binding: ItemStatisticsBinding) :
        RecyclerView.ViewHolder(
            binding.getRoot()
        ) {
        private val context: Context = binding.getRoot().context
        private var statisticListListener: StatisticListListener? = null

        fun setAlbumsListCallback(statisticListListener: StatisticListListener?) {
            this.statisticListListener = statisticListListener
        }

        fun bind(model: Statistic) {
            binding.tvStatisticsTitle.text = model.title
            binding.tvStatisticsNumber.text = model.number.toString()

            UtilsGeneral.Companion.instance!!
                .loadImage(context, model.iconUrl!!)
                .into(binding.ivStatisticsImage)

            binding.statisticsCard.setOnClickListener {
                statisticListListener?.onClickStatisticItemListener(
                    model
                )
            }
        }
    }

    interface StatisticListListener {
        fun onClickStatisticItemListener(model: Statistic)
    }
}