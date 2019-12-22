package ru.hbracket.sbmvi.mvi

import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject

class RxFeature<S, M, E>(
    initialState: S,
    initialEffects: Set<E>,
    private val update: (S, M) -> Pair<S, Set<E>>,
    private val handler: Handler<E, M>
) {

    private val states = BehaviorSubject.create<S>()
    private val mutations = PublishSubject.create<M>()

    private val featureLC: Disposable

    init {
        featureLC = mutations
            .observeOn(Schedulers.single())
            .scan(initialState to initialEffects) { (s, _), m ->
                update(s, m)
            }.subscribe { (s, es) ->
                states.onNext(s)
                es.forEach { e ->
                    handler.handle(e, ::mutate)
                }
            }
    }

    fun mutate(mutation: M) {
        mutations.onNext(mutation)
    }

    fun state(): Observable<S> =
        states

}

interface Handler<E, M> {

    fun handle(effect: E, sink: (M) -> Unit)

}