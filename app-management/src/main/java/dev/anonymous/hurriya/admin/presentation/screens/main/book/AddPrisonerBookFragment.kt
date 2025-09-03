package dev.anonymous.hurriya.admin.presentation.screens.main.book

import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts.GetContent
import androidx.activity.result.contract.ActivityResultContracts.OpenDocument
import androidx.navigation.fragment.navArgs
import com.google.firebase.Timestamp
import dev.anonymous.hurriya.admin.R
import dev.anonymous.hurriya.admin.databinding.FragmentAddPrisonerBookBinding
import dev.anonymous.hurriya.admin.firebase.controller.FirebaseController
import dev.anonymous.hurriya.admin.firebase.controller.FirebaseController.FirebaseCallback
import dev.anonymous.hurriya.admin.firebase.controller.FirebaseController.UploadFileCallback
import dev.anonymous.hurriya.admin.domain.models.Book
import dev.anonymous.hurriya.admin.presentation.components.BaseFragment
import dev.anonymous.hurriya.admin.utils.UtilsGeneral
import java.util.UUID

class AddPrisonerBookFragment :
    BaseFragment<FragmentAddPrisonerBookBinding>(FragmentAddPrisonerBookBinding::inflate) {
    private lateinit var firebaseController: FirebaseController
    private val args: AddPrisonerBookFragmentArgs by navArgs()

    private var pdfUri: Uri? = null
    private var pdfImageUri: Uri? = null
    private var pdfName: String? = null
    private var pdfImageName: String? = null

    private var newImageUrl: String? = null
    private var newPdfUrl: String? = null
    private var uuid: String? = null
    private var oldPdfUrl: String? = null
    private var oldImageUrl: String? = null
    private var bookName: String? = null
    private var bookAuthor: String? = null

    private var deleteOldImage = false
    private var deleteOldPdf = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        init()
    }

    private fun init() {
        firebaseController = FirebaseController.instance!!

        setUpListeners()
        putDateIfExist()
    }

    private fun setUpListeners() {
        binding.buChoosePdfFile.setOnClickListener {
            getPdfLauncher.launch(arrayOf("application/pdf"))
        }

        binding.buChooseImageNews.setOnClickListener {
            getPdfImageLauncher.launch("image/*")
        }

        binding.btnAddBook.setOnClickListener {
            performSetBook()
        }
    }

    private fun putDateIfExist() {
        args.model?.let {
            uuid = it.id
            oldPdfUrl = it.pdfUrl
            oldImageUrl = it.imageUrl

            UtilsGeneral.instance!!
                .loadImage(requireContext(), it.imageUrl!!)
                .into(binding.ivImageBook)

            binding.buChoosePdfFile.visibility = View.GONE
            binding.etBookName.setText(it.name)
            binding.etAuthor.setText(it.author)

            setAppBarTitle(R.string.update_the_book)
            binding.btnAddBook.setText(R.string.update)
        }
    }

    private fun checkData(title: String?, description: String?): Boolean {
        return !TextUtils.isEmpty(title) && !TextUtils.isEmpty(description) &&  // If the user comes to add data
                ((pdfUri != null && pdfImageUri != null) // Or comes to update data
                        || (oldPdfUrl != null && oldImageUrl != null))
    }

    private fun performSetBook() {
        val bookName = binding.etBookName.getText().toString().trim { it <= ' ' }
        val bookAuthor = binding.etAuthor.getText().toString().trim { it <= ' ' }

        if (checkData(bookName, bookAuthor)) {
            this.bookName = bookName
            this.bookAuthor = bookAuthor

            if (uuid == null) {
                uuid = UUID.randomUUID().toString()
            }

            showLoadingDialog()
            checkFollowingProcess()
        }
    }

    private fun uploadPdfFile() {
        firebaseController.uploadBookFile(
            uuid!!,
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

    private fun uploadImage() {
        firebaseController.uploadBookFile(
            uuid!!,
            pdfImageUri!!,
            pdfImageName!!,
            object : UploadFileCallback {
                override fun onSuccess(fileUrl: String) {
                    pdfImageUri = null
                    newImageUrl = fileUrl

                    checkFollowingProcess()
                }

                override fun onFailure(errorMessage: String) {
                    dismissLoadingDialog()
                }
            })
    }

    private fun setBook(book: Book) {
        firebaseController.setBook(book, object : FirebaseCallback {
            override fun onSuccess() {
                checkIfAdminChangePdfOrIcon()
            }

            override fun onFailure(errorMessage: String) {
                dismissLoadingDialog()
            }
        })
    }

    private fun deleteBookFiles(uuid: String) {
        firebaseController.deleteBookFiles(uuid, object : FirebaseCallback {
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

    private fun dismissDialogAndFinishSuccessfully() {
        UtilsGeneral.instance!!
            .showToast(requireContext(), getString(R.string.task_completed_successfully))
        dismissLoadingDialog()
        closeCurrentFragment()
    }

    private val getPdfLauncher = registerForActivityResult(OpenDocument()) { result: Uri? ->
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

    private val getPdfImageLauncher = registerForActivityResult(GetContent()) { result: Uri? ->
        if (result != null) {
            pdfImageUri = result
            binding.ivImageBook.setImageURI(result)
            pdfImageName = UtilsGeneral.instance!!.getFileName(result, requireContext())

            if (oldImageUrl != null) {
                deleteOldImage = true
            }
        }
    }

    private fun checkFollowingProcess() {
        if (pdfUri != null) {
            uploadPdfFile()
        } else if (pdfImageUri != null) {
            uploadImage()
        } else {
            setBook(this.statistic)
        }
    }

    private fun checkIfAdminChangePdfOrIcon() {
        if (deleteOldImage && deleteOldPdf) {
            deleteBookFiles(uuid!!)
        } else if (deleteOldImage) {
            deleteFileUsingUrl(oldImageUrl!!)
        } else if (deleteOldPdf) {
            deleteFileUsingUrl(oldPdfUrl!!)
        } else {
            dismissDialogAndFinishSuccessfully()
        }
    }

    private val statistic: Book
        get() {
            val imageUrl = if (newImageUrl != null) newImageUrl else oldImageUrl
            val pdfUrl = if (newPdfUrl != null) newPdfUrl else oldPdfUrl

            return Book(
                uuid, imageUrl, bookName, bookAuthor, pdfUrl, Timestamp.now()
            )
        }
}