//! The module contains a single structure, the App.
use crate::repo::{email::EmailRepo, jwt::JwtRepo};
use sqlx::PgPool;

// Shared state of the program.
#[derive(Clone)]
pub struct App {
    pub pool: PgPool,
    pub email: EmailRepo,
    pub jwt: JwtRepo,
}
