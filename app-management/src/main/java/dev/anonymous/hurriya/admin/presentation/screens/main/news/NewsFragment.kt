package dev.anonymous.hurriya.admin.presentation.screens.main.news

import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import dev.anonymous.hurriya.admin.presentation.screens.main.news.NewsAdapter
import dev.anonymous.hurriya.admin.presentation.screens.main.news.NewsAdapter.NewsListListener
import dev.anonymous.hurriya.admin.databinding.FragmentNewsBinding
import dev.anonymous.hurriya.admin.firebase.controller.FirebaseController
import dev.anonymous.hurriya.admin.firebase.controller.FirebaseController.FirebaseCallback
import dev.anonymous.hurriya.admin.firebase.controller.FirebaseController.GetDataCallback
import dev.anonymous.hurriya.admin.domain.models.News
import dev.anonymous.hurriya.admin.presentation.components.BaseFragment

class NewsFragment : BaseFragment<FragmentNewsBinding>(FragmentNewsBinding::inflate),
    NewsListListener {
    private lateinit var firebaseController: FirebaseController

    private var newsAdapter: NewsAdapter? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        init()
    }

    private fun init() {
        firebaseController = FirebaseController.instance!!

        setupListeners()
        setupNewsAdapter()
        this.news
    }

    private fun setupListeners() {
        binding.floatAddNews.setOnClickListener {
            navigateTo(
                NewsFragmentDirections.actionNewsFragmentToAddNewsFragment(null)
            )
        }
    }

    private fun setupNewsAdapter() {
        newsAdapter = NewsAdapter()
        binding.newsRecyclerView.setAdapter(newsAdapter)
        binding.newsRecyclerView.setLayoutManager(LinearLayoutManager(requireContext()))
        binding.newsRecyclerView.setHasFixedSize(true)

        newsAdapter!!.setNewsListCallback(this)
    }

    private val news: Unit
        get() {
            binding.progressNews.visibility = View.VISIBLE
            firebaseController.getNews(object : GetDataCallback<News> {
                override fun onSuccess(data: ArrayList<News>) {
                    binding.progressNews.visibility = View.GONE
                    newsAdapter!!.setData(data)
                }

                override fun onFailure(errorMessage: String) {
                }
            })
        }

    private fun deleteNews(newsId: String, imageUrl: String) {
        showLoadingDialog()
        firebaseController.deleteNews(newsId, object : FirebaseCallback {
            override fun onSuccess() {
                newsAdapter!!.removeItem(newsId)
                deleteNewsImage(imageUrl)
            }

            override fun onFailure(errorMessage: String) {
                dismissLoadingDialog()
            }
        })
    }

    private fun deleteNewsImage(imageUrl: String) {
        firebaseController.deleteFileUsingUrl(imageUrl, object : FirebaseCallback {
            override fun onSuccess() {
                dismissLoadingDialog()
            }

            override fun onFailure(errorMessage: String) {
                dismissLoadingDialog()
            }
        })
    }

    override fun onClickItemListener(model: News) {
        navigateTo(
            NewsFragmentDirections.actionNewsFragmentToAddNewsFragment(model)
        )
    }

    override fun onClickDeleteListener(newsId: String, imageUrl: String) {
        deleteNews(newsId, imageUrl)
    }
}