package io.kweb

import com.google.gson.Gson
import io.kweb.dom.element.Element
import org.apache.commons.lang3.StringEscapeUtils
import java.util.*
import java.util.concurrent.*
import kotlin.reflect.KClass
import kotlin.reflect.jvm.jvmName

/**
 * Created by ian on 1/7/17.
 */


val random = Random()

val gson = Gson()

val scheduledExecutorService: ScheduledExecutorService = Executors.newScheduledThreadPool(5)

fun wait(delay: Long, unit : TimeUnit, toRun : () -> Unit): ScheduledFuture<*> = scheduledExecutorService.schedule(toRun, delay, unit)

fun String.escapeEcma() = StringEscapeUtils.escapeEcmaScript(this)

fun Any.toJson(): String = gson.toJson(this)

fun <T> warnIfBlocking(maxTimeMs: Long, onBlock : (Thread) -> Unit, f : () -> T) : T {
    val runningThread = Thread.currentThread()
    val watcher = scheduledExecutorService.schedule(object : Runnable {
        override fun run() {
            onBlock(runningThread)
        }

    }, maxTimeMs, TimeUnit.MILLISECONDS)
    val r = f()
    watcher.cancel(false)
    return r
}

/**
 * Dump a stacktrace generated by a user-supplied lambda, but attempt to remove irrelevant lines to the
 * trace.  This is a little ugly but seems to work well, there may be a better approach.
 */
fun Array<StackTraceElement>.pruneAndDumpStackTo(logStatementBuilder: StringBuilder) {
    val disregardClassPrefixes = listOf(Kweb::class.jvmName, WebBrowser::class.jvmName, Element::class.jvmName, "org.jetbrains.ktor", "io.netty", "java.lang", "kotlin.coroutines", "kotlinx.coroutines")
    this.filter { ste -> ste.lineNumber >= 0 && !disregardClassPrefixes.any { ste.className.startsWith(it) } }.forEach { stackTraceElement ->
        logStatementBuilder.appendln("        at ${stackTraceElement.className}.${stackTraceElement.methodName}(${stackTraceElement.fileName}:${stackTraceElement.lineNumber})")
    }
}

val <T : Any> KClass<T>.pkg : String get() {
    val packageName = qualifiedName
    val className = simpleName
    return if (packageName != null && className != null) {
        val endIndex = packageName.length - className.length - 1
        packageName.substring(0, endIndex)
    } else {
        throw RuntimeException("Cannot determine package for $this because it may be local or an anonymous object literal")
    }
}

class CacheResult<in I, out O>(val f : (I) -> O) {
    private val cache = ConcurrentHashMap<I, O>()
    operator fun get(i : I) {

    }
}

data class NotFoundException(override val message: String) : Exception(message)