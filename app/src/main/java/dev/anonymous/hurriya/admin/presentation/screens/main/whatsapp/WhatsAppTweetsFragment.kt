package dev.anonymous.hurriya.admin.presentation.screens.main.whatsapp

import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.Timestamp
import dev.anonymous.hurriya.admin.R
import dev.anonymous.hurriya.admin.presentation.screens.main.whatsapp.WhatsAppTweetsAdapter
import dev.anonymous.hurriya.admin.databinding.FragmentWhatsappTweetsBinding
import dev.anonymous.hurriya.admin.firebase.controller.FirebaseController
import dev.anonymous.hurriya.admin.firebase.controller.FirebaseController.FirebaseCallback
import dev.anonymous.hurriya.admin.firebase.controller.FirebaseController.GetDataCallback
import dev.anonymous.hurriya.admin.domain.models.WhatsappTweet
import dev.anonymous.hurriya.admin.presentation.components.BaseFragment
import dev.anonymous.hurriya.admin.utils.UtilsGeneral

class WhatsAppTweetsFragment :
    BaseFragment<FragmentWhatsappTweetsBinding>(FragmentWhatsappTweetsBinding::inflate) {
    private lateinit var firebaseController: FirebaseController
    private var whatsAppTweetsAdapter: WhatsAppTweetsAdapter? = null

    private val addTweetResultKey = "add_tweet_result"
    private val updateUrlResultKey = "update_whatsapp_url_result"

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        init()
    }

    private fun init() {
        firebaseController = FirebaseController.instance!!

        observeDialogResults()
        setUpListeners()
        setUpWhatsAppTweetsAdapter()
        attachItemTouchHelperToRecycler()
        this.whatsAppTweets
    }

    private fun observeDialogResults() {
        val savedStateHandle = findNavController().currentBackStackEntry?.savedStateHandle ?: return

        savedStateHandle.getLiveData<String>(addTweetResultKey)
            .observe(viewLifecycleOwner) { text ->
                val tweet = WhatsappTweet(text, Timestamp.now())
                setWhatsAppTweets(tweet)

                savedStateHandle.remove<String>(addTweetResultKey)
            }

        savedStateHandle.getLiveData<String>(updateUrlResultKey)
            .observe(viewLifecycleOwner) { url ->
                updateWhatsAppGroupUrl(url)

                savedStateHandle.remove<String>(updateUrlResultKey)
            }
    }

    private fun setUpListeners() {
        binding.floatPushTweets.setOnClickListener {
            findNavController().navigate(
                WhatsAppTweetsFragmentDirections.actionWhatsAppTweetsFragmentToGetTextDialog(
                    R.string.add_new_tweet,
                    R.string.enter_tweet_text,
                    R.string.add,
                    addTweetResultKey
                )
            )
        }

        binding.floatUpdateWhatsAppGroupUrl.setOnClickListener {
            findNavController().navigate(
                WhatsAppTweetsFragmentDirections.actionWhatsAppTweetsFragmentToGetTextDialog(
                    R.string.update_url_whatsapp_group,
                    R.string.enter_the_new_url,
                    R.string.update,
                    updateUrlResultKey
                )
            )
        }
    }

    private fun setUpWhatsAppTweetsAdapter() {
        whatsAppTweetsAdapter = WhatsAppTweetsAdapter()
        binding.whatsappTweetsRecycler.setLayoutManager(LinearLayoutManager(requireContext()))
        binding.whatsappTweetsRecycler.setAdapter(whatsAppTweetsAdapter)
        binding.whatsappTweetsRecycler.setHasFixedSize(true)
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
                    val whatsAppTweetId = whatsAppTweetsAdapter!!.removeWhatsAppMessage(position)
                    deleteWhatsAppTweets(whatsAppTweetId)
                }
            }
        }).attachToRecyclerView(binding.whatsappTweetsRecycler)
    }

    private val whatsAppTweets: Unit
        get() {
            firebaseController.getWhatsappTweets(object : GetDataCallback<WhatsappTweet> {
                override fun onSuccess(data: ArrayList<WhatsappTweet>) {
                    binding.progressWhatsAppMessages.visibility = View.GONE
                    whatsAppTweetsAdapter!!.setData(data)
                }

                override fun onFailure(errorMessage: String) {
                }
            })
        }

    private fun setWhatsAppTweets(whatsAppTweet: WhatsappTweet) {
        showLoadingDialog()
        firebaseController.setWhatsappTweets(whatsAppTweet, object : FirebaseCallback {
            override fun onSuccess() {
                dismissDialogAndWhatsUpMessageSuccessfully(whatsAppTweet)
            }

            override fun onFailure(errorMessage: String) {
                dismissLoadingDialog()
            }
        })
    }

    private fun dismissDialogAndWhatsUpMessageSuccessfully(whatsAppTweet: WhatsappTweet) {
        dismissLoadingDialog()
        UtilsGeneral.instance!!.showToast(
            requireContext(),
            getString(R.string.task_completed_successfully)
        )
        whatsAppTweetsAdapter!!.addWhatsAppMessage(whatsAppTweet)
    }

    private fun deleteWhatsAppTweets(whatsAppTweetId: String) {
        showLoadingDialog()
        firebaseController.deleteWhatsappTweets(whatsAppTweetId, object : FirebaseCallback {
            override fun onSuccess() {
                dismissLoadingDialog()
            }

            override fun onFailure(errorMessage: String) {
                dismissLoadingDialog()
            }
        })
    }

    private fun updateWhatsAppGroupUrl(newUrl: String) {
        showLoadingDialog()
        firebaseController.setWhatsAppGroupUrl(newUrl, object : FirebaseCallback {
            override fun onSuccess() {
                UtilsGeneral.instance!!.showToast(
                    requireContext(),
                    getString(R.string.join_url_updated)
                )
                dismissLoadingDialog()
            }

            override fun onFailure(errorMessage: String) {
                UtilsGeneral.instance!!.showToast(
                    requireContext(),
                    getString(R.string.something_went_wrong)
                )
                dismissLoadingDialog()
            }
        })
    }
}