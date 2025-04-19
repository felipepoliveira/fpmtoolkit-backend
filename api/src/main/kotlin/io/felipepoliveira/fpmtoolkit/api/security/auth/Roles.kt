package io.felipepoliveira.fpmtoolkit.api.security.auth

class Roles {
    companion object {
        const val ROLE_ORG_ADMINISTRATOR = "ROLE_ORG_ADMINISTRATOR"
        const val ROLE_ORG_MEMBER_ADMINISTRATOR = "ROLE_ORG_MEMBER_ADMINISTRATOR"

        const val STL_MOST_SECURE = "ROLE_STL_MOST_SECURE"
        const val STL_SAME_SESSION = "ROLE_STL_SAME_SESSION"
        const val STL_SECURE = "ROLE_STL_SECURE"
    }
}