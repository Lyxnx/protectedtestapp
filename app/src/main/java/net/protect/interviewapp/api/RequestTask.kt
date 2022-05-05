package net.protect.interviewapp.api

import io.reactivex.rxjava3.core.Observable

abstract class RequestTask<R : Any> {

    val observable: Observable<R> by lazy {
        buildObservable(RestInterface.INSTANCE)
    }

    protected abstract fun buildObservable(api: RestInterface): Observable<R>
}