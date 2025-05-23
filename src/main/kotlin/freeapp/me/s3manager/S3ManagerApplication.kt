package freeapp.me.s3manager

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class S3ManagerApplication

fun main(args: Array<String>) {
    runApplication<S3ManagerApplication>(*args)
}
