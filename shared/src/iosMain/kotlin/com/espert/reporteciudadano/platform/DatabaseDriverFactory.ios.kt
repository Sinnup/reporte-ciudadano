package com.espert.reporteciudadano.platform

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.native.NativeSqliteDriver
import com.espert.reporteciudadano.database.AppDatabase

actual class DatabaseDriverFactory {
    actual fun createDriver(): SqlDriver = NativeSqliteDriver(AppDatabase.Schema, "reports.db")
}
