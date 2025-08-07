package dev.anonymous.hurriya.admin.presentation.screens.main.book

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import dev.anonymous.hurriya.admin.databinding.ItemBooksBinding
import dev.anonymous.hurriya.admin.domain.models.Book
import dev.anonymous.hurriya.admin.utils.UtilsGeneral

class PrisonerBooksAdapter : RecyclerView.Adapter<PrisonerBooksAdapter.PrisonerBooksViewHolder>() {
    private var books: ArrayList<Book>
    private var prisonerBooksListListener: PrisonerBooksListListener? = null

    init {
        books = ArrayList<Book>()
    }

    fun setPrisonerBooksListListener(prisonerBooksListListener: PrisonerBooksListListener) {
        this.prisonerBooksListListener = prisonerBooksListListener
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setData(books: ArrayList<Book>) {
        this.books = books
        notifyDataSetChanged()
    }

    fun removeBook(uuid: String?) {
        for (i in books.indices) {
            if (books[i].id.equals(uuid)) {
                books.removeAt(i)
                notifyItemRemoved(i)
                break
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PrisonerBooksViewHolder {
        val binding = ItemBooksBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return PrisonerBooksViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PrisonerBooksViewHolder, position: Int) {
        val model = books[position]
        holder.bind(model)

        holder.setPrisonerBooksListListener(prisonerBooksListListener)
    }

    override fun getItemCount(): Int {
        return books.size
    }

    class PrisonerBooksViewHolder(private val binding: ItemBooksBinding) :
        RecyclerView.ViewHolder(binding.getRoot()) {
        private val context: Context = binding.getRoot().context

        private var prisonerBooksListListener: PrisonerBooksListListener? = null

        fun setPrisonerBooksListListener(prisonerBooksListListener: PrisonerBooksListListener?) {
            this.prisonerBooksListListener = prisonerBooksListListener
        }

        fun bind(model: Book) {
            binding.bookName.text = model.name
            binding.bookAuthor.text = model.author

            UtilsGeneral.Companion.instance!!
                .loadImage(context, model.imageUrl!!)
                .into(binding.bookImage)

            binding.buBookCard.setOnClickListener {
                prisonerBooksListListener?.onClickItemListener(
                    model
                )
            }

            binding.btnDeleteBook.setOnClickListener {
                prisonerBooksListListener?.onClickDeleteListener(
                    model.id!!
                )
            }
        }
    }

    interface PrisonerBooksListListener {
        fun onClickItemListener(model: Book)

        fun onClickDeleteListener(uuid: String)
    }
}