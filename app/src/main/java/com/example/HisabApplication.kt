package com.example

import android.app.Application
import com.example.data.HisabRepository
import com.example.data.local.HisabDatabase

class HisabApplication : Application() {
    val database: HisabDatabase by lazy { HisabDatabase.getInstance(this) }
    val repository: HisabRepository by lazy { HisabRepository(this, database.hisabDao()) }
}
