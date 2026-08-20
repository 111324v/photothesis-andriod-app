package com.photosynthesis.app

import android.app.Application

/**
 * 全局 Application 类
 * 用于初始化数据库、全局单例等
 */
class PhotosynthesisApp : Application() {

    // 懒加载数据库实例，整个App生命周期只创建一次
    val database: AppDatabase by lazy {
        AppDatabase.getInstance(this)
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
    }

    companion object {
        lateinit var instance: PhotosynthesisApp
            private set
    }
}
