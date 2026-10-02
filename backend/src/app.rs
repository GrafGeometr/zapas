//! Модуль с реализацией одноименной структуры.
use crate::err::GlobalErr;
use sqlx::PgPool;
use tracing::info;

/// Общее состояние программы.
#[derive(Clone)]
pub struct App {
    pub pool: PgPool,
}

impl App {
    /// Создание нового состояния.
    pub async fn new(db_url: &str) -> Result<Self, GlobalErr> {
        let res = Self {
            pool: sqlx::Pool::connect(db_url).await?,
        };
        res.migration().await?;

        info!("Create App State OK");
        Ok(res)
    }

    /// Создание нового состояния. Переменные берутся из окружения.
    pub async fn from_env() -> Result<Self, GlobalErr> {
        Self::new(&std::env::var("DATABASE_URL")?).await
    }

    /// Автоматическое применение миграций.
    async fn migration(&self) -> Result<(), GlobalErr> {
        info!("Start migration");
        sqlx::migrate!("./migrations").run(&self.pool).await?;
        info!("Finish migration");
        Ok(())
    }
}
