package io.felipepoliveira.fpmtoolkit.beans

interface AppContextProvider {
    /**
     * Return the used app context of the application
     */
    fun getAppContext(): AppContext
}

enum class AppContext {
    /**
     * Identify development context. Used only on development context
     */
    DEVELOPMENT,
    ;
}