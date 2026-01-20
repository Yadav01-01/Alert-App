package com.alert.app.di

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.alert.app.BuildConfig
import com.alert.app.base.BaseApplication
import com.alert.app.errormessage.MessageClass
import com.alert.app.repository.MainRepository
import com.alert.app.repository.MainRepositoryImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Singleton
    @Provides
    fun alertApiInterFace(retrofit: Retrofit.Builder, okHttpClient: OkHttpClient): ApiInterfaceClass {
        return  retrofit
            .client(okHttpClient)
            .build()
            .create(ApiInterfaceClass::class.java)
    }

    @Provides
    @Singleton
    fun alertRepository(api: ApiInterfaceClass): MainRepository {
        return MainRepositoryImpl(api)
    }


    @Singleton
    @Provides
    fun alertAuthenticator(@ApplicationContext context: Context): AuthInterceptor {
        return AuthInterceptor(context)
    }

    @Singleton
    @Provides
    fun alertOkHttpClient(authInterceptor: AuthInterceptor): OkHttpClient {

        val loggingInterceptor = HttpLoggingInterceptor { message -> Log.d("RetrofitLog", message) }
        if (BuildConfig.DEBUG) {
            loggingInterceptor.level = HttpLoggingInterceptor.Level.BODY
        }else{
            loggingInterceptor.level = HttpLoggingInterceptor.Level.NONE
        }


        return OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(loggingInterceptor)
            .connectTimeout(120, TimeUnit.MINUTES)
            .writeTimeout(120, TimeUnit.MINUTES)
            .readTimeout(120, TimeUnit.MINUTES)
            .build()
    }

    private fun handleLogout(context:Context) {
        // Use application context to avoid lifecycle issues
        val appContext = context.applicationContext
        // Run on the main thread for UI-related tasks
        Handler(Looper.getMainLooper()).post {
            BaseApplication.alertError(appContext, MessageClass.sessionError,true)
        }
    }

    @Singleton
    @Provides
    fun alertRetrofitBuilder(): Retrofit.Builder = Retrofit.Builder()
        .baseUrl(BuildConfig.BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())

}

