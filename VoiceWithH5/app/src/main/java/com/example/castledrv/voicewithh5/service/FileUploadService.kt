package com.example.castledrv.voicewithh5.service

import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import rx.Observable

/**
 * Created by CastleDrv on 2017/7/16.
 */

interface FileUploadService {
    @Multipart
    @POST("upload")
    fun uploadFile(@Part("file\"; filename=\"avatar.amr\"") file: RequestBody): Observable<ResponseBody>
}

