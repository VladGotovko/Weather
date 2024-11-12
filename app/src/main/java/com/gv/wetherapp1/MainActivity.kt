package com.gv.wetherapp1

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.gv.wetherapp1.fragments.MainFragment

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        supportFragmentManager
            .beginTransaction() // начало взаимодействия с фрагментами ( удаление, замена, добавление
            .replace(R.id.placeHolder, MainFragment.newInstance()) // Заменяем контейнер (например, FrameLayout) в активности на ваш фрагмент
            .commit() // принимаем изменения
    }
}