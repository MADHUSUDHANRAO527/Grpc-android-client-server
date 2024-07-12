package com.hac.protobuf.client

import android.net.Uri
import com.ncr.proto.gen.KdsServiceGrpcKt
import com.ncr.proto.gen.SyncRequest
import io.grpc.ManagedChannelBuilder
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.asExecutor
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import java.io.Closeable

//class KdsClient {
//    var managedChannel = ManagedChannelBuilder.forAddress("10.0.2.2", 50051).build()
//    var blockingStub = KdsServiceGrpc.newBlockingStub(managedChannel)
//
//    fun sendRequest(lastRead: String, lastSent: String, nodeId:String): SyncResponse {
//        val request = SyncRequest.newBuilder()
//            .setLastRead(lastRead)
//            .setLastSent(lastSent)
//            .setNodeId(nodeId)
//            .build()
//
//        return blockingStub.sync(request)
//    }
//
//    val defaultResponse:SyncResponse = SyncResponse.newBuilder().setValue("default").build()
//
//}

class KdsRpc(uri: Uri) : Closeable {

    val responseFlow = MutableSharedFlow<String>()

    private val channel = let {
        println("Connecting to ${uri.host}:${uri.port}")

        val builder = ManagedChannelBuilder.forAddress(uri.host, uri.port)
        if (uri.scheme == "https") {
            builder.useTransportSecurity()
        } else {
            builder.usePlaintext()
        }

        builder.executor(Dispatchers.IO.asExecutor()).build()
    }

    private val stub = KdsServiceGrpcKt.KdsServiceCoroutineStub(channel)

    @OptIn(DelicateCoroutinesApi::class)
    suspend fun sync(name: String, qty: String, price: String) {
        try {
            val res = channel.getState(true)
            println(res)
            val request = SyncRequest.newBuilder()
                .setItemName(name)
                .setItemQnty(qty)
                .setItemPrice(price)
                .build()
            GlobalScope.launch(Dispatchers.Main) {

                val response = async { stub.sync(request) }.await()
                println("Madhu -> $response")
                responseFlow.emit(response.value)
            }.join()

        } catch (e: Exception) {
            responseFlow.emit(e.message ?: "Unknown Error")
            e.printStackTrace()
        }
    }

    override fun close() {
        channel.shutdownNow()
    }
}