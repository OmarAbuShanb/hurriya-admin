package dev.anonymous.hurriya.admin.presentation.screens.main.prisoners

import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts.GetContent
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.navArgs
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.firebase.Timestamp
import dev.anonymous.hurriya.admin.R
import dev.anonymous.hurriya.admin.databinding.FragmentAddPrisonerCardBinding
import dev.anonymous.hurriya.admin.domain.models.PrisonerCard
import dev.anonymous.hurriya.admin.firebase.controller.FirebaseController
import dev.anonymous.hurriya.admin.firebase.controller.FirebaseController.FirebaseCallback
import dev.anonymous.hurriya.admin.firebase.controller.FirebaseController.UploadFileCallback
import dev.anonymous.hurriya.admin.presentation.components.BaseFragment
import dev.anonymous.hurriya.admin.utils.UtilsGeneral
import java.util.Calendar
import java.util.Date
import java.util.TimeZone
import java.util.UUID

class AddPrisonerCardFragment :
    BaseFragment<FragmentAddPrisonerCardBinding>(FragmentAddPrisonerCardBinding::inflate) {
    private lateinit var firebaseController: FirebaseController
    private val args: AddPrisonerCardFragmentArgs by navArgs()

    private var imageUri: Uri? = null
    private var imageName: String? = null

    private var uuid: String? = null
    private var oldImageUrl: String? = null
    private var name: String? = null
    private var dateOfArrest: String? = null
    private var judgment: String? = null
    private var living: String? = null
    private var newImageUrl: String? = null

    private var deleteOldImage = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        init()
    }

    private fun init() {
        firebaseController = FirebaseController.instance!!

        putDate()
        setUpListeners()
    }

    private fun setUpListeners() {
        binding.btnAddPrisonerCard.setOnClickListener {
            performSetPrisonerCard()
        }
        binding.etSelectArrestedDate.setOnClickListener {
            showMaterialDatePicker()
        }

        binding.buChoosePrisonerImage.setOnClickListener {
            getPrisonerImageLauncher.launch(
                "image/*"
            )
        }
    }

    private fun putDate() {
        args.model?.let {
            uuid = it.id
            oldImageUrl = it.imageUrl

            UtilsGeneral.instance!!
                .loadImage(requireContext(), it.imageUrl!!)
                .into(binding.ivPrisoner)

            binding.etName.setText(it.name)

            binding.etSelectArrestedDate.text = it.dateOfArrest
            binding.etSelectArrestedDate.setTextColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.black
                )
            )

            binding.etJudgment.setText(it.judgment)
            binding.etLiving.setText(it.living)

            setAppBarTitle(R.string.update_the_prisoner_card)
            binding.btnAddPrisonerCard.setText(R.string.update)
        }
    }

    private fun checkData(
        name: String,
        dateOfArrest: String,
        judgment: String,
        living: String
    ): Boolean {
        return !TextUtils.isEmpty(name) && !TextUtils.isEmpty(dateOfArrest) && !TextUtils.isEmpty(
            judgment
        ) && !TextUtils.isEmpty(living) // If the user comes to add data
                && (imageUri != null // Or comes to update data
                || oldImageUrl != null)
    }

    private fun performSetPrisonerCard() {
        val name = binding.etName.getText().toString().trim()
        val dateOfArrest = binding.etSelectArrestedDate.getText().toString().trim()
        val judgment = binding.etJudgment.getText().toString().trim()
        val living = binding.etLiving.getText().toString().trim()

        if (checkData(name, dateOfArrest, judgment, living)) {
            this.name = name
            this.dateOfArrest = dateOfArrest
            this.judgment = judgment
            this.living = living

            if (uuid == null) {
                uuid = UUID.randomUUID().toString()
            }

            showLoadingDialog()
            checkFollowingProcess()
        }
    }

    private fun uploadImage() {
        firebaseController.uploadPrisonerImage(
            imageUri!!,
            imageName!!,
            object : UploadFileCallback {
                override fun onSuccess(fileUrl: String) {
                    imageUri = null
                    newImageUrl = fileUrl

                    checkFollowingProcess()
                }

                override fun onFailure(errorMessage: String) {
                    dismissLoadingDialog()
                }
            })
    }

    private fun setNews(prisonerCard: PrisonerCard) {
        firebaseController.setPrisonerCard(prisonerCard, object : FirebaseCallback {
            override fun onSuccess() {
                checkIfAdminChangePrisonerImage()
            }

            override fun onFailure(errorMessage: String) {
                dismissLoadingDialog()
            }
        })
    }

    private fun deletePrisonerImage(imageUrl: String) {
        firebaseController.deleteFileUsingUrl(imageUrl, object : FirebaseCallback {
            override fun onSuccess() {
                dismissDialogAndFinishSuccessfully()
            }

            override fun onFailure(errorMessage: String) {
                dismissLoadingDialog()
            }
        })
    }

    private fun dismissDialogAndFinishSuccessfully() {
        UtilsGeneral.instance!!.showToast(
            requireContext(),
            getString(R.string.task_completed_successfully)
        )
        dismissLoadingDialog()
        closeCurrentFragment()
    }

    private val getPrisonerImageLauncher = registerForActivityResult(GetContent()) { result ->
        if (result != null) {
            imageUri = result
            imageName = UtilsGeneral.instance!!.getFileName(result, requireContext())
            binding.ivPrisoner.setImageURI(result)

            if (oldImageUrl != null) {
                deleteOldImage = true
            }
        }
    }

    private fun showMaterialDatePicker() {
        val today = MaterialDatePicker.todayInUtcMilliseconds()

        val minCalendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
        minCalendar.set(1950, Calendar.JANUARY, 1)
        val minDate = minCalendar.timeInMillis

        val maxDate = MaterialDatePicker.todayInUtcMilliseconds()

        val constraintsBuilder =
            CalendarConstraints.Builder()
                .setStart(minDate)
                .setEnd(maxDate)
                .setOpenAt(today)

        val datePicker =
            MaterialDatePicker.Builder.datePicker()
                .setTitleText("اختر التاريخ")
                .setSelection(today)
                .setCalendarConstraints(constraintsBuilder.build())
                .build()

        datePicker.addOnPositiveButtonClickListener { selection ->
            if (selection != null) {
                val selectedDate = Date(selection)
                setUpEditTextSelectArrestedDate(selectedDate)
            }
        }

        datePicker.show(parentFragmentManager, "MATERIAL_DATE_PICKER")
    }

    private fun setUpEditTextSelectArrestedDate(selectedDate: Date) {
        val stringDate = UtilsGeneral.instance!!.getStringDateFromDate(selectedDate)
        binding.etSelectArrestedDate.text = stringDate
        binding.etSelectArrestedDate.setTextColor(
            ContextCompat.getColor(
                requireContext(),
                R.color.black
            )
        )
    }

    private fun checkFollowingProcess() {
        if (imageUri != null) {
            uploadImage()
        } else {
            setNews(this.prisonerCard)
        }
    }

    private fun checkIfAdminChangePrisonerImage() {
        if (deleteOldImage) {
            deletePrisonerImage(oldImageUrl!!)
        } else {
            dismissDialogAndFinishSuccessfully()
        }
    }

    private val prisonerCard: PrisonerCard
        get() {
            val imageUrl = if (newImageUrl != null) newImageUrl else oldImageUrl

            return PrisonerCard(
                uuid, imageUrl, name, dateOfArrest, judgment, living, Timestamp.now()
            )
        }
}