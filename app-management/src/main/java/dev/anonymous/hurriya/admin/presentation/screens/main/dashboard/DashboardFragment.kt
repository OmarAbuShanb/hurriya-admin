package dev.anonymous.hurriya.admin.presentation.screens.main.dashboard

import android.os.Bundle
import android.text.TextUtils
import android.view.View
import androidx.annotation.IdRes
import androidx.navigation.fragment.findNavController
import dev.anonymous.hurriya.admin.R
import dev.anonymous.hurriya.admin.databinding.FragmentDashboardBinding
import dev.anonymous.hurriya.admin.databinding.ItemDashboardCardBinding
import dev.anonymous.hurriya.admin.databinding.ItemSectionCardBinding
import dev.anonymous.hurriya.admin.firebase.controller.FirebaseController
import dev.anonymous.hurriya.admin.firebase.controller.FirebaseController.FirebaseCallback
import dev.anonymous.hurriya.admin.presentation.components.BaseFragment
import dev.anonymous.hurriya.admin.utils.UtilsGeneral


class DashboardFragment :
    BaseFragment<FragmentDashboardBinding>(FragmentDashboardBinding::inflate) {
    private lateinit var firebaseController: FirebaseController
    private lateinit var utilsGeneral: UtilsGeneral

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        init()
    }

    private fun init() {
        firebaseController = FirebaseController.instance!!
        utilsGeneral = UtilsGeneral.instance!!

        initializeDashboardCards()
        setupSections()
        setupListeners()
    }

    private fun initializeDashboardCards() {
        configureCard(
            binding.statisticsCard,
            R.string.prisoner_statistics,
            R.drawable.prisoners_statistics,
            R.id.prisonersStatisticsFragment
        )
        configureCard(
            binding.booksCard,
            R.string.prisoner_books,
            R.drawable.prisoners_books,
            R.id.prisonersBooksActivity
        )
        configureCard(
            binding.cardsCard,
            R.string.prisoner_cards,
            R.drawable.prisoners_cards,
            R.id.prisonersCardsFragment
        )
        configureCard(
            binding.postersCard,
            R.string.prisoner_posters,
            R.drawable.prisoners_designs,
            R.id.prisonersPostersFragment
        )
    }

    private fun configureCard(
        card: ItemDashboardCardBinding,
        titleResId: Int,
        iconResId: Int,
        @IdRes resId: Int
    ) {
        card.tvCardTitle.setText(titleResId)
        card.ivCardIcon.setImageResource(iconResId)
        card.cardItem.setOnClickListener {
            findNavController().navigate(resId)
        }
    }


    private fun setupSections() {
        configureSections(
            binding.newsSection,
            R.string.today_s_news,
            R.drawable.ic_public,
            R.id.newsFragment
        )

        configureSections(
            binding.albumsSection,
            R.string.albums,
            R.drawable.ic_image,
            R.id.albumsFragment
        )

        configureSections(
            binding.videosSection,
            R.string.videos,
            R.drawable.ic_video,
            R.id.videosFragment
        )

        configureSections(
            binding.whatsappTweetsSection,
            R.string.whatsapp_tweets,
            R.drawable.ic_whatsapp,
            R.id.whatsAppTweetsFragment

        )
    }

    private fun configureSections(
        section: ItemSectionCardBinding,
        titleResId: Int,
        iconResId: Int,
        @IdRes resId: Int
    ) {
        section.tvSectionTitle.setText(titleResId)
        section.ivSectionIcon.setImageResource(iconResId)
        section.cardSection.setOnClickListener {
            findNavController().navigate(resId)
        }
    }

    private fun setupListeners() {
        binding.btnUpdateUrgentNews.setOnClickListener {
            updateUrgentNews()
        }
        binding.floatPushNotification.setOnClickListener {
            findNavController().navigate(
                DashboardFragmentDirections.actionToSendNotificationFragment()
            )
        }
    }

    private fun updateUrgentNews() {
        val urgentNewsText = binding.edArgentNews.getText().toString().trim()

        if (!TextUtils.isEmpty(urgentNewsText)) {
            binding.edArgentNews.setText("")
            clearEditTextFocusIfHas()

            showLoadingDialog()
            firebaseController.setUrgentNews(urgentNewsText, object : FirebaseCallback {
                override fun onSuccess() {
                    utilsGeneral.showToast(
                        requireContext(),
                        getString(R.string.urgent_news_updated)
                    )
                    dismissLoadingDialog()
                }

                override fun onFailure(errorMessage: String) {
                    utilsGeneral.showToast(
                        requireContext(),
                        getString(R.string.something_went_wrong)
                    )
                    dismissLoadingDialog()
                }
            })
        }
    }

    override fun onStop() {
        super.onStop()

        clearEditTextFocusIfHas()
    }

    private fun clearEditTextFocusIfHas() {
        if (binding.edArgentNews.hasFocus()) {
            binding.edArgentNews.clearFocus()
        }
    }
}