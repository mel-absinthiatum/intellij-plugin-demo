package abyss.coroutines

import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine


class CoroutinePlayground {
    fun runTest() = runBlocking {
        val flow = doubleStringFlow()
        flow.collect { println("collected from flow: $it")}
    }

    fun blockingRun() = runBlocking<Unit> {
        launch() {
            val s = coroutineTest()
            println(s)
        }
    }

    // To Launch on UI
    // launch(UI) {
    //
    // show message
    // }

    suspend fun blockingRun2() = coroutineScope {
        launch() {
            val s = coroutineTest()
            println(s)
        }
    }

    // Plain list building
//    suspend fun list(): List<String> = buildList {
//        add(compute("a"))
//        add(compute("b"))
//        add(compute("c"))
//    }

    private suspend fun stringFlow(): Flow<String> = flow {
        emit(compute("a"))
        emit(compute("b"))
        emit(compute("c"))
    }

    private suspend fun doubleStringFlow(): Flow<String> = stringFlow().map { compute(" double $it") }


    private suspend fun compute(text: String): String {
        delay(20)
        return "delayed $text"
    }

    private suspend fun coroutineTest(): String = suspendCoroutine { cont ->
        cont.resume("coroutine produced value")
    }
}