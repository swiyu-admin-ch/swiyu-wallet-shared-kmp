package ch.admin.foitt.swiyu.shared.passwordstrengthestimator.services

import ch.admin.foitt.swiyu.shared.passwordstrengthestimator.PasswordEvaluator
import ch.admin.foitt.swiyu.shared.passwordstrengthestimator.model.PasswordStrength
import kotlin.test.Test
import kotlin.test.assertEquals

class PasswordEvaluatorTest {

    private val evaluator = PasswordEvaluator()

    @Test
    fun weakPasswords_returnWeak() {
        weakPasswords.forEach { password ->
            assertEquals(PasswordStrength.WEAK, evaluator.evaluate(password), "for \"$password\"")
        }
    }

    @Test
    fun moderatePasswords_returnModerate() {
        moderatePasswords.forEach { password ->
            assertEquals(PasswordStrength.MODERATE, evaluator.evaluate(password), "for \"$password\"")
        }
    }

    @Test
    fun strongPasswords_returnStrong() {
        strongPasswords.forEach { password ->
            assertEquals(PasswordStrength.STRONG, evaluator.evaluate(password), "for \"$password\"")
        }
    }

    @Test
    fun blackListContainsPassword_returnWeak() {
        val strength = evaluator.evaluate("swiyu_2", blackList = mockBlackList)
        assertEquals(PasswordStrength.WEAK, strength)
    }

    private val weakPasswords = listOf("Password1!", "qwerty", "aaaaaa", "hunter2", "", "123456789")
    private val moderatePasswords = listOf("Summer2024!", "swiyu-10?%")
    private val strongPasswords = listOf("k7Rq\$m2Vx9pL", "plum-tiger-9-velvet-comb", "]-(|\"\$\"<^_,#{.?,'.'::!!&&#|~#-\$=", "J.i<yyBddw<S@LgKs)UPsq=GX!@8e{c0T~%:`qv@H:#neS.6o&")
    private val mockBlackList = listOf("swiyu_1", "swiyu_2")
}