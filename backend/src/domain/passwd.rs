//! Работа с паролями.
use crate::err::AppErr;
use argon2::{Argon2, PasswordHasher, PasswordVerifier};
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
