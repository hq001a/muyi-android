package com.czl.lib_base.base

import android.app.Activity
import android.app.Application
import android.os.Bundle
import androidx.core.content.ContextCompat
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import com.alibaba.android.arouter.launcher.ARouter
import com.blankj.utilcode.util.LogUtils
import com.blankj.utilcode.util.Utils
import com.bumptech.glide.Glide
import com.czl.lib_base.BuildConfig
import com.czl.lib_base.R
import com.czl.lib_base.di.allModule
import com.czl.lib_base.util.ToastHelper
import com.scwang.smart.refresh.footer.ClassicsFooter
import com.scwang.smart.refresh.header.MaterialHeader
import com.scwang.smart.refresh.layout.SmartRefreshLayout
import com.tencent.mmkv.MMKV
import es.dmoral.toasty.Toasty
import io.reactivex.plugins.RxJavaPlugins
import me.jessyan.autosize.AutoSizeConfig
import me.yokeyword.fragmentation.Fragmentation
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level


/**
 * @author Alwyn
 * @Date 2020/7/20
 * @Description
 */
open class MyApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) {
            ARouter.openLog()
            ARouter.openDebug()
            //        DoraemonKit.apply {
//            setDebug(BuildConfig.DEBUG)
//            setAwaysShowMainIcon(BuildConfig.DEBUG)
//            install(this@MyApplication)
//        }
        }
        ARouter.init(this)
        setApplication(this)
        MMKV.initialize(this)
        // 初始化Fragmentation
        Fragmentation.builder()
            .stackViewMode(Fragmentation.NONE)
            .debug(BuildConfig.DEBUG)
            .install()
        // 屏幕适配
        AutoSizeConfig.getInstance().setCustomFragment(true).setBaseOnWidth(false)
            .setExcludeFontScale(true).designHeightInDp = 720
        //是否开启日志打印
        LogUtils.getConfig().setLogSwitch(BuildConfig.DEBUG).setConsoleSwitch(BuildConfig.DEBUG)
        startKoin {
            androidLogger(Level.ERROR)
            androidContext(this@MyApplication)
            modules(allModule)
        }
        RxJavaPlugins.setErrorHandler {
            ToastHelper.showErrorToast("系统错误")
            it.printStackTrace()
        }
        // 设置吐司不以队列循环展示
        Toasty.Config.getInstance().allowQueue(false).apply()
    }

    companion object {
        init {
            ClassicsFooter.REFRESH_FOOTER_FINISH = "加载完成"
            ClassicsFooter.REFRESH_FOOTER_LOADING = "加载中"
            ClassicsFooter.REFRESH_FOOTER_FAILED = "加载失败"
            ClassicsFooter.REFRESH_FOOTER_NOTHING = "没有更多数据了"

            SmartRefreshLayout.setDefaultRefreshInitializer { _, layout ->
                layout.apply {
                    setEnableOverScrollDrag(true)
                    setEnableScrollContentWhenLoaded(true)
                    setEnableAutoLoadMore(true)
                    setEnableOverScrollBounce(true)
                    setFooterHeight(60f)
                    setEnableFooterTranslationContent(true)
                }
            }
            SmartRefreshLayout.setDefaultRefreshHeaderCreator { context, layout ->
                layout.apply {
                    setPrimaryColorsId(R.color.mColorPrimary, R.color.white)
                }
                MaterialHeader(context).setColorSchemeColors(
                    ContextCompat.getColor(
                        context,
                        R.color.mColorPrimary
                    )
                )
            }
            SmartRefreshLayout.setDefaultRefreshFooterCreator { context, _ ->
                ClassicsFooter(context)
                    .setFinishDuration(0)
                    .setProgressDrawable(CircularProgressDrawable(context).apply {
                        setColorSchemeColors(ContextCompat.getColor(context, R.color.mColorAccent))
                    })
            }
        }
    }

    private fun setApplication(application: Application) {
        //初始化工具类
        Utils.init(application)
        //注册监听每个activity的生命周期,便于堆栈式管理
        application.registerActivityLifecycleCallbacks(object :
            ActivityLifecycleCallbacks {
            override fun onActivityCreated(
                activity: Activity,
                savedInstanceState: Bundle?
            ) {
                AppManager.instance.addActivity(activity)
            }

            override fun onActivityStarted(activity: Activity) {}
            override fun onActivityResumed(activity: Activity) {}
            override fun onActivityPaused(activity: Activity) {}
            override fun onActivityStopped(activity: Activity) {}
            override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}
            override fun onActivityDestroyed(activity: Activity) {
                AppManager.instance.removeActivity(activity)
            }
        })
    }

    override fun onTerminate() {
        super.onTerminate()
        ARouter.getInstance().destroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        Glide.get(this).clearMemory()
    }

    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)
        if (level == TRIM_MEMORY_UI_HIDDEN) {
            Glide.get(this).clearMemory()
        }
        Glide.get(this).trimMemory(level)
    }

}