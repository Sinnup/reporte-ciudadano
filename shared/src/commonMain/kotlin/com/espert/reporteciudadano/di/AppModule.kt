package com.espert.reporteciudadano.di

import com.espert.reporteciudadano.data.datasource.remote.GeocodingApi
import com.espert.reporteciudadano.data.repository.GeocodingRepositoryImpl
import com.espert.reporteciudadano.data.repository.ReportRepositoryImpl
import com.espert.reporteciudadano.database.AppDatabase
import com.espert.reporteciudadano.domain.repository.GeocodingRepository
import com.espert.reporteciudadano.domain.repository.ReportRepository
import com.espert.reporteciudadano.domain.repository.SyncStateRepository
import com.espert.reporteciudadano.domain.usecase.*
import com.espert.reporteciudadano.feature.camera.CameraViewModel
import com.espert.reporteciudadano.feature.myreports.MyReportsViewModel
import com.espert.reporteciudadano.feature.reportdetail.ReportDetailViewModel
import com.espert.reporteciudadano.feature.reportform.ReportFormViewModel
import com.espert.reporteciudadano.feature.reportsmap.ReportsMapViewModel
import com.espert.reporteciudadano.navigation.AppViewModel
import com.espert.reporteciudadano.platform.DatabaseDriverFactory
import io.ktor.client.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.core.parameter.parametersOf
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
    viewModel { MyReportsViewModel(get(), getOrNull()) }
    viewModel { (reportId: String) -> ReportDetailViewModel(reportId, get(), get()) }
    viewModelOf(::ReportsMapViewModel)
}
