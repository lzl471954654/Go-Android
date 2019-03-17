package com.chaoyu.go

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.widget.Toast
import com.chaoyu.go.beans.Package
import kotlinx.android.synthetic.main.activity_create_room.*

class MainActivity : AppCompatActivity() {

    var type = 1
    lateinit var name : String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_room)

        connect_button.setOnClickListener {
            connected()
            type = 2
        }

        create_button.setOnClickListener {
            create()
            type = 1
        }

    }

    private fun connected(){
        progress.visibility = View.VISIBLE
        tips.visibility = View.VISIBLE
        connect_button.isEnabled = false
        name = connect_room_input.text.toString()
        val c = Connection(Package.CONNECT_ROOM,connect_room_input.text.toString(),null,{success()},{failed()})
        c.connect()
    }

    private fun create(){
        progress.visibility = View.VISIBLE
        tips.visibility = View.VISIBLE
        create_button.isEnabled = false
        name = create_room_input.text.toString()
        val c = Connection(Package.CREATE_ROOM,create_room_input.text.toString(),null,{success()},{failed()})
        c.connect()
    }

    private fun failed(){
        runOnUiThread {
            progress.visibility = View.INVISIBLE
            tips.visibility = View.INVISIBLE
            Toast.makeText(this,"连接失败",Toast.LENGTH_LONG).show()
            connect_button.isEnabled = true
            create_button.isEnabled = true
        }
    }

    private fun success(){
        runOnUiThread {
            progress.visibility = View.INVISIBLE
            tips.visibility = View.INVISIBLE
            Toast.makeText(this,"连接成功",Toast.LENGTH_LONG).show()
            Thread{
                Thread.sleep(2000)
                runOnUiThread {
                    val intent = Intent(this,ChoseRoomActivity::class.java)
                    intent.putExtra("type",type)
                    intent.putExtra("name",name)
                    startActivity(intent)
                    finish()
                }
            }.start()
        }
    }


}
