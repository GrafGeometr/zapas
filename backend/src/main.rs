use sqlx::PgPool;
use tracing::{info, warn};

/// Глобальная ошибка всей программы. Происходит при конфигах и тд.
pub type GlobalErr = Box<dyn std::error::Error>;

#[tokio::main]
async fn main() -> Result<(), GlobalErr> {
    init_fmt();
    dotenv();

    let pool = connect_db().await?;
    migration(&pool).await?;

    Ok(())
}

/// Инициализация логгирования программы.
fn init_fmt() {
    println!(
        r#"
-----------------------------------------------------------

╺━┓┏━┓┏━┓┏━┓┏━┓
┏━┛┣━┫┣━┛┣━┫┗━┓
┗━╸╹ ╹╹  ╹ ╹┗━┛

version: {}

-----------------------------------------------------------

"#,
        env!("CARGO_PKG_VERSION")
    );

    tracing_subscriber::fmt()
        .with_target(true)
        .with_file(true)
        .with_line_number(true)
        .with_max_level(tracing::Level::TRACE)
        .pretty()
        .init();
    warn!("Session is coming");
}

/// Подключение к БД.
async fn connect_db() -> Result<PgPool, GlobalErr> {
    let db_url = std::env::var("DATABASE_URL")?;
    let pool = sqlx::Pool::connect(&db_url).await?;
    Ok(pool)
}

/// Миграция при запуске.
async fn migration(pool: &PgPool) -> Result<(), GlobalErr> {
    info!("Start migration");
    sqlx::migrate!("./migrations").run(pool).await?;
    info!("Finish migration");
    Ok(())
}

/// Загрузка переменных окружения из `.env` файла.
fn dotenv() {
    match dotenvy::dotenv() {
        Ok(_) => {
            info!("Import .env")
        }
        Err(err) => {
            warn!("Import .env failed: {}", err);
        }
    }
}
