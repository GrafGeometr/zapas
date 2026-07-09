use crate::repo::email::EmailRepo;
use sqlx::PgPool;

#[derive(Clone)]
pub struct App {
    pub pool: PgPool,
    pub email: EmailRepo,
}
