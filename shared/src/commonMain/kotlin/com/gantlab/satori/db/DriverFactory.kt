package com.gantlab.satori.db

import app.cash.sqldelight.db.SqlDriver

expect class DriverFactory {
    fun createDriver(): SqlDriver
}

fun createDatabase(driverFactory: DriverFactory): SatoriDatabase {
    val driver = driverFactory.createDriver()
    return SatoriDatabase(driver)
}
