package copas.app

import io.ktor.client.*
import io.ktor.client.engine.android.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ApiClient(private val baseUrl: String, private val token: String) {
  private val client = HttpClient(Android)

  suspend fun pullClipboard(): String =
          withContext(Dispatchers.IO) {
            val response = client.get("$baseUrl/pull")
            response.bodyAsText()
          }

  suspend fun pushClipboard(text: String) =
          withContext(Dispatchers.IO) {
            client.post("$baseUrl/push") {
              url { parameters.append("token", token) }
              setBody("text=$text")
            }
          }
}
