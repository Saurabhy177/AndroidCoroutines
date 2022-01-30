package com.example.androidcoroutines

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.*
import kotlin.system.measureTimeMillis

class MainActivity : AppCompatActivity() {

    val TAG = this.javaClass.name

    lateinit var tvDummy: TextView
    lateinit var btnNext: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tvDummy = findViewById(R.id.tvDummy)
        btnNext = findViewById(R.id.btnNext)

        btnNext.setOnClickListener {
            coroutineScope()
        }

        //kotlinCoroutine()
        //coroutineContext()
        //switchingCoroutineContext()
        //coroutineRunBlocking()
        //coroutineJobs()
        //cancelLongRunningJobs()
        //autoCancelLongRunningJobs()
        //coroutineAsyncAndAwait()
    }

    /**
     * Performing a set of instructions in a new thread using coroutine.
     * */
    private fun kotlinCoroutine() {
        // Global scope means the coroutine will live as long as the application lives.
        GlobalScope.launch {
            // delay() pauses only the current coroutine and not the whole thread.
            delay(1000L)

            // Coroutines launches a new thread
            Log.d(TAG, "Coroutine says hello from ${Thread.currentThread().name}")

            val networkCallAnswer = doNetworkCall()
            val networkCallAnswer2 = doNetworkCall2()
            Log.d(TAG, networkCallAnswer)
            Log.d(TAG, networkCallAnswer2)
        }

        Log.d(TAG, "Hello from  ${Thread.currentThread().name}")
    }

    /**
     * suspend function can only be called either from a suspend function
     * or within a coroutine.
     * */
    private suspend fun doNetworkCall(): String {
        delay(3000L)
        return "This is the network call answer"
    }

    private suspend fun doNetworkCall2(): String {
        delay(3000L)
        return "This is the second network call answer"
    }

    /**
     * Coroutine context determines coroutines will be started in which thread.
     * */
    private fun coroutineContext() {
        GlobalScope.launch(Dispatchers.Main) {
            // Dispatchers.Main starts the coroutine in the main thread.
            // UI operations can only be performed from the main thread.
            Log.d(TAG, "Hello from main context: ${Thread.currentThread().name}")
        }

        GlobalScope.launch(Dispatchers.IO) {
            // Dispatchers.IO starts the coroutine in the IO thread used for all sorts of
            // data operations - networking, database operations & reading/writing files.
            Log.d(TAG, "Hello from io context: ${Thread.currentThread().name}")
        }

        GlobalScope.launch(Dispatchers.Default) {
            // Dispatchers.Default starts the coroutine in the Default thread & used when
            // we want to perform complex & long-running calculations i.e. heavy CPU operations.
            Log.d(TAG, "Hello from default context: ${Thread.currentThread().name}")
        }

        GlobalScope.launch(Dispatchers.Unconfined) {
            // Dispatchers.Unconfined is not confined to any specific thread.
            // It executes the initial continuation of a coroutine in the current call-frame and
            // lets the coroutine resume in whatever thread being used by the corresponding
            // suspending function.
            Log.d(TAG, "Hello from unconfined context: ${Thread.currentThread().name}")
        }

        GlobalScope.launch(newSingleThreadContext("MyThread")) {
            // Creating a custom thread
            Log.d(TAG, "Hello from custom context: ${Thread.currentThread().name}")
        }
    }

    /**
     * We can easily switch coroutine context within a coroutine.
     * */
    private fun switchingCoroutineContext() {
        GlobalScope.launch(Dispatchers.IO) {
            Log.d(TAG, "Starting coroutine in thread ${Thread.currentThread().name}")
            val answer = doNetworkCall()

            // It will cause error as ui can only be accessed from main thread.
            // tvDummy.text = answer

            withContext(Dispatchers.Main) {
                Log.d(TAG, "Setting text in thread ${Thread.currentThread().name}")

                // Switching coroutine context to main thread so as to update views.
                tvDummy.text = answer
            }
        }
    }

    /**
     * runBlocking creates a coroutine in the main thread and also
     * blocks the main thread.
     * */
    private fun coroutineRunBlocking() {
        Log.d(TAG, "Before runBlocking in thread ${Thread.currentThread().name}")

        runBlocking {
            // Both IO coroutines run asynchronously
            launch(Dispatchers.IO) {
                delay(3000L)
                Log.d(TAG, "Finished IO coroutine 1")
            }
            launch(Dispatchers.IO) {
                delay(3000L)
                Log.d(TAG, "Finished IO coroutine 2")
            }
            Log.d(TAG, "Start of runBlocking in thread ${Thread.currentThread().name}")
            delay(5000L)
            Log.d(TAG, "End of runBlocking in thread ${Thread.currentThread().name}")
        }

        Log.d(TAG, "After runBlocking in thread ${Thread.currentThread().name}")
    }

    private fun coroutineJobs() {
        val job = GlobalScope.launch(Dispatchers.Default) {
            repeat(5) {
                Log.d(TAG, "Coroutine is still running ...")
                delay(1000L)
            }
        }

        runBlocking {
            // Blocking the main thread till the coroutine job is finished.
            //job.join()

            // Cancelling the job after 2 seconds.
            delay(2000L)
            job.cancel()

            Log.d(TAG, "Main thread is continuing ...")
        }
    }

    private fun cancelLongRunningJobs() {
        val job = GlobalScope.launch(Dispatchers.Default) {
            Log.d(TAG, "Starting long running calculations ...")
            for (i in 30..50) {
                // Coroutine is too busy in calculations to check if the job has been cancelled.
                // Also, there is no pause or delay in this call. So, we have to check manually.
                if (isActive) {
                    // Checking if the coroutine is still active or cancelled.
                    Log.d(TAG, "Result for i = $i: ${fib(i)}")
                }
            }
            Log.d(TAG, "Ending long running calculations ...")
        }

        runBlocking {
            delay(2000L)
            job.cancel()

            Log.d(TAG, "Cancelled job!")
        }
    }

    private fun autoCancelLongRunningJobs() {
        val job = GlobalScope.launch(Dispatchers.Default) {
            Log.d(TAG, "Starting long running calculations ...")
            withTimeout(2000L) {
                for (i in 30..50) {
                    // Coroutine is too busy in calculations to check if the job has been cancelled.
                    // Also, there is no pause or delay in this call. So, we have to check manually.
                    if (isActive) {
                        // Checking if the coroutine is still active or cancelled.
                        Log.d(TAG, "Result for i = $i: ${fib(i)}")
                    }
                }
            }
            Log.d(TAG, "Ending long running calculations ...")
        }
    }

    private fun fib(n: Int): Long {
        return if (n == 0) 0
        else if (n == 1) 1
        else fib(n - 1) + fib(n - 2)
    }

    private fun coroutineAsyncAndAwait() {
        GlobalScope.launch(Dispatchers.IO) {
            val time = measureTimeMillis {
                // The two calls are happening sequentially.
                val answer1 = doNetworkCall()
                val answer2 = doNetworkCall2()

                Log.d(TAG, "Answer 1 is $answer1")
                Log.d(TAG, "Answer 2 is $answer2")
            }
            Log.d(TAG, "Sequential request took $time ms.")

            val asyncTime = measureTimeMillis {
                // Performing calls asynchronously using async.
                // async returns a Deferred<return_type> object.
                val answer1 = async { doNetworkCall() }
                val answer2 = async { doNetworkCall2() }

                // Awaits for completion of this value without blocking a thread and resumes
                // when deferred computation is complete, returning the resulting value
                // or throwing the corresponding exception if the deferred was cancelled.
                // Here, we are using await() as the result will be ready in some time.
                Log.d(TAG, "Answer 1 is ${answer1.await()}")
                Log.d(TAG, "Answer 2 is ${answer2.await()}")
            }
            Log.d(TAG, "Async request took $asyncTime ms.")
        }
    }

    private fun coroutineScope() {
        // lifecycleScope will stick coroutine to the lifecycle of the MainActivity
        // and it will be cancelled when the activity is finished / destroyed.
        lifecycleScope.launch {
            while (true) {
                // Also, checking if the coroutine is active for long running tasks.
                if (isActive) {
                    delay(1000L)
                    Log.d(TAG, "Global scope still running ...")
                }
            }
        }

        GlobalScope.launch {
            delay(5000L)
            Log.d(TAG, "Opened second activity!")
            Intent(this@MainActivity, SecondActivity::class.java).also {
                startActivity(it)
                finish()
            }
        }
    }
}