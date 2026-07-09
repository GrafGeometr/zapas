use crate::repo::{email::EmailRepo, jwt::JwtRepo};
use sqlx::PgPool;

#[derive(Clone)]
pub struct App {
    pub pool: PgPool,
    pub email: EmailRepo,
    pub jwt: JwtRepo,
}
