package com.bitcode.locationbasedservices

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.location.*
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.location.LocationManagerCompat
import java.util.*

class MainActivity : AppCompatActivity() {

    lateinit var locationManager: LocationManager

    var brLocation = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            mt("Got the location")
            /*for(key in intent!!.extras!!.keySet() ) {
                mt(key)
            }*/
            var location =
                intent!!.getParcelableExtra<Location>(LocationManager.KEY_STATUS_CHANGED) as Location
            mt("Location: ${location?.latitude} , ${location?.longitude}")
        }
    }

    var brProximity = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            mt("proximity intent fired..")
            var isEntering =
                intent?.getBooleanExtra(LocationManager.KEY_PROXIMITY_ENTERING, false)
            mt("proximity intent fired.. ${isEntering}")
        }
    }

    @SuppressLint("MissingPermission")
    @RequiresApi(Build.VERSION_CODES.P)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager

        //mt("loc enabled? ${locationManager.isLocationEnabled}")

        for (provider in locationManager.allProviders) {
            mt("Provider : $provider")
            var locationProvider = locationManager.getProvider(provider)
            mt("Cost? ${locationProvider!!.hasMonetaryCost()}")
            mt("Alt? ${locationProvider!!.supportsAltitude()}")
            mt("Sat? ${locationProvider!!.requiresSatellite()}")
            mt("Net? ${locationProvider!!.requiresNetwork()}")
            mt("Power? ${locationProvider!!.powerRequirement}")
            mt("Accuracy? ${locationProvider!!.accuracy}")
            mt("Cell Net? ${locationProvider!!.requiresCell()}")

            var location = locationManager.getLastKnownLocation(provider)
            if (location != null) {
                mt("location: ${location.latitude} ,  ${location.longitude}")
            }
            mt("-------------------------------------")
        }

        var criteria = Criteria()
        criteria.accuracy = Criteria.ACCURACY_FINE
        criteria.powerRequirement = Criteria.POWER_LOW
        criteria.isAltitudeRequired = true
        criteria.isCostAllowed = true

        registerReceiver(
            brLocation,
            IntentFilter("in.bitcode.pune.LOCATION")
        )

        var bestProvider = locationManager.getBestProvider(criteria, true)
        mt("Best Provider: ${bestProvider}")

        var intent = Intent("in.bitcode.pune.LOCATION")
        var pendingIntent = PendingIntent.getBroadcast(
            this,
            1,
            intent,
            0
        )
        //locationManager.requestSingleUpdate(bestProvider!!, pendingIntent)

        var locationListener = MyLocationListener()

        if (bestProvider != null) {
            locationManager.requestLocationUpdates(
                bestProvider,
                2000,
                100f,
                locationListener
            )
        }
        //locationManager.removeUpdates(locationListener)

        registerReceiver(
            brProximity,
            IntentFilter("in.bitcode.OFFICE")
        )

        locationManager.addProximityAlert(
            18.56206, 73.916745,
            1000F,
            -1,
            PendingIntent.getBroadcast(
                this,
                1,
                Intent("in.bitcode.OFFICE"),
                0
            )
        )

        var geocoder = Geocoder(this, Locale.getDefault())
        //var addresses = geocoder.getFromLocation(18.5091, 73.8327, 10)
        var addresses = geocoder.getFromLocationName("BitCode pune", 10)
        for(adr in addresses) {
            mt("${adr.postalCode} ${adr.getAddressLine(0)} ${adr.phone}")
        }

    }

    inner class MyLocationListener : LocationListener {

        override fun onProviderEnabled(provider: String) {
        }

        override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {
        }

        override fun onLocationChanged(location: Location) {
            mt("Location is ${location.latitude} , ${location.longitude}")
        }

    }

    private fun mt(text: String) {
        Log.e("tag", text)
    }
}