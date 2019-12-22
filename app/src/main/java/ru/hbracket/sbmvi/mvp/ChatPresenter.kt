package ru.hbracket.sbmvi.mvp

import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import ru.hbracket.sbmvi.domain.ChatDataSource
import ru.hbracket.sbmvi.domain.Message

class ChatPresenter : BasePresenter<IChatView>() {

    private val dataSource = ChatDataSource()

    private var disposables: CompositeDisposable? = null
    private var messages = mutableListOf<Message>()

    private lateinit var username: String

    override fun afterFirstAttach(view: IChatView) {
        disposables = CompositeDisposable()
        dataSource.getHistory()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ msgs ->
                this.view?.setLoading(false)
                this.view?.addMessages(msgs)
                messages.addAll(msgs)
                messages.sortBy { it.createdAt }
            }, ::onError)
            .also { disposables?.add(it) }
    }

    override fun afterViewAttached(view: IChatView) {
        if (disposables == null)
            disposables = CompositeDisposable()
        dataSource.messages()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { msg ->
                this.view?.addMessages(listOf(msg))
            }.also { disposables?.add(it) }
    }

    override fun beforeViewDetach(view: IChatView) {
        disposables?.dispose()
        disposables = null
    }

    fun setUser(username: String) {
        this.username = username
    }

    fun sendMessage(text: String) {
        dataSource
            .send(username, text)
            .subscribe()
            .also { disposables?.add(it) }
    }

    private fun onError(err: Throwable) {
        view?.showError(err)
    }

}