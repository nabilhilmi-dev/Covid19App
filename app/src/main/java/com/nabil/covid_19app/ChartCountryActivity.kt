package com.nabil.covid_19app

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.bumptech.glide.Glide
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.nabil.covid_19app.Model.InfoNegara
import com.nabil.covid_19app.Model.Negara
import com.nabil.covid_19app.Network.InfoService
import kotlinx.android.synthetic.main.activity_chart_country.*
import okhttp3.OkHttpClient
import retrofit2.*
import retrofit2.converter.gson.GsonConverterFactory
import java.text.DecimalFormat
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayList

class ChartCountryActivity : AppCompatActivity() {
    companion object{
        const val EXTRA_COUNTRY = "EXTRA_COUNTRY"
        lateinit var simpanDataNegara: String
        lateinit var simpanDataFlag: String

    }

    private val sharedPrefFile = "kotlinsharedpreference"
    private lateinit var sharedPrefrences: SharedPreferences
    private var dayCases = ArrayList<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chart_country)

        sharedPrefrences = this.getSharedPreferences(sharedPrefFile, Context.MODE_PRIVATE)


        //format angka yang ditampilkan
        val formatter: NumberFormat = DecimalFormat("#,###")
        val editor: SharedPreferences.Editor = sharedPrefrences.edit()


        val data = intent.getParcelableExtra<Negara>(EXTRA_COUNTRY)
        data?.let {
            txt_name_country.text = data.Country
            latest_update.text = data.Date
            hasil_total_deaths_currently.text = formatter.format(data.TotalDeaths?.toDouble())
            hasil_new_deaths_currently.text = formatter.format(data.NewDeaths?.toDouble())
            hasil_new_confirmed_currently.text = formatter.format(data.NewConfirmed?.toDouble())
            hasil_total_confirmed_currently.text = formatter.format(data.TotalConfirmed?.toDouble())
            hasil_total_recovered_currently.text = formatter.format(data.TotalRecovered?.toDouble())
            hasil_new_recovered_currently.text = formatter.format(data.NewRecovered?.toDouble())

            editor.putString(data.Country, data.Country)
            editor.apply()
            editor.commit()

            val simpanNegara = sharedPrefrences.getString(data.Country, data.Country)
            val simpanFlag = sharedPrefrences.getString(data.CountryCode, data.CountryCode)
            simpanDataNegara = simpanNegara.toString()
            simpanDataFlag = simpanFlag.toString() + "/flat/64.png"

            if (simpanFlag != null){
                Glide.with(this).load("https://www.countryflags.io/$simpanDataFlag")
                    .into(img_flag_country)

            }else{
                Toast.makeText(this,  "Kagak Ada Gambar", Toast.LENGTH_SHORT).show()
            }

            getCountry()

        }
    }

    private fun getCountry() {
        val okHttp = OkHttpClient().newBuilder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .writeTimeout(15, TimeUnit.SECONDS)
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.covid19api.com/dayone/country/")
            .client(okHttp)
            .addConverterFactory(GsonConverterFactory.create())
            .build()


        val api = retrofit.create(InfoService::class.java)
        api.getInfoService(simpanDataNegara).enqueue(object : Callback<List<InfoNegara>>{
            @SuppressLint("SimpleDataFormat")
            @RequiresApi(Build.VERSION_CODES.O)
            override fun onResponse(
                call: Call<List<InfoNegara>>,
                response: Response<List<InfoNegara>>
            ) {
                val getListDataCorona: List<InfoNegara> = response.body()!!
                if (response.isSuccessful){
                    val barEnteries: ArrayList<BarEntry> = ArrayList()
                    val barEnteries2: ArrayList<BarEntry> = ArrayList()
                    val barEnteries3: ArrayList<BarEntry> = ArrayList()
                    val barEnteries4: ArrayList<BarEntry> = ArrayList()
                    var i = 0

                    while (i < getListDataCorona.size){
                        for (s in getListDataCorona){
                            val barEntry  = BarEntry(i.toFloat(), s.Confirmed?.toFloat() ?: 0f)
                            val barEntry2 = BarEntry(i.toFloat(), s.Deaths?.toFloat() ?: 0f)
                            val barEntry3 = BarEntry(i.toFloat(), s.Recovered?.toFloat() ?: 0f)
                            val barEntry4 = BarEntry(i.toFloat(), s.Active?.toFloat() ?: 0f)


                            val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:SS'Z'")
                            val outputFormat = SimpleDateFormat("dd-MM-yyyy")
                            val date: Date? = inputFormat.parse(s.Date!!)
                            val formattedDate: String = outputFormat.format(date!!)
                            dayCases.add(formattedDate)


                            barEnteries.add(barEntry)
                            barEnteries2.add(barEntry2)
                            barEnteries3.add(barEntry3)
                            barEnteries4.add(barEntry4)

                            i++
                        }

                        val xAxis: XAxis = barChartView.xAxis
                        xAxis.valueFormatter = IndexAxisValueFormatter(dayCases)
                        barChartView.axisLeft.axisMinimum = 0f
                        xAxis.position =XAxis.XAxisPosition.BOTTOM
                        xAxis.granularity = 1f
                        xAxis.setCenterAxisLabels(true)
                        xAxis.isGranularityEnabled = true

                        val barDataSet = BarDataSet(barEnteries, "Confirmed")
                        val barDataSet2 = BarDataSet(barEnteries2, "Deaths")
                        val barDataSet3 = BarDataSet(barEnteries3, "Recovered")
                        val barDataSet4 = BarDataSet(barEnteries4, "Active")
                        barDataSet.setColors(Color.parseColor("#F44336"))
                        barDataSet2.setColors(Color.parseColor("#FFEB3B"))
                        barDataSet3.setColors(Color.parseColor("#03DAC5"))
                        barDataSet4.setColors(Color.parseColor("#2196F3"))

                        val data = BarData(barDataSet, barDataSet2, barDataSet3, barDataSet4)
                        barChartView.data = data

                        val barSpace = 0.2f
                        val groupSpace = 0.3f
                        val groupCount = 4f

                        data.barWidth = 0.15f
                        barChartView.invalidate()
                        barChartView.setNoDataTextColor(R.color.black)
                        barChartView.setTouchEnabled(true)
                        barChartView.description.isEnabled
                        barChartView.xAxis.axisMinimum = 0f
                        barChartView.setVisibleXRangeMaximum(0f + barChartView.barData.getGroupWidth(
                            groupSpace,
                            barSpace
                           ) *groupCount
                        )

                        barChartView.groupBars(0f, groupSpace, barSpace)


                    }

                }

            }

            override fun onFailure(call: Call<List<InfoNegara>>, t: Throwable) {
                Toast.makeText(this@ChartCountryActivity, "error re-enter to this country",
                Toast.LENGTH_SHORT).show()
            }

        })

    }

}