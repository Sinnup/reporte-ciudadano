package com.espert.reeporteciudadano.di

import com.espert.reeporteciudadano.data.datasource.remote.GeocodingApi
import com.espert.reeporteciudadano.data.repository.GeocodingRepositoryImpl
import com.espert.reeporteciudadano.data.repository.ReportRepositoryImpl
import com.espert.reeporteciudadano.database.AppDatabase
import com.espert.reeporteciudadano.domain.repository.GeocodingRepository
import com.espert.reeporteciudadano.domain.repository.ReportRepository
import com.espert.reeporteciudadano.domain.usecase.*
import com.espert.reeporteciudadano.feature.camera.CameraViewModel
import com.espert.reeporteciudadano.feature.myreports.MyReportsViewModel
import com.espert.reeporteciudadano.feature.reportdetail.ReportDetailViewModel
import com.espert.reeporteciudadano.feature.reportform.ReportFormViewModel
import com.espert.reeporteciudadano.feature.reportsmap.ReportsMapViewModel
import com.espert.reeporteciudadano.navigation.AppViewModel
import com.espert.reeporteciudadano.platform.DatabaseDriverFactory
import io.ktor.client.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

fun appModule(driverFactory: DatabaseDriverFactory) = module {
    single { driverFactory.createDriver() }
    single { AppDatabase(get()) }
    single {
        HttpClient {
            install(ContentNegotiation) { json(Json { ignoreUnknownKeys = true }) }
        }
    }
    single { GeocodingApi(get()) }
    single<ReportRepository> { ReportRepositoryImpl(get()) }
    single<GeocodingRepository> { GeocodingRepositoryImpl(get()) }
    factory { SaveReportUseCase(get()) }
    factory { GetAllReportsUseCase(get()) }
    factory { GetReportByIdUseCase(get()) }
    factory { ReverseGeocodeUseCase(get()) }
    viewModelOf(::AppViewModel)
    viewModelOf(::CameraViewModel)
    viewModel { ReportFormViewModel(get(), get()) }
    viewModelOf(::MyReportsViewModel)
    viewModel { (reportId: String) -> ReportDetailViewModel(reportId, get()) }
    viewModelOf(::ReportsMapViewModel)
}
