package io.felipepoliveira.fpmtoolkit.tests

import io.kotest.core.config.AbstractProjectConfig
import io.kotest.core.extensions.Extension
import io.kotest.extensions.spring.SpringExtension
import io.kotest.spring.SpringAutowireConstructorExtension

class KotestConfig : AbstractProjectConfig() {
    override fun extensions() = listOf(SpringExtension, SpringAutowireConstructorExtension)
}