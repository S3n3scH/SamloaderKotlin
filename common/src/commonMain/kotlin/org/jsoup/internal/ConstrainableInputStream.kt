package org.jsoup.internal

import com.soywiz.korio.stream.*
import io.ktor.client.network.sockets.*
import io.ktor.utils.io.errors.*
import kotlinx.datetime.Clock
import org.jsoup.helper.Validate
import kotlin.math.min

/**
 * A jsoup internal class (so don't use it as there is no contract API) that enables constraints on an Input Stream,
 * namely a maximum read size, and the ability to Thread.interrupt() the read.
 */
class ConstrainableInputStream private constructor(private val `in`: SyncInputStream, bufferSize: Int, maxSize: Int) : SyncInputStream {
    private val capped: Boolean
    private val maxSize: Int
    private var startTime: Long
    private var timeout: Long = 0 // optional max time of request
    private var remaining: Int
    private var interrupted: Boolean = false

    init {
        Validate.isTrue(maxSize >= 0)
        this.maxSize = maxSize
        remaining = maxSize
        capped = maxSize != 0
        startTime = Clock.System.now().toEpochMilliseconds()
    }

    override fun read(buffer: ByteArray, offset: Int, len: Int): Int {
        var len: Int = len
        if (interrupted || capped && remaining <= 0) return -1
//        if (Thread.interrupted()) {
//            // interrupted latches, because parse() may call twice (and we still want the thread interupt to clear)
//            interrupted = true
//            return -1
//        }
        if (expired()) throw SocketTimeoutException("Read timeout")
        if (capped && len > remaining) len = remaining // don't read more than desired, even if available
        try {
            val read: Int = `in`.read(buffer, offset, len)
            remaining -= read
            return read
        } catch (e: SocketTimeoutException) {
            return 0
        }
    }

    /**
     * Reads this inputstream to a ByteBuffer. The supplied max may be less than the inputstream's max, to support
     * reading just the first bytes.
     */
    @Throws(IOException::class)
    fun readToByteBuffer(max: Int): ByteArray {
        Validate.isTrue(max >= 0, "maxSize must be 0 (unlimited) or larger")
        val localCapped: Boolean = max > 0 // still possibly capped in total stream
        val bufferSize: Int = if (localCapped && max < DefaultSize) max else DefaultSize
        val readBuffer: ByteArray = ByteArray(bufferSize)
        val write = ByteArray(bufferSize)
        val outStream: SyncOutputStream = write.openSync("w")
        var read: Int
        var remaining: Int = max
        while (true) {
            read = read(readBuffer, 0, if (localCapped) min(remaining, bufferSize) else bufferSize)
            if (read == -1) break
            if (localCapped) { // this local byteBuffer cap may be smaller than the overall maxSize (like when reading first bytes)
                if (read >= remaining) {
                    outStream.write(readBuffer, 0, remaining)
                    break
                }
                remaining -= read
            }
            outStream.write(readBuffer, 0, read)
        }
        return write
    }

    public fun reset() {
        remaining = maxSize
    }

    fun timeout(startTimeNanos: Long, timeoutMillis: Long): ConstrainableInputStream {
        startTime = startTimeNanos
        timeout = timeoutMillis * 1000000
        return this
    }

    private fun expired(): Boolean {
        if (timeout == 0L) return false
        val now: Long = Clock.System.now().toEpochMilliseconds()
        val dur: Long = now - startTime
        return (dur > timeout)
    }

    companion object {
        private val DefaultSize: Int = 1024 * 32

        /**
         * If this InputStream is not already a ConstrainableInputStream, let it be one.
         * @param in the input stream to (maybe) wrap
         * @param bufferSize the buffer size to use when reading
         * @param maxSize the maximum size to allow to be read. 0 == infinite.
         * @return a constrainable input stream
         */
        fun wrap(`in`: SyncInputStream, bufferSize: Int, maxSize: Int): ConstrainableInputStream {
            return if (`in` is ConstrainableInputStream) `in` else ConstrainableInputStream(
                `in`,
                bufferSize,
                maxSize
            )
        }
    }
}
