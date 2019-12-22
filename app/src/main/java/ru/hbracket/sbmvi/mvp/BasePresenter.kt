package ru.hbracket.sbmvi.mvp

abstract class BasePresenter<V> {

    protected var view: V? = null
    private var neverAttached: Boolean = true

    fun attachView(view: V) {
        this.view = view
        if (neverAttached) {
            neverAttached = false
            afterFirstAttach(view)
        }
        afterViewAttached(view)
    }

    fun detachView() {
        view?.let { v ->
            beforeViewDetach(v)
            view = null
        }
    }

    open fun afterViewAttached(view: V) {}

    open fun afterFirstAttach(view: V) {}

    open fun beforeViewDetach(view: V) {}

}