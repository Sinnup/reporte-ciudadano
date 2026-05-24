package com.espert.reporteciudadano.platform

import app.cash.sqldelight.db.SqlDriver

actual class DatabaseDriverFactory {
    actual fun createDriver(): SqlDriver = throw UnsupportedOperationException("Web DB not configured")
}
