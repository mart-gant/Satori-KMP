package com.gantlab.satori.db

import com.gantlab.satori.network.SatoriApiService
import com.gantlab.satori.settings.SettingsManager

// Redundant implementation. Use SatoriRepository from commonMain.
class AndroidSatoriRepository(
    database: SatoriDatabase,
    api: SatoriApiService? = null,
    settings: SettingsManager? = null,
) : SatoriRepository(database, api, settings)
