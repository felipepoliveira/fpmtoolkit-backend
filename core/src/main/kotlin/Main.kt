package io.felipepoliveira.fpmtoolkit

import io.felipepoliveira.fpmtoolkit.beans.AppContext
import io.felipepoliveira.fpmtoolkit.beans.AppContextProvider
import io.felipepoliveira.fpmtoolkit.beans.CoreBeans
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import

@Configuration
@Import(CoreBeans::class)
class DummyLauncherConfig : AppContextProvider {
    override fun getAppContext(): AppContext = AppContext.DEVELOPMENT
}

fun main() {
    runApplication<DummyLauncherConfig>()
}