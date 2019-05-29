package com.chaoyu.go

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_account_login.*
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import org.jetbrains.anko.doAsync
import org.json.JSONObject

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_account_login)
        initListener()
    }

    fun initListener(){
        account_ac_commit.setOnClickListener {
            val userAccount = account_ac_id.text.toString()
            val userPass = account_ac_pass.text.toString()
            doAsync {
                val formBody = FormBody.Builder()
                    .add("userAccount",userAccount)
                    .add("userPass",userPass)
                    .build()
                val request = Request.Builder()
                    .url("http://dev.server.liuzhilin.io:10000/user/goLogin")
                    .post(formBody)
                    .build()
                val response = client.newCall(request).execute()
                val data = response.body()!!.string()
                val jsonObject = JSONObject(data)
                val code = jsonObject.getInt("code")
                if (code == 1){
                    runOnUiThread {
                        Toast.makeText(this@LoginActivity,"登录成功",Toast.LENGTH_LONG).show()
                        val intent = Intent(this@LoginActivity,MainActivity::class.java)
                        startActivity(intent)
                    }
                }else{
                    runOnUiThread {
                        Toast.makeText(this@LoginActivity,"登录失败，请重试",Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }

    companion object{
        val client = OkHttpClient()
    }


}