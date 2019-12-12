package abyss.coroutines

import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

interface Callback<T> {
    fun onComplete(result: T)
    fun onException(e: Exception?)
}

suspend fun <T> awaitCallback(block: (Callback<T>) -> Unit) : T =
    suspendCancellableCoroutine { cont ->
        block(object : Callback<T> {
            override fun onComplete(result: T) = cont.resume(result)
            override fun onException(e: Exception?) {
                e?.let { cont.resumeWithException(it) }
            }
        })
    }


fun test1(a: Int, callback: Callback<String>) {
    Thread.sleep(1000)
    callback.onComplete(a.toString())
}

fun test2(a: String, callback: Callback<String>) {
    Thread.sleep(1000)
    callback.onComplete(a)
}

fun test3(a: Int, b:Int, callback: Callback<Int>) {
    Thread.sleep(1000)
    callback.onComplete(a + b)
}

fun <A, T> toSuspendFunction (fn: (A, Callback<T>) -> Unit): suspend (A)-> T = { a: A ->
    awaitCallback { fn(a, it) }
}

fun <A, B, T> toSuspendFunction (fn: (A, B, Callback<T>) -> Unit): suspend (A, B)-> T = { a: A, b: B ->
    awaitCallback { fn(a, b, it) }
}

fun main(args: Array<String>) = runBlocking {
    val testRes1: String = awaitCallback { test1(5, it) }
    val testRes2: String = awaitCallback { test2("test", it) }
    val testRes3: Int = awaitCallback { test3(5, 3, it) }
    val testRes4: String = toSuspendFunction(::test2)("test")
    val testRes5: Int = toSuspendFunction(::test3)(1, 2)
}