package com.chaoyu.go

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Point
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.Toast
import com.chaoyu.go.beans.Package
import kotlinx.android.synthetic.main.activity_main.*
import org.jetbrains.anko.doAsync
import org.json.JSONObject

class ChoseRoomActivity : AppCompatActivity() {

    var isInit = false
    var type = 1
    lateinit var name : String

    @SuppressLint("HandlerLeak")
    val handler = object : Handler() {
        override fun handleMessage(msg: Message?) {
            when(msg?.what){
                Package.EXIT->{
                    exit()
                }
                Package.LUOZI->{
                    val pkg = msg.obj as Package
                    panel.addPoint(pkg.x,pkg.y,pkg.isWhite)
                }
                Package.GAME_START->{
                    Toast.makeText(this@ChoseRoomActivity,"游戏开始，你是白棋",Toast.LENGTH_SHORT).show()
                    initPanel()
                }
                Package.EAT->{
                    val pkg = msg.obj as Package
                    panel.beEat(pkg.isWhite, Point(pkg.x,pkg.y))
                }
            }
        }
    }

    private fun initPanel(){
        panel.setGameStart(type)
        /*if (!isInit){
            panel.reInit(11,type,callback)
        }*/
    }

    private fun exit(){
        Toast.makeText(this@ChoseRoomActivity,"连接断开",Toast.LENGTH_SHORT).show()
        Thread{
            Thread.sleep(2000)
            runOnUiThread {
                Connection.map[name]?.exitLoop()
                val intent = Intent(this@ChoseRoomActivity,MainActivity::class.java)
                startActivity(intent)
                finish()
            }
        }
    }



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        type = intent.getIntExtra("type",1)
        if (type == 2){
            panel.setGameStart(type)
        }
        name = intent.getStringExtra("name")
        /*panel.reInit(11,type,callback)
        panel.visibility = View.VISIBLE*/
        panel.setCallback(callback)
        Connection.map[name]!!.handler = handler
    }


    val callback = object : StatusCallback {
        override fun send(x: Int, y: Int, isWhite: Boolean) {
            Connection.map[name]!!.send(x,y,isWhite)
        }

        override fun updateMsg(status: String, msg: String) {
            val newMessage = "${message.text}\n$msg"
            message.text = newMessage
        }

        override fun sendEat(x: Int, y: Int, isWhite: Boolean) {
            Connection.map[name]!!.sendEat(x,y,isWhite)
        }

        override fun win(isWhite: Boolean) {
            Toast.makeText(this@ChoseRoomActivity, if (isWhite) "白棋胜利" else "黑棋胜利" ,Toast.LENGTH_LONG).show()
            Thread{
                Thread.sleep(1000)
                runOnUiThread {
                    finish()
                }
            }.start()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacksAndMessages(null)
    }
}

interface StatusCallback{
    fun send(x:Int,y:Int,isWhite:Boolean)
    fun updateMsg(status:String,msg:String)
    fun sendEat(x: Int,y: Int,isWhite: Boolean)
    fun win(isWhite: Boolean)
}