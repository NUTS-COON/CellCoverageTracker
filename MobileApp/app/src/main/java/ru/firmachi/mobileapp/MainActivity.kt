package ru.firmachi.mobileapp

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.os.Bundle
import android.telephony.SubscriptionManager
import android.telephony.TelephonyManager
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProviders
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.LocationSettingsStatusCodes
import com.here.android.mpa.common.GeoCoordinate
import com.here.android.mpa.common.OnEngineInitListener
import com.here.android.mpa.mapping.Map
import com.here.android.mpa.mapping.SupportMapFragment
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.network_info_block.view.*
import ru.firmachi.mobileapp.appServices.TrackingService
import ru.firmachi.mobileapp.models.CellData
import ru.firmachi.mobileapp.services.NetworkService
import java.util.*
import kotlin.collections.ArrayList

class MainActivity : AppCompatActivity() {

    private val REQUEST_PERMISSIONS_CODE = 1
    private val REQUEST_GPS_CODE = 11
    private val REQUIRED_SDK_PERMISSIONS = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.READ_PHONE_STATE)


    lateinit var viewModel: MainViewModel
    lateinit var mapFragment: SupportMapFragment
    private var map: Map? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initViewModel()
        initialize()
        checkPermissions()
    }


    private fun run(){
        startService(Intent(baseContext, TrackingService::class.java))
        viewModel.ready(getNetworkService())
    }


    private fun initViewModel(){
        viewModel = ViewModelProviders.of(this).get(MainViewModel::class.java)
        viewModel.cellDataLiveData.observe(this, androidx.lifecycle.Observer{

            if(it.isNotEmpty()){
                val data = it.first()
                map!!.setCenter(GeoCoordinate(data.latitude, data.longitude), Map.Animation.NONE)
                showNetworkInfo(it)
            }
        })
        viewModel.scoreLiveData.observe(this, androidx.lifecycle.Observer {
            showScoreInfo(it)
        })
    }


    private fun initialize() {
        mapFragment = supportFragmentManager.findFragmentById(R.id.map_fragment) as SupportMapFragment
        mapFragment.init { error ->
            if (error == OnEngineInitListener.Error.NONE) {
                //mapFragment.mapGesture.addOnGestureListener(gestureListener)
                map = mapFragment.map
                map!!.setCenter(GeoCoordinate(54.193422, 37.616266), Map.Animation.NONE)
                map!!.zoomLevel = 11.0
            } else {
                Toast.makeText(applicationContext, "Карты Here В С Ё. Помянем", Toast.LENGTH_SHORT).show()
            }
        }
    }



    private fun showNetworkInfo(cellData: List<CellData>){
        ll_network_info_block.removeAllViews()
        cellData.forEach {
            ll_network_info_block.addView(getNetworkInfoBlock(it))
        }
    }


    private fun showScoreInfo(score: Int){
        tv_score_info.text = "Вы отправили $score точек"
        tv_score_level.text = getLevelName(score)
    }


    private fun getNetworkInfoBlock(cellData: CellData): LinearLayout{
        val v = LayoutInflater.from(this).inflate(R.layout.network_info_block, null) as LinearLayout
        v.iv_signal_icon.setImageResource(getSignalIconResource(cellData.level))

        val operatorLogoResourceId = getOperatorLogoResource(cellData.operatorName)
        if(operatorLogoResourceId != -1){
            v.iv_operator_logo.setImageResource(operatorLogoResourceId)
            v.iv_operator_logo.visibility = View.VISIBLE
            v.tv_operator_name.visibility = View.GONE
        }else{
            v.tv_operator_name.text = cellData.operatorName
            v.iv_operator_logo.visibility = View.GONE
            v.tv_operator_name.visibility = View.VISIBLE
        }

        v.tv_operator_name.text = cellData.operatorName
        v.tv_signal_type.text = cellData.cellType

        v.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT,
            1.0f)

        return v
    }


    private fun getOperatorLogoResource(operatorName: String): Int{
        return when(operatorName){
            "Beeline" -> R.drawable.logo_beeline
            "Tele2" -> R.drawable.logo_tele2
            "MTS RUS" -> R.drawable.logo_mts
            "MTS" -> R.drawable.logo_mts
            else -> -1
        }
    }


    private fun getSignalIconResource(signalLevel: Int): Int{
        return when (signalLevel){
            0 -> R.drawable.signal_red_0
            1 -> R.drawable.signal_red_1
            2 -> R.drawable.signal_red_3
            3 -> R.drawable.signal_blue_4
            4 -> R.drawable.signal_blue_5
            else -> R.drawable.signal_blue_5
        }
    }


    private fun getLevelName(score: Int): String{
        return "Колумб"
    }


    private fun getNetworkService(): NetworkService {
        val telephonyManager = getSystemService(Context.TELEPHONY_SERVICE) as (TelephonyManager)
        val subscriptionManager = getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE) as SubscriptionManager
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        return NetworkService(applicationContext, telephonyManager, fusedLocationClient, subscriptionManager)
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
                it.getResult(ApiException::class.java)
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
                    else -> {
                        Toast.makeText(baseContext,
                            "Для работы приложения необходимо включить геолокацию",
                            Toast.LENGTH_SHORT).show()
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
