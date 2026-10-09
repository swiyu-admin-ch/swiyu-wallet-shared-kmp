use std::collections::HashSet;
use std::sync::LazyLock;
use zxcvbn::Score;

static COMMON_PASSWORDS: LazyLock<HashSet<String>> = LazyLock::new(|| {
    include_str!("../resources/swiyu-weak-passwords.txt")
        .lines()
        .map(str::trim)
        .filter(|line| !line.is_empty())
        .map(str::to_lowercase)
        .collect()
});

#[derive(uniffi::Enum)]
pub enum PasswordStrength {
    Weak,
    Moderate,
    Strong,
}

#[uniffi::export]
fn evaluate_password(password: String, inputs: Vec<String>) -> PasswordStrength {
    if COMMON_PASSWORDS.contains(&password.to_lowercase()) {
        return PasswordStrength::Weak;
    }
    let input_refs: Vec<&str> = inputs.iter().map(String::as_str).collect();
    match zxcvbn::zxcvbn(&password, &input_refs).score() {
        Score::Zero | Score::One => PasswordStrength::Weak,
        Score::Two | Score::Three => PasswordStrength::Moderate,
        Score::Four => PasswordStrength::Strong,
        _ => PasswordStrength::Weak,
    }
}

uniffi::setup_scaffolding!();