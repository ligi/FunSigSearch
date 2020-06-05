import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.channels.produce
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.walleth.khex.hexToByteArray
import java.io.File
import java.security.MessageDigest
import java.security.Security

fun main(args: Array<String>) = runBlocking {
    val channel = produce {
        val signatures = File("function_signtures.csv").readLines().plus("")
        File("function_names.csv").inputStream().bufferedReader().lineSequence().forEach { name ->
            signatures.forEach { sig ->
                send("$name($sig)")
            }
        }

        close()
    }

    Security.addProvider(BouncyCastleProvider())
    val needle = args.first().hexToByteArray()
    repeat(18) {
        GlobalScope.launch {
            val digester = MessageDigest.getInstance("Keccak-256")
            channel.consumeEach {
                val hash = digester.digest(it.toByteArray())
                if (hash[0] == needle[0] && hash[1] == needle[1] && hash[2] == needle[2] && hash[3] == needle[3]) {
                    println(it)
                }
                digester.reset()
            }
        }
    }

}