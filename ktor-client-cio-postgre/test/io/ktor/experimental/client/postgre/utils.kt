package io.ktor.experimental.client.postgre

import io.ktor.experimental.client.db.*
import kotlinx.coroutines.experimental.*
import kotlinx.io.core.*
import java.net.*
import kotlin.system.*

fun postgreTest(
    address: InetSocketAddress,
    database: String = "postgres", user: String = "myuser", password: String = "hello",
    block: suspend PostgreClient.() -> Unit
): Unit = runBlocking {
    PostgreClient(address, database, user, password).use { it.block() }
}

fun main(args: Array<String>) = runBlocking {
    val client = PostgreClient(
        InetSocketAddress("0.0.0.0", 32768),
        "postgres", "myuser", "hello"
    )

    client.query("DROP TABLE IF EXISTS Persons;")
        .log()

    client.query("DROP TABLE IF EXISTS Persons;")
        .log()

    client.query(
        "CREATE TABLE IF NOT EXISTS Persons (\n" +
                "    PersonID int NOT NULL UNIQUE,\n" +
                "    LastName varchar(255),\n" +
                "    FirstName varchar(255),\n" +
                "    Address varchar(255),\n" +
                "    City varchar(255) \n" +
                ");"
    ).log()


    val time = measureTimeMillis {
        (0..1000).map { id ->
            async {
                client.query(
                    "INSERT INTO Persons VALUES" +
                            "($id, 'lastname$id', 'firstname$id', 'address$id', 'city$id');"
                )
            }
        }.awaitAll()
    }
    println(time)

    val response = client.query("SELECT * FROM persons;")
        .log()

    val rows = response.rows.iterator()
    while (rows.hasNext()) {
        rows.next()
            .log()
    }

    client.close()
}

private fun <T> T.log(): T {
    println(this)
    return this
}