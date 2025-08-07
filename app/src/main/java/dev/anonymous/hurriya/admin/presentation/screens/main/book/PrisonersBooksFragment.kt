package dev.anonymous.hurriya.admin.presentation.screens.main.book

import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import dev.anonymous.hurriya.admin.presentation.screens.main.book.PrisonerBooksAdapter
import dev.anonymous.hurriya.admin.presentation.screens.main.book.PrisonerBooksAdapter.PrisonerBooksListListener
import dev.anonymous.hurriya.admin.databinding.FragmentPrisonersBooksBinding
import dev.anonymous.hurriya.admin.firebase.controller.FirebaseController
import dev.anonymous.hurriya.admin.firebase.controller.FirebaseController.FirebaseCallback
import dev.anonymous.hurriya.admin.firebase.controller.FirebaseController.GetDataCallback
import dev.anonymous.hurriya.admin.domain.models.Book
import dev.anonymous.hurriya.admin.presentation.components.BaseFragment

class PrisonersBooksFragment :
    BaseFragment<FragmentPrisonersBooksBinding>(FragmentPrisonersBooksBinding::inflate),
    PrisonerBooksListListener {
    private lateinit var firebaseController: FirebaseController

    private var prisonerBooksAdapter: PrisonerBooksAdapter? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        init()
    }

    private fun init() {
        firebaseController = FirebaseController.instance!!

        setupListeners()
        setupNewsAdapter()
        this.books
    }

    private fun setupListeners() {
        binding.floatAddBook.setOnClickListener {
            navigateTo(
                PrisonersBooksFragmentDirections
                    .actionPrisonersBooksActivityToAddPrisonerBookFragment(null)
            )
        }
    }

    private fun setupNewsAdapter() {
        prisonerBooksAdapter = PrisonerBooksAdapter()
        binding.booksRecyclerView.setAdapter(prisonerBooksAdapter)
        val manager
                : RecyclerView.LayoutManager =
            StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
        binding.booksRecyclerView.setLayoutManager(manager)
        binding.booksRecyclerView.setHasFixedSize(true)

        prisonerBooksAdapter!!.setPrisonerBooksListListener(this)
    }

    private val books: Unit
        get() {
            binding.progressBooks.visibility = View.VISIBLE
            firebaseController.getBooks(object : GetDataCallback<Book> {
                override fun onSuccess(data: ArrayList<Book>) {
                    binding.progressBooks.visibility = View.GONE
                    prisonerBooksAdapter!!.setData(data)
                }

                override fun onFailure(errorMessage: String) {
                }
            })
        }

    private fun deleteBook(uuid: String) {
        showLoadingDialog()
        firebaseController.deleteBook(uuid, object : FirebaseCallback {
            override fun onSuccess() {
                prisonerBooksAdapter!!.removeBook(uuid)
                deleteBookFiles(uuid)
            }

            override fun onFailure(errorMessage: String) {
                dismissLoadingDialog()
            }
        })
    }

    private fun deleteBookFiles(uuid: String) {
        firebaseController.deleteBookFiles(uuid, object : FirebaseCallback {
            override fun onSuccess() {
                dismissLoadingDialog()
            }

            override fun onFailure(errorMessage: String) {
                dismissLoadingDialog()
            }
        })
    }

    override fun onClickItemListener(model: Book) {
        navigateTo(
            PrisonersBooksFragmentDirections
                .actionPrisonersBooksActivityToAddPrisonerBookFragment(model)
        )
    }

    override fun onClickDeleteListener(uuid: String) {
        deleteBook(uuid)
    }
}