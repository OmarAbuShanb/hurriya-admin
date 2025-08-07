package dev.anonymous.hurriya.admin.presentation.screens.main.posters

import android.os.Bundle
import android.view.View
import dev.anonymous.hurriya.admin.presentation.screens.main.posters.PrisonerPostersAdapter
import dev.anonymous.hurriya.admin.presentation.screens.main.posters.PrisonerPostersAdapter.DeletePostersListListener
import dev.anonymous.hurriya.admin.databinding.FragmentPrisonersPostersBinding
import dev.anonymous.hurriya.admin.firebase.controller.FirebaseController
import dev.anonymous.hurriya.admin.firebase.controller.FirebaseController.FirebaseCallback
import dev.anonymous.hurriya.admin.firebase.controller.FirebaseController.GetDataCallback
import dev.anonymous.hurriya.admin.domain.models.Poster
import dev.anonymous.hurriya.admin.presentation.components.BaseFragment

class PrisonersPostersFragment :
    BaseFragment<FragmentPrisonersPostersBinding>(FragmentPrisonersPostersBinding::inflate),
    DeletePostersListListener {
    private lateinit var firebaseController: FirebaseController

    private var prisonerPostersAdapter: PrisonerPostersAdapter? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        init()
    }

    private fun init() {
        firebaseController = FirebaseController.instance!!

        setupListeners()
        setupPrisonerPostersAdapter()
        this.posters
    }

    private fun setupListeners() {
        binding.floatAddPoster.setOnClickListener {
            navigateTo(
                PrisonersPostersFragmentDirections
                    .actionPrisonersPostersFragmentToAddPrisonerPosterFragment()
            )
        }
    }

    private fun setupPrisonerPostersAdapter() {
        prisonerPostersAdapter = PrisonerPostersAdapter()
        binding.postersPager.setAdapter(prisonerPostersAdapter)

        prisonerPostersAdapter!!.setDeletePrisonerPostersListListener(this)
    }

    private val posters: Unit
        get() {
            binding.progressPrisonersPosters.visibility = View.VISIBLE
            firebaseController.getPosters(object : GetDataCallback<Poster> {
                override fun onSuccess(data: ArrayList<Poster>) {
                    binding.progressPrisonersPosters.visibility = View.GONE
                    prisonerPostersAdapter!!.setData(data)
                }

                override fun onFailure(errorMessage: String) {
                }
            })
        }

    private fun deletePoster(prisonerPosterId: String, imageUrl: String) {
        showLoadingDialog()
        firebaseController.deletePoster(prisonerPosterId, object : FirebaseCallback {
            override fun onSuccess() {
                prisonerPostersAdapter!!.removeItem(prisonerPosterId)
                deletePosterImage(imageUrl)
            }

            override fun onFailure(errorMessage: String) {
                dismissLoadingDialog()
            }
        })
    }

    private fun deletePosterImage(imageUrl: String) {
        firebaseController.deleteFileUsingUrl(imageUrl, object : FirebaseCallback {
            override fun onSuccess() {
                dismissLoadingDialog()
            }

            override fun onFailure(errorMessage: String) {
                dismissLoadingDialog()
            }
        })
    }

    override fun onClickDeleteListener(prisonerPosterId: String, imageUrl: String) {
        deletePoster(prisonerPosterId, imageUrl)
    }
}