package com.gantlab.satori.db

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver

actual fun createTestDriver(): SqlDriver {
    val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
    SatoriDatabase.Schema.create(driver)
    return driver
}
