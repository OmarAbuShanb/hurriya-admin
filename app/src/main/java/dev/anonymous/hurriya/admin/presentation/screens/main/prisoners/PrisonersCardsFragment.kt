package dev.anonymous.hurriya.admin.presentation.screens.main.prisoners

import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import dev.anonymous.hurriya.admin.presentation.screens.main.prisoners.PrisonerCardAdapter
import dev.anonymous.hurriya.admin.presentation.screens.main.prisoners.PrisonerCardAdapter.PrisonersCardsListListener
import dev.anonymous.hurriya.admin.databinding.FragmentPrisonersCardsBinding
import dev.anonymous.hurriya.admin.firebase.controller.FirebaseController
import dev.anonymous.hurriya.admin.firebase.controller.FirebaseController.FirebaseCallback
import dev.anonymous.hurriya.admin.firebase.controller.FirebaseController.GetDataCallback
import dev.anonymous.hurriya.admin.domain.models.PrisonerCard
import dev.anonymous.hurriya.admin.presentation.components.BaseFragment

class PrisonersCardsFragment :
    BaseFragment<FragmentPrisonersCardsBinding>(FragmentPrisonersCardsBinding::inflate),
    PrisonersCardsListListener {
    private lateinit var firebaseController: FirebaseController
    private var prisonerCardAdapter: PrisonerCardAdapter? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        init()
    }

    private fun init() {
        firebaseController = FirebaseController.instance!!

        setupListeners()
        setupPrisonerCardsAdapter()
        this.prisonersCards
    }

    private fun setupListeners() {
        binding.floatAddPrisoner.setOnClickListener {
            navigateTo(
                PrisonersCardsFragmentDirections
                    .actionPrisonersCardsFragmentToAddPrisonerCardFragment(null)
            )
        }
    }

    private fun setupPrisonerCardsAdapter() {
        prisonerCardAdapter = PrisonerCardAdapter()
        val manager = GridLayoutManager(requireContext(), 2)
        binding.prisonersCardsRecyclerView.setLayoutManager(manager)
        binding.prisonersCardsRecyclerView.setAdapter(prisonerCardAdapter)
        binding.prisonersCardsRecyclerView.setHasFixedSize(true)

        prisonerCardAdapter!!.setNewsListCallback(this)
    }

    private val prisonersCards: Unit
        get() {
            binding.progressPrisonersCards.visibility = View.VISIBLE
            firebaseController.getPrisonersCards(object : GetDataCallback<PrisonerCard> {
                override fun onSuccess(data: ArrayList<PrisonerCard>) {
                    binding.progressPrisonersCards.visibility = View.GONE
                    prisonerCardAdapter!!.setPrisonerCards(data)
                }

                override fun onFailure(errorMessage: String) {
                }
            })
        }

    private fun deletePrisonerCard(prisonerCardId: String, imageUrl: String) {
        showLoadingDialog()
        firebaseController.deletePrisonerCard(prisonerCardId, object : FirebaseCallback {
            override fun onSuccess() {
                prisonerCardAdapter!!.removeItem(prisonerCardId)
                deletePrisonerImage(imageUrl)
            }

            override fun onFailure(errorMessage: String) {
                dismissLoadingDialog()
            }
        })
    }

    private fun deletePrisonerImage(imageUrl: String) {
        firebaseController.deleteFileUsingUrl(imageUrl, object : FirebaseCallback {
            override fun onSuccess() {
                dismissLoadingDialog()
            }

            override fun onFailure(errorMessage: String) {
                dismissLoadingDialog()
            }
        })
    }

    override fun onClickItemListener(model: PrisonerCard) {
        navigateTo(
            PrisonersCardsFragmentDirections
                .actionPrisonersCardsFragmentToAddPrisonerCardFragment(model)
        )
    }

    override fun onClickDeleteListener(prisonerCardId: String, imageUrl: String) {
        deletePrisonerCard(prisonerCardId, imageUrl)
    }
}