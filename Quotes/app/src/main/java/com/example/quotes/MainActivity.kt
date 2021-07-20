package com.example.quotes

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.viewpager2.widget.ViewPager2
import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.ktx.remoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfigSettings
import org.json.JSONArray
import org.json.JSONObject
import kotlin.math.absoluteValue

class MainActivity : AppCompatActivity() {

    private val viewPager: ViewPager2 by lazy{
        findViewById(R.id.viewPager)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initViews()
        initData()
    }

    private fun initViews(){
        viewPager.setPageTransformer { page, position ->
            // 페이지 넘길 시 글씨 투명도 조정
            when{
                position.absoluteValue >= 1f ->{
                    page.alpha = 0f
                }
                position.absoluteValue == 0f ->{
                    page.alpha = 1f
                }
                else ->{
                    page.alpha = 1f - 2 * position.absoluteValue
                }
            }
        }
    }

    //앱을 실행할 때마다 fetch가 이루어짐
    private fun initData() {
        val remoteConfig = Firebase.remoteConfig
        remoteConfig.setConfigSettingsAsync(
            remoteConfigSettings {
                minimumFetchIntervalInSeconds = 0
            }
        )
        remoteConfig.fetchAndActivate().addOnCompleteListener {
            if (it.isSuccessful){
                val quotes = parseQuotesJson(remoteConfig.getString("quotes"))
                val isNameRevealed = remoteConfig.getBoolean("is_name_revealed")
                displayQuotesPager(quotes,isNameRevealed)

            }
        }
    }
    private fun parseQuotesJson(json: String): List<Quote>{
        val jsonArray = JSONArray(json)
        var jsonList = emptyList<JSONObject>()

        for(index in 0 until jsonArray.length()){
           val jsonObject = jsonArray.getJSONObject(index)
            jsonObject?.let {
                jsonList = jsonList + it
            }
        }

        return  jsonList.map {
            Quote(
                quote = it.getString("quote"),
                name = it.getString("name")
            )
        }
    }

    private fun displayQuotesPager(quotes:List<Quote>, isNameRevealed: Boolean){
        val adapter = QuotesPagerAdapter(
            quotes = quotes,
            isNameRevealed = isNameRevealed
        )
        viewPager.adapter = adapter
        viewPager.setCurrentItem(adapter.itemCount/2 - 3, false)
    }
}