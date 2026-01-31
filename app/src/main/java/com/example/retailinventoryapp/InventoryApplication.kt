package com.example.retailinventoryapp

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class InventoryApplication : Application() {
    // Această clasă rămâne goală, dar prezența ei activează Hilt în tot proiectul.
}