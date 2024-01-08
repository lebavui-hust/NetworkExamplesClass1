package vn.edu.hust.networkexamples

import android.app.ProgressDialog
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import vn.edu.hust.networkexamples.databinding.ActivityMainBinding
import java.net.HttpURLConnection
import java.net.URL

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkCapabilities = connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
        if (networkCapabilities == null) {
            Log.v("TAG", "No connection")
        } else {
            if (networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                Log.v("TAG", "Has WIFI connection")
            }
            if (networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                Log.v("TAG", "Has cellular connection")
            }
        }

        // sendGet()
        // sendPost()

        binding.buttonDownload.setOnClickListener {
            downloadFile()
        }

//        val jsonString = "[{\"name\":\"John\", \"age\":20, \"gender\":\"male\"}, {\"name\":\"Peter\", \"age\":21, \"gender\":\"male\"}, {\"name\":\"July\", \"age\":19, \"gender\":\"female\"}]"
//        val jArr = JSONArray(jsonString)
//        for (i in 0..jArr.length() - 1) {
//            val jObj = jArr.getJSONObject(i)
//            val name = jObj.getString("name")
//            val age = jObj.getInt("age")
//            val gender = jObj.getString("gender")
//
//            Log.v("TAG", "$name - $age - $gender")
//        }

        val moshi = Moshi.Builder()
            .add((KotlinJsonAdapterFactory()))
            .build()
        val retrofit = Retrofit.Builder()
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .baseUrl("https://jsonplaceholder.typicode.com/")
            .build()
        val myService = retrofit.create(MyService::class.java)

        lifecycleScope.launch(Dispatchers.IO) {
//            val allPosts = myService.getAllPost()
//            Log.v("TAG", "Num posts: ${allPosts.size}")
//            for (post in allPosts) {
//                Log.v("TAG", post.title)
//            }

            val post = myService.getPost(1)
            Log.v("TAG", post.title)
        }

        Glide.with(this)
            .load("https://lebavui.github.io/walls/wall1.jpg")
            .apply(
                RequestOptions()
                .placeholder(R.drawable.baseline_downloading_24)
                .error(R.drawable.baseline_error_outline_24))
            .into(findViewById<ImageView>(R.id.imageView))
    }

    fun sendGet() {
        lifecycleScope.launch(Dispatchers.IO) {
            val url = URL("https://hust.edu.vn")
            val conn = url.openConnection() as HttpURLConnection

            // Get results
            Log.v("TAG", "Response code: ${conn.responseCode}")

            val reader = conn.inputStream.reader()
            val content = reader.readText()
            reader.close()

            Log.v("TAG", content)
        }
    }

    fun downloadFile() {

        val progressDialog = ProgressDialog(this)
        progressDialog.setMessage("Downloading")
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL)
        progressDialog.show()

        lifecycleScope.launch(Dispatchers.IO) {
            Log.v("TAG", "Started")

            val url = URL("https://lebavui.github.io/videos/ecard.mp4")
            val conn = url.openConnection() as HttpURLConnection

            // Get results
            Log.v("TAG", "Response code: ${conn.responseCode}")

            val inputStream = conn.inputStream
            val outputStream = openFileOutput("download.mp4", MODE_PRIVATE)

            val buffer = ByteArray(2048)

            val total = conn.contentLength
            var downloaded = 0

            while (true) {
                val len = inputStream.read(buffer)
                if (len <= 0)
                    break
                outputStream.write(buffer, 0, len)

                downloaded += len
                withContext(Dispatchers.Main) {
                    progressDialog.max = total
                    progressDialog.progress = downloaded
                }
            }

            outputStream.close()
            inputStream.close()

            Log.v("TAG", "Completed")

            withContext(Dispatchers.Main) {
                progressDialog.dismiss()
            }
        }


    }

    fun sendPost() {
        lifecycleScope.launch(Dispatchers.IO) {
            val url = URL("https://httpbin.org/post")
            val conn = url.openConnection() as HttpURLConnection

            conn.requestMethod = "POST"
            conn.doOutput = true

            // Send POST body
            val writer = conn.outputStream.writer()
            writer.write("user=admin&password=123456")
            writer.close()

            // Get results
            Log.v("TAG", "Response code: ${conn.responseCode}")

            val reader = conn.inputStream.reader()
            val content = reader.readText()
            reader.close()

            Log.v("TAG", content)
        }
    }
}