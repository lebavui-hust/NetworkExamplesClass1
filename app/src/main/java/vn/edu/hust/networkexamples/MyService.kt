package vn.edu.hust.networkexamples

import retrofit2.http.GET
import retrofit2.http.Path

interface MyService {
    @GET("posts")
    suspend fun getAllPost(): List<Post>

    @GET("posts/{postId}")
    suspend fun getPost(@Path("postId") postId: Int): Post
}