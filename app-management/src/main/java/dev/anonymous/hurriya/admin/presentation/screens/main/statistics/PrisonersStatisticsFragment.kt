package dev.anonymous.hurriya.admin.presentation.screens.main.statistics

import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import dev.anonymous.hurriya.admin.presentation.screens.main.statistics.StatisticsAdapter
import dev.anonymous.hurriya.admin.presentation.screens.main.statistics.StatisticsAdapter.StatisticListListener
import dev.anonymous.hurriya.admin.databinding.FragmentPrisonersStatisticsBinding
import dev.anonymous.hurriya.admin.firebase.controller.FirebaseController
import dev.anonymous.hurriya.admin.firebase.controller.FirebaseController.FirebaseCallback
import dev.anonymous.hurriya.admin.firebase.controller.FirebaseController.GetDataCallback
import dev.anonymous.hurriya.admin.domain.models.Statistic
import dev.anonymous.hurriya.admin.presentation.components.BaseFragment
import dev.anonymous.hurriya.admin.utils.UtilsGeneral
import java.util.Collections

class PrisonersStatisticsFragment :
    BaseFragment<FragmentPrisonersStatisticsBinding>(FragmentPrisonersStatisticsBinding::inflate),
    StatisticListListener {
    private lateinit var firebaseController: FirebaseController

    private var statisticsAdapter: StatisticsAdapter? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        init()
    }

    private fun init() {
        firebaseController = FirebaseController.instance!!

        setupListeners()
        setupStatisticsAdapter()
        attachItemTouchHelperToRecycler()
        this.statistics
    }

    private fun setupListeners() {
        binding.floatAddStatistics.setOnClickListener {
            navigateTo(
                PrisonersStatisticsFragmentDirections
                    .actionPrisonersStatisticsFragmentToAddPrisonersStatisticFragment(null)
            )
        }
    }

    private fun setupStatisticsAdapter() {
        statisticsAdapter = StatisticsAdapter()
        binding.statisticsRecycler.setAdapter(statisticsAdapter)
        binding.statisticsRecycler.setLayoutManager(LinearLayoutManager(requireContext()))
        binding.statisticsRecycler.setHasFixedSize(true)

        statisticsAdapter!!.setStatisticListCallback(this)
    }

    private fun attachItemTouchHelperToRecycler() {
        ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(
            ItemTouchHelper.ACTION_STATE_IDLE,
            ItemTouchHelper.RIGHT
        ) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val uuid = statisticsAdapter!!.removeStatistic(position)
                    deleteStatistics(uuid)
                }
            }
        }).attachToRecyclerView(binding.statisticsRecycler)
    }

    private val statistics: Unit
        get() {
            binding.progressStatistics.visibility = View.VISIBLE
            binding.tvDateOfLastUpdate.visibility = View.GONE

            firebaseController.getStatistics(object : GetDataCallback<Statistic> {
                override fun onSuccess(data: ArrayList<Statistic>) {
                    binding.progressStatistics.visibility = View.GONE

                    if (!data.isEmpty()) {
                        statisticsAdapter!!.setData(data)
                        setDateOfLastStatisticsUpdate(data)
                    }
                }

                override fun onFailure(errorMessage: String) {
                }
            })
        }

    private fun setDateOfLastStatisticsUpdate(statistics: ArrayList<Statistic>) {
        // sort statistics using timestamp
        Collections.sort(
            statistics,
            Comparator { statistic1: Statistic, statistics2: Statistic ->
                statistic1.timestamp!!.compareTo(statistics2.timestamp!!)
            }
        )

        // get last statistic in array list
        val lastStatistic = statistics[statistics.size - 1]
        // last Statistic Timestamp to Date
        val lastStatisticDate = lastStatistic.timestamp!!.toDate()
        // format date to string => yyyy/MM/dd
        val lastUpdate = UtilsGeneral.instance!!.getStringDateFromDate(lastStatisticDate)

        binding.tvDateOfLastUpdate.text = lastUpdate
        binding.tvDateOfLastUpdate.visibility = View.VISIBLE
    }

    private fun deleteStatistics(uuid: String) {
        showLoadingDialog()
        firebaseController.deleteStatistic(uuid, object : FirebaseCallback {
            override fun onSuccess() {
                deleteStatisticFiles(uuid)
            }

            override fun onFailure(errorMessage: String) {
                dismissLoadingDialog()
            }
        })
    }

    private fun deleteStatisticFiles(uuid: String) {
        firebaseController.deleteStatisticFiles(uuid, object : FirebaseCallback {
            override fun onSuccess() {
                dismissLoadingDialog()
            }

            override fun onFailure(errorMessage: String) {
                dismissLoadingDialog()
            }
        })
    }

    override fun onClickStatisticItemListener(model: Statistic) {
        navigateTo(
            PrisonersStatisticsFragmentDirections
                .actionPrisonersStatisticsFragmentToAddPrisonersStatisticFragment(model)
        )
    }
}