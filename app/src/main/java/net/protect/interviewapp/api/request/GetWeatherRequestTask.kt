package net.protect.interviewapp.api.request

import io.reactivex.rxjava3.core.Observable
import net.protect.interviewapp.api.RequestTask
import net.protect.interviewapp.api.RestInterface
import net.protect.interviewapp.api.Singletons
import net.protect.interviewapp.api.response.GetWeatherResponse

class GetWeatherRequestTask(private val city: String, private val units: String = "metric") : RequestTask<GetWeatherResponse>() {

    override fun buildObservable(api: RestInterface): Observable<GetWeatherResponse> {
        return api.getWeather(city, Singletons.API_KEY!!, units)
    }
}