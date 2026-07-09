pub mod app;
pub mod domain;
pub mod err;
pub mod repo;
pub mod web;

use crate::{app::App, repo::email::EmailRepo};
use sqlx::PgPool;
use tracing::{info, warn};

#[tokio::main]
async fn main() -> Result<(), Box<dyn std::error::Error>> {
    fmt();
    dotenv();
    let pool = connect_db().await?;
    migration(&pool).await?;

    let app = App {
        pool,
        email: EmailRepo::new(),
    };
    web::server(app).await?;

    Ok(())
}

fn dotenv() {
    match dotenvy::dotenv() {
        Ok(_) => {
            info!("Import .env");
        }
        Err(err) => {
            warn!(".env file read error: {}", err);
        }
    }
}

fn fmt() {
    println!("Hello, world!");

    tracing_subscriber::fmt()
        .with_target(true)
        .with_file(true)
        .with_line_number(true)
        .with_max_level(tracing::Level::TRACE)
        .pretty()
        .init();
    warn!("Session is coming");
}

async fn connect_db() -> Result<PgPool, Box<dyn std::error::Error>> {
    let db_url = std::env::var("DATABASE_URL")?;
    let pool = sqlx::PgPool::connect(&db_url).await?;
    Ok(pool)
}

async fn migration(pool: &PgPool) -> Result<(), Box<dyn std::error::Error>> {
    info!("Start migration...");
    sqlx::migrate!("./migrations").run(pool).await?;
    info!("Finish migration!");
    Ok(())
}
