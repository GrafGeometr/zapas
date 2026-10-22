//! Работа с паролями.
use crate::err::AppErr;
use argon2::{Argon2, PasswordHasher, PasswordVerifier};
use rand::{RngExt, seq::SliceRandom};
use tracing::{error, warn};

/// Создание `argon2` хеш пароля.
pub fn gen_passwd_hash(passwd: &str) -> Result<String, AppErr> {
    let argon2 = Argon2::default();
    let res = argon2
        .hash_password(passwd.as_bytes())
        .map_err(|err| {
            error!("Argon2 error: {}", err);
            AppErr::Internal
        })?
        .to_string();
    Ok(res)
}

/// Проверка пароля по хешу.
pub fn check_passwd(hash: &str, passwd: &str) -> Result<(), AppErr> {
    let argon2 = Argon2::default();
    argon2
        .verify_password(passwd.as_bytes(), hash)
        .map_err(|err| {
            warn!("Argon2 error: {}", err);
            AppErr::BadPasswd
        })?;
    Ok(())
}

#[derive(Clone, Copy)]
enum CharType {
    Digit,
    Alpha,
    BigAlpha,
    Spec,
}

fn array_by_char_type(t: CharType) -> &'static [u8] {
    match t {
        CharType::Digit => b"0123456789",
        CharType::Alpha => b"qwertyuiopasdfghjklzxcvbnm",
        CharType::BigAlpha => b"QWERTYUIOPASDFGHJKLZXCVBNM",
        CharType::Spec => b"!@#$%^&*()_+",
    }
}

const GEN_PASSWD_LEN: usize = 20;
const CHAR_TYPES: [CharType; 4] = [
    CharType::Digit,
    CharType::Alpha,
    CharType::BigAlpha,
    CharType::Spec,
];

fn gen_random_char_types() -> [CharType; GEN_PASSWD_LEN] {
    let mut rng = rand::rng();

    let mut types: [CharType; GEN_PASSWD_LEN] = [CharType::Digit; GEN_PASSWD_LEN];
    types[..CHAR_TYPES.len()].copy_from_slice(&CHAR_TYPES);

    for tp in types[CHAR_TYPES.len()..GEN_PASSWD_LEN].iter_mut() {
        *tp = CHAR_TYPES[rng.random_range(..CHAR_TYPES.len())];
    }
    types.shuffle(&mut rng);
    types
}

/// Генерация случайного пароля.
pub fn gen_random_passwd() -> String {
    let mut rng = rand::rng();

    let mut res = String::with_capacity(GEN_PASSWD_LEN);
    for t in gen_random_char_types() {
        let chars = array_by_char_type(t);
        let c = chars[rng.random_range(..chars.len())];
        res.push(c as char);
    }

    res
}
