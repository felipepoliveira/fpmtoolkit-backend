package io.felipepoliveira.fpmtoolkit.tests.unit.security

import io.felipepoliveira.fpmtoolkit.security.PasswordRank
import io.felipepoliveira.fpmtoolkit.security.calculatePasswordRank
import io.felipepoliveira.fpmtoolkit.security.comparePassword
import io.felipepoliveira.fpmtoolkit.security.hashPassword
import io.felipepoliveira.fpmtoolkit.tests.UnitTestsConfiguration
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ContextConfiguration

@SpringBootTest
@ContextConfiguration(classes = [UnitTestsConfiguration::class])
class PasswordSecurityTests : FunSpec({

    test("Check for expected password ranks depending on the input") {
        calculatePasswordRank("ThisShouldBeAtLeastAcceptable1!") shouldBe PasswordRank.Acceptable
        calculatePasswordRank("ThisIsAlmostSafeButDoesNotHaveASymbol1") shouldBe PasswordRank.NotAcceptable
        calculatePasswordRank("ThisIsAlmostSafeButDoesNotHaveADigit!") shouldBe PasswordRank.NotAcceptable
        calculatePasswordRank("ThisIsAlmostSafeButDoesItJustHaveASpaceChar ") shouldBe PasswordRank.NotAcceptable
        calculatePasswordRank("Ab1!@") shouldBe PasswordRank.NotAcceptable // because is too short
    }

    test("Test if password hashing and matching is working") {
        val passwordA = "PasswordA"
        val passwordB = "PasswordB"
        val hashedPasswordA = hashPassword(passwordA)
        val hashedPasswordB = hashPassword(passwordB)

        comparePassword(passwordA, hashedPasswordA) shouldBe true
        comparePassword(passwordA, hashedPasswordB) shouldBe false
        comparePassword(passwordB, hashedPasswordB) shouldBe true
        comparePassword(passwordB, hashedPasswordA) shouldBe false
    }

    test("Test if the same password generate different hashes") {
        val password = "Password"
        val hashedPassword1 = hashPassword(password)
        val hashedPassword2 = hashPassword(password)
        val hashedPassword3 = hashPassword(password)

        hashedPassword1 shouldNotBe hashedPassword2 shouldNotBe hashedPassword3
    }
})
