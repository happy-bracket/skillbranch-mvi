package ru.hbracket.sbmvi.domain

import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.subjects.ReplaySubject

class ChatDataSource {

    private val subject = ReplaySubject.create<Message>()

    fun messages(): Observable<Message> {
        return subject
    }

    fun send(from: String, content: String): Single<Unit> {
        return Single.create { em ->
            em.onSuccess(subject.onNext(Message(from, content, System.currentTimeMillis())))
        }
    }

    fun getHistory(): Single<List<Message>> {
        return Single.just(listOf())
    }

}