package things.dev.di

import android.os.Build
import com.google.gson.Gson
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import mqtt.broker.Broker
import things.dev.features.wifi.framework.AndroidRAndBelowNetworkCallbackProxy
import things.dev.features.wifi.framework.AndroidSAndAboveNetworkCallbackProxy
import things.dev.features.wifi.framework.NetworkCallbackProxy
import things.dev.features.wifi.framework.NetworkCallbackProxyFactory
import javax.inject.Named

@Module
@InstallIn(SingletonComponent::class)
class ApplicationModule {
    @Provides
    fun providesGson(): Gson {
        return Gson()
    }
    @Provides
    fun providesBroker(): Broker {
        return Broker()
    }
    @Provides
    @Named("NetworkCallbackFlags")
    fun providesNetworkCallbackFlags(): Int {
        return 0
    }
}