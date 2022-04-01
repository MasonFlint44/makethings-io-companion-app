package things.dev.di

import android.app.Activity
import androidx.activity.ComponentActivity
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import things.dev.features.permissions.framework.AndroidPermissionService
import things.dev.features.permissions.framework.PermissionService

@Module
@InstallIn(ActivityComponent::class)
abstract class ActivityModule {
//    @Binds
//    abstract fun bindMainPageModel(model: MainPageModelImpl): MainPageModel

    companion object {
        @Provides
        fun providesLifecycleOwner(activity: Activity): LifecycleOwner {
            return activity as LifecycleOwner
        }
        @Provides
        fun providesLifecycleScope(activity: Activity): LifecycleCoroutineScope {
            return (activity as ComponentActivity).lifecycleScope
        }
        @Provides
        fun providesPermissionService(permissionService: AndroidPermissionService): PermissionService {
            return permissionService
        }
    }
}