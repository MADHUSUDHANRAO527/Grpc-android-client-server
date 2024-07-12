package com.hac.protobuf.client

import com.ncr.proto.gen.KdsServiceGrpcKt
import com.ncr.proto.gen.SyncRequest
import com.ncr.proto.gen.SyncResponse
import io.grpc.Grpc
import io.grpc.InsecureServerCredentials
import io.grpc.Server
import io.grpc.ServerBuilder
import java.time.LocalDateTime


class KdsServer {
    val port: Int = 50051
    private val serverBuilder: ServerBuilder<*> = Grpc.newServerBuilderForPort(port, InsecureServerCredentials.create())
    val server: Server = serverBuilder
            .addService(KdsController())
            .build()

    fun start() {
        server.start()
        println("Server started, listening on $port")
        Runtime.getRuntime().addShutdownHook(
            Thread {
                println("*** shutting down gRPC server since JVM is shutting down")
                this@KdsServer.stop()
                println("*** server shut down")
            },
        )
    }

    private fun stop() {
        server.shutdown()
    }

    fun blockUntilShutdown() {
        server.awaitTermination()
    }
}


class KdsController : KdsServiceGrpcKt.KdsServiceCoroutineImplBase() {
    override suspend fun sync(request: SyncRequest): SyncResponse {
        println("sync")
        println(request)
        println(request.itemName)
        val response: SyncResponse = SyncResponse.newBuilder()
            .setValue(request.itemName)
            .build()

        return response
    }
}