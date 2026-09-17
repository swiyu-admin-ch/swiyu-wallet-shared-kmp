package ch.admin.foitt.swiyu.shared.passwordstrengthestimator

import ch.admin.foitt.swiyu.shared.passwordstrengthestimator.model.PasswordStrength
import uniffi.password_strength_estimator_wrapper.evaluatePassword

class PasswordEvaluator {
    /**
     * @param password the password to evaluate.
     * @param blackList context-specific words not allowed
     */
    fun evaluate(password: String, blackList: List<String> = emptyList()): PasswordStrength {
        return evaluatePassword(password = password, inputs = blackList)
    }
}