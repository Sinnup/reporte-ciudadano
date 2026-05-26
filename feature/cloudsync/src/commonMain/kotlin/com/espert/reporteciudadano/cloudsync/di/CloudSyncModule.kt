package com.espert.reporteciudadano.cloudsync.di

import com.espert.reporteciudadano.cloudsync.data.datasource.remote.DynamoDbDataSource
import com.espert.reporteciudadano.cloudsync.data.datasource.remote.LocalFileReader
import com.espert.reporteciudadano.cloudsync.data.datasource.remote.S3DataSource
import com.espert.reporteciudadano.cloudsync.data.repository.CloudSyncRepositoryImpl
import com.espert.reporteciudadano.cloudsync.domain.repository.CloudSyncRepository
import com.espert.reporteciudadano.cloudsync.domain.usecase.*
import com.espert.reporteciudadano.cloudsync.platform.loadAwsCredentials
import com.espert.reporteciudadano.domain.repository.SyncStateRepository
import org.koin.dsl.module

val cloudSyncModule = module {
    // AWS credentials — loaded once at startup from the platform-specific properties file
    single { loadAwsCredentials() }

    // Data sources
    single { DynamoDbDataSource(get(), get()) }
    single { LocalFileReader() }
    single { S3DataSource(get(), get(), get()) }

    // Repository — binds as both CloudSyncRepository and SyncStateRepository
    single<CloudSyncRepositoryImpl> { CloudSyncRepositoryImpl(get(), get(), get()) }
    single<CloudSyncRepository> { get<CloudSyncRepositoryImpl>() }
    single<SyncStateRepository> { get<CloudSyncRepositoryImpl>() }

    // Use cases
    factory { GetPendingSyncsUseCase(get()) }
    factory { RecordSyncFailureUseCase(get()) }
    factory { SyncPhotoUseCase(get()) }
    factory { SyncReportUseCase(get(), get()) }
    factory { RetryFailedSyncsUseCase(get(), get()) }
}
