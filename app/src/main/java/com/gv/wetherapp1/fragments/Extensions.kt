package com.gv.wetherapp1.fragments

import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.PackageManagerCompat
import androidx.fragment.app.Fragment
//С помошью extension функции проверяем разрешения от пользователя
fun Fragment.isPermissionGranted(p:String):Boolean {
    return ContextCompat.checkSelfPermission(
        activity as AppCompatActivity,p) == PackageManager.PERMISSION_GRANTED

}