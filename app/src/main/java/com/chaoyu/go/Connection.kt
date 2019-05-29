package com.chaoyu.go

import android.os.Handler
import android.os.Message
import android.util.Log
import com.chaoyu.go.beans.Package
import com.chaoyu.go.utils.*
import java.io.IOException
import java.net.Socket
import java.net.SocketException
import java.net.SocketTimeoutException


class Connection(
    var type : Int,
    var name : String,
    var handler: Handler?,
    val successHandler : ()->Unit,
    val faliedHandler : ()->Unit) {

    companion object {
        val map = HashMap<String,Connection>()
    }


    lateinit var thread : Thread
    lateinit var socket: Socket

    @Volatile
    private var loopFlag = true


    public fun connect(){
        thread = Thread{
            try {
                val socket = Socket("yanglei.duckdns.org",20000)
                this.socket = socket
                startSendThread()
                val pack = Package(type)
                pack.roomName = name
                sendPackage(pack,socket)
                while (loopFlag){
                    val pkg = getPackage(socket)
                    parsePKG(pkg)
                }
            }catch (e:SocketException){
                e.printStackTrace()
            }catch (e:SocketTimeoutException){
                e.printStackTrace()
            }catch (e:IOException){
                e.printStackTrace()
            }finally {
                map.remove(name)
                stopSendThread()
                handler?.obtainMessage(-1)?.sendToTarget()
            }
        }
        thread.start()
    }

    public fun exitLoop(){
        loopFlag = false
    }

    public fun send(x:Int,y:Int,isWhite:Boolean){
        val pkg = Package(Package.LUOZI)
        pkg.x = x
        pkg.y = y
        pkg.roomName = name
        pkg.isWhite = isWhite
        sendPackage(pkg,socket)
    }

    public fun sendEat(x:Int,y: Int,isWhite: Boolean){
        val pkg = Package(Package.EAT)
        pkg.x = x
        pkg.y = y
        pkg.roomName = name
        pkg.isWhite = isWhite
        sendPackage(pkg,socket)
    }

    private fun parsePKG(pkg : Package){
        when(pkg.type){
            Package.CREATE_SUC->{
                map[name] = this
                successHandler.invoke()
            }
            Package.CONNECT_SUC->{
                map[name] = this
                successHandler.invoke()
            }
            Package.CONNECT_FAL->{
                faliedHandler.invoke()
            }
            else->{
                if (handler != null)
                    handler?.obtainMessage(pkg.type,pkg)?.sendToTarget()
                else
                    Log.e(this.javaClass.name,"null handler")
            }
        }
    }


}