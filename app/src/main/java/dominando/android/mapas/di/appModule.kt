package dominando.android.mapas.di

import dominando.android.mapas.MapViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    viewModel<MapViewModel>()
}