package io.felipepoliveira.fpmtoolkit.commons.io

import kotlin.random.Random

class RandomString {
    companion object {

        const val SEED_DIGITS = "0123456789"
        const val SEED_UPPER_CASE_LETTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
        const val SEED_LOWER_CASE_LETTERS = "abcdefghijklmnopqrstuvwxyz"

        fun generate(seed: String, amountOfChars: Int): String {
            if (amountOfChars < 1) {
                throw IllegalArgumentException("The amount of chars should be >= 1")
            }
            if (seed.isEmpty()) {
                throw IllegalArgumentException("The seed can not be empty")
            }

            // generate the random string
            val random = Random(System.currentTimeMillis() - 1024 * 8)
            val randomString = StringBuffer()
            for (i in 1..amountOfChars) {
                randomString.append(seed[random.nextInt(seed.length)])
            }

            return randomString.toString()

        }

    }
}