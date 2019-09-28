package ru.firmachi.mobileapp

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import ru.firmachi.mobileapp.services.TrackingService
import java.util.*
import kotlin.collections.ArrayList

class MainActivity : AppCompatActivity() {

    private val REQUEST_PERMISSIONS_CODE = 1
    private val REQUEST_GPS_CODE = 11
    private val REQUIRED_SDK_PERMISSIONS =
        arrayOf(Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.READ_PHONE_STATE)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        checkPermissions()
    }

    private fun run(){
        startService(Intent(applicationContext, TrackingService::class.java))
    }


    private fun checkGpsTurnOn(){
        val locationRequest = LocationRequest.create()
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        locationRequest.interval = 5 * 1000
        locationRequest.fastestInterval = 2 * 1000
        val builder = LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest)
        builder.setAlwaysShow(true)

        LocationServices.getSettingsClient(this).checkLocationSettings(builder.build()).addOnCompleteListener {
            try {
                val ignore = it.getResult(ApiException::class.java)
                run()
            } catch (exception: ApiException) {
                when (exception.statusCode) {
                    LocationSettingsStatusCodes.RESOLUTION_REQUIRED ->{
                        try {
                            val resolvable = exception as ResolvableApiException
                            resolvable.startResolutionForResult(this, REQUEST_GPS_CODE)
                        }
                        catch (e: IntentSender.SendIntentException) { }
                        catch (e: ClassCastException) { }
                    }
                }
            }
        }
    }

    private fun checkPermissions() {
        val missingPermissions = ArrayList<String>()
        for (permission in REQUIRED_SDK_PERMISSIONS) {
            val result = ContextCompat.checkSelfPermission(this, permission)
            if (result != PackageManager.PERMISSION_GRANTED) {
                missingPermissions.add(permission)
            }
        }
        if (missingPermissions.isNotEmpty()) {
            val permissions = missingPermissions
                .toTypedArray()
            ActivityCompat.requestPermissions(this, permissions, REQUEST_PERMISSIONS_CODE)
        } else {
            val grantResults = IntArray(REQUIRED_SDK_PERMISSIONS.size)
            Arrays.fill(grantResults, PackageManager.PERMISSION_GRANTED)
            onRequestPermissionsResult(
                REQUEST_PERMISSIONS_CODE, REQUIRED_SDK_PERMISSIONS,
                grantResults
            )
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode){
            REQUEST_GPS_CODE -> {
                when (resultCode){
                    Activity.RESULT_OK -> {
                        run()
                    }
                }
            }
        }
    }


    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            REQUEST_PERMISSIONS_CODE -> {
                for (index in permissions.indices.reversed()) {
                    if (grantResults[index] != PackageManager.PERMISSION_GRANTED) {
                        Toast.makeText(baseContext,
                            "Для работы приложения вам нужно предоставить необходимые разрешения",
                            Toast.LENGTH_SHORT).show()
                        return
                    }
                }
                checkGpsTurnOn()
            }
        }
    }
}
