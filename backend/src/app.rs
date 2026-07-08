use sqlx::PgPool;

#[derive(Clone)]
pub struct App {
    pub pool: PgPool,
}
