package com.gv.wetherapp1.fragments

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.activityViewModels
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.android.material.tabs.TabLayoutMediator
import com.gv.wetherapp1.DialogManager
import com.gv.wetherapp1.MainViewModel
import com.gv.wetherapp1.R
import com.gv.wetherapp1.adapters.VpAdapter
import com.gv.wetherapp1.adapters.WeatherModel
import com.gv.wetherapp1.databinding.FragmentMainBinding
import com.squareup.picasso.Picasso
import org.json.JSONObject

const val API_KEY="f7c9b1aaf7324fc2b7c145511241011"
class MainFragment : Fragment() {
    private lateinit var flocationClient:FusedLocationProviderClient
    private val fList=listOf(
        HoursFragment.newInstance(),
        DaysFragment.newInstance()

    )
    private val tList=listOf(
        "HOURS",
        "DAYS"
        )
    private lateinit var pLauncher: ActivityResultLauncher<String>
    private lateinit var binding: FragmentMainBinding
    private val model: MainViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding =FragmentMainBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        checkPermission()
        init()
        updateCurrentCard()
    }
//Если пользователь все-таки включил, то приложение обновиься и продолжит работу
    override fun onResume() {
        super.onResume()
        checkLocation()
    }

    // прописываем логику переключения двух фрагментов в TabLayout
    private fun init()=with(binding){
        flocationClient=LocationServices.getFusedLocationProviderClient(requireContext())
        val adapter= VpAdapter(activity as FragmentActivity,fList)
        vp.adapter=adapter
        //Здесь и будет происходить переключение
        TabLayoutMediator(tabLayout, vp){
                                        // переход с определенного таба на другой с передачей нужного текста на позициях
            tab, pos->tab.text=tList[pos]
        }.attach()
        ibSync.setOnClickListener{
            tabLayout.selectTab(tabLayout.getTabAt(0))//при нажатии на кпопку обновления вызвращает на первый таб(погода по часам)
          checkLocation()


        }
        ibSearch.setOnClickListener{
            DialogManager.searhByNameDialog(requireContext(),object: DialogManager.Listener{
                override fun onClick(name: String?) {
                    //проверка на ноль
                    name?.let { it1 -> requestWeatherData(it1) }
                }

            })
        }
    }
        //эта функция отправляет нас в настройки, для включения gps

    private fun checkLocation(){
        if(isLocationEnabled()){
            getLocation()
        } else {
            DialogManager.locationSettingsDialog(requireContext(),object :DialogManager.Listener{
                override fun onClick(name:String?) {
                    startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
                }

            })
        }
    }
// Проверяет, включена ли gps если нет, то выводит диалоговое сообщение
    private fun isLocationEnabled():Boolean{
        val lm=activity?.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return lm.isProviderEnabled(LocationManager.GPS_PROVIDER)
    }

    //С помощью этой функции будем получать сведения о местоположения
    private fun getLocation(){
        val ct=CancellationTokenSource()
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {

            return
        }
        flocationClient
            .getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY,ct.token)
            .addOnCompleteListener{
                requestWeatherData("${it.result.latitude},${it.result.longitude}")
            }
    }

    //для того, чтобы следить за жизненным циклом активити\фрагментов и если они не созданы еще не передаваь информацию с сервера
    //передаем в вью
    private fun updateCurrentCard()=with(binding){
        model.liveDataCurrent.observe(viewLifecycleOwner){
            val maxMinTemp="${it.minTemp}°С/${it.maxTemp}°С"
            tvData.text=it.time
            tvCity.text=it.city
            tvCondition.text=it.condition
            tvCurrentTemp.text=it.currentTemp.ifEmpty { maxMinTemp }
            tvMaxMin.text=if(it.currentTemp.isEmpty()) "" else maxMinTemp
            Picasso.get().load("https:"+ it.imageUrl).into(imWether)

        }
    }

    // с помощью launcher создаем проверку разрешений от пользоваеля:
    private fun permissionListener(){
        pLauncher=registerForActivityResult(
            ActivityResultContracts.RequestPermission()){
         Toast.makeText(activity,"Permissiom is $it",Toast.LENGTH_LONG).show()
        }
    }

private fun checkPermission (){
    if(!isPermissionGranted(Manifest.permission.ACCESS_FINE_LOCATION)) {
        permissionListener()
        pLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
    }
}
    private fun requestWeatherData(city:String){
        val url="https://api.weatherapi.com/v1/forecast.json" +
        "?key= $API_KEY" +
                "&q=$city" +
                "&days=" +
                "3" +
                "&aqi=no&alerts=no"

        //Создание очереди запросов
        val queue= Volley.newRequestQueue(context)
        val request=StringRequest(
            Request.Method.GET,
            url,
            //сюда и придет наш json fail
            {
                result-> parseWeatherData(result)
            },
            //проверям на ошибки
            {
                error-> Log.d("MyLog","Error:$error")
            }
        )
        queue.add(request)
    }

    //Создаем функцию с помощью которой будем доставать полученные данные
    private fun parseWeatherData(result: String) {
        val mainObject = JSONObject(result)
        val list= parseDays(mainObject)

        parseCurrentData(mainObject,list[0])
    }

    private fun parseDays(mainObject: JSONObject):List<WeatherModel> {
        val list = ArrayList<WeatherModel>()
        //достаем массив в виде JSON object
        val daysArray = mainObject.getJSONObject("forecast")
            .getJSONArray("forecastday")
        //Достаем из массива каждый день
        val name = mainObject.getJSONObject("location").getString("name")
        for (i in 0 until daysArray.length()) {
            val day = daysArray[i] as JSONObject//УКАЗЫВАЕМ ЧТО ЭТО JSON ОБЪЕКТ
            val item = WeatherModel(
                name,
                day.getString("date"),
                day.getJSONObject("day").getJSONObject("condition").getString("text"),
                "",
                day.getJSONObject("day").getString("maxtemp_c").toFloat().toInt().toString(),
                day.getJSONObject("day").getString("mintemp_c").toFloat().toInt().toString(),
                day.getJSONObject("day").getJSONObject("condition")
                    .getString("icon"),
                day.getJSONArray("hour").toString()
            )
            list.add(item)
        }
        model.liveDataList.value=list
return list
    }
    private fun parseCurrentData(mainObject:JSONObject,weatherItem:WeatherModel){
        val item = WeatherModel(
            mainObject.getJSONObject("location").getString("name"),
            mainObject.getJSONObject("current").getString("last_updated"),
            mainObject.getJSONObject("current")
                .getJSONObject("condition").getString("text"),
            mainObject.getJSONObject("current").getString("temp_c").toFloat().toInt().toString(),
            weatherItem.maxTemp,
            weatherItem.minTemp,
            mainObject.getJSONObject("current").getJSONObject("condition").getString("icon"),
            weatherItem.hours

        )
        model.liveDataCurrent.value=item


    }


    companion object {

        @JvmStatic
        fun newInstance() = MainFragment()

    }
}