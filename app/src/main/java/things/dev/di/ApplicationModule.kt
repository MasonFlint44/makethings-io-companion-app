package things.dev.di

import com.google.gson.Gson
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import mqtt.broker.Broker

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
}