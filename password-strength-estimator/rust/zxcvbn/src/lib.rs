use zxcvbn::Score;

#[derive(uniffi::Enum)]
pub enum PasswordStrength {
    Weak,
    Moderate,
    Strong,
}

#[uniffi::export]
fn evaluate_password(password: String, inputs: Vec<String>) -> PasswordStrength {
    let input_refs: Vec<&str> = inputs.iter().map(String::as_str).collect();
    match zxcvbn::zxcvbn(&password, &input_refs).score() {
        Score::Zero | Score::One => PasswordStrength::Weak,
        Score::Two | Score::Three => PasswordStrength::Moderate,
        Score::Four => PasswordStrength::Strong,
        _ => PasswordStrength::Weak,
    }
}

uniffi::setup_scaffolding!();