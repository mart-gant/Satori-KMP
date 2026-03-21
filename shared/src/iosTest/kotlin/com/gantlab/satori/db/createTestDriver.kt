package com.gantlab.satori.db

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.native.NativeSqliteDriver

actual fun createTestDriver(): SqlDriver {
    return NativeSqliteDriver(SatoriDatabase.Schema, "SatoriTest.db")
}
