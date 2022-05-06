package net.davegoddin.trigbag.service

import net.davegoddin.trigbag.model.Postcode
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface PostcodeService {
    //HTTP GET request to uri defined in service builder using postcode string parameter
    @GET("{postcode}")
    fun getPostcode(@Path("postcode") postcode: String) : Call<Postcode>
}