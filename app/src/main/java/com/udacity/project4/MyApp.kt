package com.udacity.project4

import android.app.Application
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.local.LocalDB
import com.udacity.project4.locationreminders.data.local.RemindersLocalRepository
import com.udacity.project4.locationreminders.reminderslist.RemindersListViewModel
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.GlobalContext.startKoin
import org.koin.dsl.module


class MyApp : Application() {

    override fun onCreate() {
        super.onCreate()

        /**
         * use Koin Library as a service locator
         */
        val myModule = module {
            //Declare singleton definitions to be later injected using by inject()
            single { LocalDB.createRemindersDao(get()) }
            single<ReminderDataSource> { RemindersLocalRepository(get()) }

            //Declare a ViewModel - be later inject into Fragment with dedicated injector using by viewModel()
            viewModel { RemindersListViewModel(get(), get()) }
            viewModel { SaveReminderViewModel(get(), get()) }
        }

        startKoin {
            androidContext(this@MyApp)
            modules(listOf(myModule))
        }
    }

}