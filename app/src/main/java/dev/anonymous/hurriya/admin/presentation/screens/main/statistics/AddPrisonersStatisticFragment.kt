package dev.anonymous.hurriya.admin.presentation.screens.main.statistics

import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts.GetContent
import androidx.activity.result.contract.ActivityResultContracts.OpenDocument
import androidx.navigation.fragment.navArgs
import com.google.firebase.Timestamp
import dev.anonymous.hurriya.admin.R
import dev.anonymous.hurriya.admin.databinding.FragmentAddPrisonersStatisticBinding
import dev.anonymous.hurriya.admin.firebase.controller.FirebaseController
import dev.anonymous.hurriya.admin.firebase.controller.FirebaseController.FirebaseCallback
import dev.anonymous.hurriya.admin.firebase.controller.FirebaseController.UploadFileCallback
import dev.anonymous.hurriya.admin.domain.models.Statistic
import dev.anonymous.hurriya.admin.presentation.components.BaseFragment
import dev.anonymous.hurriya.admin.utils.UtilsGeneral
import java.util.UUID

class AddPrisonersStatisticFragment :
    BaseFragment<FragmentAddPrisonersStatisticBinding>(FragmentAddPrisonersStatisticBinding::inflate) {
    private lateinit var firebaseController: FirebaseController
    private val args: AddPrisonersStatisticFragmentArgs by navArgs()

    private var pdfUri: Uri? = null
    private var iconUri: Uri? = null
    private var pdfName: String? = null
    private var iconName: String? = null

    private var newIconUrl: String? = null
    private var newPdfUrl: String? = null
    private var id: String? = null
    private var oldIconUrl: String? = null
    private var oldPdfUrl: String? = null
    private var title: String? = null
    private var number = 0

    private var deleteOldIcon = false
    private var deleteOldPdf = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        init()
    }

    private fun init() {
        firebaseController = FirebaseController.instance!!

        setupListeners()
        putDataIfExist()
    }

    private fun putDataIfExist() {
        args.model?.let {
            id = it.id
            oldPdfUrl = it.pdfUrl
            oldIconUrl = it.iconUrl

            binding.tvChoosePdfFile.setText(R.string._1_file_chooses)

            UtilsGeneral.instance!!
                .loadImage(requireContext(), it.iconUrl!!)
                .into(binding.ivImageStatistic)

            binding.etStatisticTitle.setText(it.title)
            binding.etStatisticNumber.setText(it.number.toString())

            setAppBarTitle(R.string.update_the_statistic)
            binding.btnAddStatistic.setText(R.string.update)
        }
    }

    private fun setupListeners() {
        this.binding.buChoosePdfFile.setOnClickListener {
            getPdfLauncher.launch(arrayOf("application/pdf"))
        }

        binding.buChooseImageStatistic.setOnClickListener {
            getStatisticIconLauncher.launch("image/*")
        }

        binding.btnAddStatistic.setOnClickListener {
            performSetStatistic()
        }
    }

    private fun checkData(title: String, number: String): Boolean {
        var isValidNumber = false
        if (!TextUtils.isEmpty(number)) {
            try {
                number.toInt()
                isValidNumber = true
            } catch (_: NumberFormatException) {
                println(getString(R.string.please_enter_a_valid_number))
            }
        }
        return !TextUtils.isEmpty(title) && isValidNumber &&  // If the user comes to add data
                ((pdfUri != null && iconUri != null) // Or comes to update data
                        || (oldPdfUrl != null && oldIconUrl != null))
    }

    private fun performSetStatistic() {
        val title = binding.etStatisticTitle.getText().toString().trim()
        val number = binding.etStatisticNumber.getText().toString().trim()

        if (checkData(title, number)) {
            showLoadingDialog()

            this.title = title
            this.number = number.toInt()

            // If the user comes to add data
            if (id == null) {
                id = UUID.randomUUID().toString()
            }
            checkFollowingProcess()
        }
    }

    private fun uploadStatisticsPdf() {
        firebaseController.uploadStatisticFile(
            id!!,
            pdfUri!!,
            pdfName!!,
            object : UploadFileCallback {
                override fun onSuccess(fileUrl: String) {
                    pdfUri = null
                    newPdfUrl = fileUrl

                    checkFollowingProcess()
                }

                override fun onFailure(errorMessage: String) {
                    dismissLoadingDialog()
                }
            })
    }

    private fun uploadStatisticIcon() {
        firebaseController.uploadStatisticFile(
            id!!,
            iconUri!!,
            iconName!!,
            object : UploadFileCallback {
                override fun onSuccess(fileUrl: String) {
                    iconUri = null
                    newIconUrl = fileUrl

                    checkFollowingProcess()
                }

                override fun onFailure(errorMessage: String) {
                    dismissLoadingDialog()
                }
            })
    }

    private fun setStatistics(statistic: Statistic) {
        firebaseController.setStatistic(statistic, object : FirebaseCallback {
            override fun onSuccess() {
                checkIfAdminChangePdfOrIcon()
            }

            override fun onFailure(errorMessage: String) {
                dismissLoadingDialog()
            }
        })
    }

    private fun deleteStatisticFiles(uuid: String) {
        firebaseController.deleteStatisticFiles(uuid, object : FirebaseCallback {
            override fun onSuccess() {
                dismissDialogAndFinishSuccessfully()
            }

            override fun onFailure(errorMessage: String) {
                dismissLoadingDialog()
            }
        })
    }

    private fun deleteFileUsingUrl(fileUrl: String) {
        firebaseController.deleteFileUsingUrl(fileUrl, object : FirebaseCallback {
            override fun onSuccess() {
                dismissDialogAndFinishSuccessfully()
            }

            override fun onFailure(errorMessage: String) {
                dismissLoadingDialog()
            }
        })
    }

    private val getPdfLauncher = registerForActivityResult(OpenDocument()) { result ->
        if (result != null) {
            pdfUri = result
            pdfName = UtilsGeneral.instance!!.getFileName(result, requireContext())

            // if user come to edit and changes the pdf
            if (oldPdfUrl != null) {
                deleteOldPdf = true
            } else {
                binding.tvChoosePdfFile.setText(R.string._1_file_chooses)
            }
        }
    }

    private val getStatisticIconLauncher = registerForActivityResult(GetContent()) { result ->
        if (result != null) {
            iconUri = result
            binding.ivImageStatistic.setImageURI(result)
            iconName = UtilsGeneral.instance!!.getFileName(result, requireContext())

            if (oldIconUrl != null) {
                deleteOldIcon = true
            }
        }
    }

    private fun dismissDialogAndFinishSuccessfully() {
        UtilsGeneral.instance!!.showToast(
            requireContext(),
            getString(R.string.task_completed_successfully)
        )
        dismissLoadingDialog()
        closeCurrentFragment()
    }

    private fun checkFollowingProcess() {
        if (pdfUri != null) {
            uploadStatisticsPdf()
        } else if (iconUri != null) {
            uploadStatisticIcon()
        } else {
            setStatistics(this.statistic)
        }
    }

    private fun checkIfAdminChangePdfOrIcon() {
        if (deleteOldIcon && deleteOldPdf) {
            deleteStatisticFiles(id!!)
        } else if (deleteOldIcon) {
            deleteFileUsingUrl(oldIconUrl!!)
        } else if (deleteOldPdf) {
            deleteFileUsingUrl(oldPdfUrl!!)
        } else {
            dismissDialogAndFinishSuccessfully()
        }
    }

    private val statistic: Statistic
        get() {
            val iconUrl = if (newIconUrl != null) newIconUrl else oldIconUrl
            val pdfUrl = if (newPdfUrl != null) newPdfUrl else oldPdfUrl

            return Statistic(
                id, title, iconUrl, number, pdfUrl, Timestamp.now()
            )
        }
}