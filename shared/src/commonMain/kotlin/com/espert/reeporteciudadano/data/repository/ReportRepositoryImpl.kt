package com.espert.reeporteciudadano.data.repository

import com.espert.reeporteciudadano.database.AppDatabase
import com.espert.reeporteciudadano.domain.model.*
import com.espert.reeporteciudadano.domain.repository.ReportRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ReportRepositoryImpl(private val db: AppDatabase) : ReportRepository {
    override suspend fun save(report: CitizenReport): Result<Unit> = withContext(Dispatchers.Default) {
        runCatching {
            db.appDatabaseQueries.insertReport(
                id = report.id,
                title = report.title,
                description = report.description,
                latitude = report.location.latitude,
                longitude = report.location.longitude,
                address = report.address,
                status = report.status.name,
                created_at = report.createdAt
            )
            report.photos.forEach { photo ->
                db.appDatabaseQueries.insertPhoto(
                    id = photo.id,
                    report_id = report.id,
                    local_path = photo.localPath,
                    exif_latitude = photo.exifLocation?.latitude,
                    exif_longitude = photo.exifLocation?.longitude
                )
            }
        }
    }

    override suspend fun getAll(): Result<List<CitizenReport>> = withContext(Dispatchers.Default) {
        runCatching {
            db.appDatabaseQueries.getAllReports().executeAsList().map { entity ->
                val photos = db.appDatabaseQueries.getPhotosForReport(entity.id).executeAsList()
                    .map { p ->
                        ReportPhoto(
                            p.id,
                            p.local_path,
                            if (p.exif_latitude != null && p.exif_longitude != null)
                                GeoLocation(p.exif_latitude, p.exif_longitude)
                            else null
                        )
                    }
                CitizenReport(
                    id = entity.id,
                    title = entity.title,
                    description = entity.description,
                    photos = photos,
                    location = GeoLocation(entity.latitude, entity.longitude),
                    address = entity.address,
                    status = ReportStatus.valueOf(entity.status),
                    createdAt = entity.created_at
                )
            }
        }
    }

    override suspend fun getById(id: String): Result<CitizenReport> = withContext(Dispatchers.Default) {
        runCatching {
            val entity = db.appDatabaseQueries.getReportById(id).executeAsOne()
            val photos = db.appDatabaseQueries.getPhotosForReport(id).executeAsList()
                .map { p ->
                    ReportPhoto(
                        p.id,
                        p.local_path,
                        if (p.exif_latitude != null && p.exif_longitude != null)
                            GeoLocation(p.exif_latitude, p.exif_longitude)
                        else null
                    )
                }
            CitizenReport(
                id = entity.id,
                title = entity.title,
                description = entity.description,
                photos = photos,
                location = GeoLocation(entity.latitude, entity.longitude),
                address = entity.address,
                status = ReportStatus.valueOf(entity.status),
                createdAt = entity.created_at
            )
        }
    }
}
