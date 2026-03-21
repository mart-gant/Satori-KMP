package com.gantlab.satori.db

import app.cash.sqldelight.db.SqlDriver

actual fun createTestDriver(): SqlDriver {
    error("JS test driver is not implemented. For JS/Wasm, SQLDelight usually requires a WebWorker or sql.js setup.")
}
