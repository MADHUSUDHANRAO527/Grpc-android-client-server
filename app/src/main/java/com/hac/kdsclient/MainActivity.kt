package com.hac.kdsclient

import android.annotation.SuppressLint
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier

import com.hac.kdsclient.ui.theme.KdsClientTheme
import com.hac.protobuf.client.KdsRpc
import com.hac.protobuf.client.KdsServer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.BindException
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
class MainActivity : ComponentActivity() {
    //    private val kdsClient = KdsClient()
//    private val default: SyncResponse = kdsClient.defaultResponse
   // private val uri by lazy { Uri.parse("http://10.0.2.2:50051/") }
    private val uri by lazy { Uri.parse("http://localhost:50051/") }

    //    private val uri by lazy { Uri.parse("http://localhost:50051/") }
    private val service by lazy { KdsRpc(uri) }
    private val server = KdsServer()
//    private val personCollectionRef = Firebase.firestore.collection("persons")

    @OptIn(DelicateCoroutinesApi::class)
    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        GlobalScope.launch {
            try {
                server.start()
            }catch (e: BindException){
                e.printStackTrace()
                runOnUiThread {
                    Toast.makeText(this@MainActivity, e.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
        enableEdgeToEdge()
        setContent {
            KdsClientTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) {
                    Greeter(service)
                }
            }
        }
    }
   /* private fun savePerson(person: Person) = CoroutineScope(Dispatchers.IO).launch {
        try {
            personCollectionRef.add(person)
            withContext(Dispatchers.Main) {
                Toast.makeText(this@MainActivity, "Successfully saved data.", Toast.LENGTH_LONG).show()
            }
        } catch(e: Exception) {
            withContext(Dispatchers.Main) {
                Toast.makeText(this@MainActivity, e.message, Toast.LENGTH_LONG).show()
            }
        }
    }*/
}


@SuppressLint("CoroutineCreationDuringComposition")
@Composable
fun Greeter(kdsRpc: KdsRpc) {
    val response = remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    scope.launch {
        kdsRpc.responseFlow.collect { it ->
            response.value = it
        }
    }
    val stringBuilder = StringBuilder()


    val itemName = remember { mutableStateOf("") }
    val itemQnty = remember { mutableStateOf("") }
    val itemPrice = remember { mutableStateOf("") }
//    val response1 = remember { mutableStateOf<SyncResponse>(default)}
    Column {
        OutlinedTextField(value = itemName.value,
            onValueChange = {
                itemName.value = it
                stringBuilder.append(it)
            },
            label = { Text("Item Name") }
        )
        OutlinedTextField(value = itemQnty.value,
            onValueChange = {
                itemQnty.value = it
                stringBuilder.append("-$it")
            },
            label = { Text("Quantity") }
        )
        OutlinedTextField(value = itemPrice.value,
            onValueChange = {
                itemPrice.value = it
                stringBuilder.append("-$it")
            },
            label = { Text("Price") }
        )

        Button(onClick = {
            scope.launch { kdsRpc.sync(itemName.value,itemQnty.value,itemPrice.value) }
        }) {
            Text("Click to send request!")
        }
        Text("Response is ${response.value}")
    }

}


