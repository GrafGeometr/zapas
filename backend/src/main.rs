//! A Rust application.
pub mod app;
pub mod domain;
pub mod err;
pub mod repo;
pub mod web;

use crate::{
    app::App,
    repo::{email::EmailRepo, jwt::JwtRepo},
};
use sqlx::PgPool;
use tracing::{info, warn};

/// The main function that runs the application.
#[tokio::main]
async fn main() -> Result<(), Box<dyn std::error::Error>> {
    fmt();
    dotenv();
    let pool = connect_db().await?;
    migration(&pool).await?;

    let app = App {
        pool,
        email: EmailRepo::new(),
        jwt: JwtRepo::from_env()?,
    };
    web::server(app).await?;

    Ok(())
}

/// Loads environment variables from a `.env` file.
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

/// Initializes tracing with specific configurations.
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

/// Establishes a connection to the PostgreSQL database.
async fn connect_db() -> Result<PgPool, Box<dyn std::error::Error>> {
    let db_url = std::env::var("DATABASE_URL")?;
    let pool = sqlx::PgPool::connect(&db_url).await?;
    Ok(pool)
}

/// Runs migrations for the PostgreSQL database.
async fn migration(pool: &PgPool) -> Result<(), Box<dyn std::error::Error>> {
    info!("Start migration...");
    sqlx::migrate!("./migrations").run(pool).await?;
    info!("Finish migration!");
    Ok(())
}
