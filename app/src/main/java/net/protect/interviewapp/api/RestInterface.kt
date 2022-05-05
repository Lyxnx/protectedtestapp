package net.protect.interviewapp.api

import com.google.gson.Gson
import com.google.gson.JsonObject
import io.reactivex.rxjava3.core.Observable
import net.protect.interviewapp.api.response.GetWeatherResponse
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import retrofit2.Converter
import retrofit2.Retrofit
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import java.lang.reflect.Type

interface RestInterface {

    companion object {
        const val BASE_URL = "https://api.openweathermap.org"

        val INSTANCE: RestInterface by lazy {
            Retrofit.Builder()
                .addConverterFactory(WeatherResponseConverter())
                .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
                .client(OkHttpClient.Builder()
                    .addInterceptor {
                        /*
                        If we don't have this here, the onError in rx will be called which we don't want
                        The API throws a return code even though it has an error JSON body which is what we want instead
                         */
                        val response = it.proceed(it.request())
                        val responseString = response.body()!!.string()
                        val responseType = response.body()!!.contentType()

                        val builder = response.newBuilder()
                            .body(ResponseBody.create(responseType, responseString))

                        // Just update the code to be "correct" so we can parse the body our way
                        if (response.code() != 200) {
                            builder.code(200)
                        }

                        return@addInterceptor builder.build()
                    }
                    .build())
                .baseUrl(BASE_URL)
                .build()
                .create(RestInterface::class.java)
        }
    }

    @GET("/data/2.5/weather")
    fun getWeather(
        @Query("q") city: String,
        @Query("appid") apiKey: String,
        @Query("units") units: String
    ): Observable<GetWeatherResponse>

    private class WeatherResponseConverter : Converter.Factory() {
        override fun responseBodyConverter(type: Type, annotations: Array<Annotation>, retrofit: Retrofit): Converter<ResponseBody, *> {
            return WeatherResponseBodyConverter.INSTANCE
        }
    }

    /*
    The response from this request isn't as simple as converting to a POJO with SerializedName
    since the temp field is contained within a field named "main"
     */

    private class WeatherResponseBodyConverter : Converter<ResponseBody, GetWeatherResponse> {
        override fun convert(value: ResponseBody): GetWeatherResponse {
            val string = value.string()

            val root = Gson().fromJson(string, JsonObject::class.java) ?: throw IllegalStateException("Response was not JSON object")

            return GetWeatherResponse().apply {
                // the code field is called "cod" - not a typo
                if (root.get("cod").asInt != 200) {
                    success = false
                    error = root.get("message").asString
                } else {
                    success = true
                    temperature = root.getAsJsonObject("main").get("temp").asDouble
                }
            }
        }

        companion object {
            val INSTANCE = WeatherResponseBodyConverter()
        }
    }
}