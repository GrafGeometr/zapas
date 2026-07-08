use tracing::{info, warn};

#[tokio::main]
async fn main() -> Result<(), Box<dyn std::error::Error>> {
    println!("Hello, world!");

    tracing_subscriber::fmt()
        .with_target(true)
        .with_file(true)
        .with_line_number(true)
        .with_max_level(tracing::Level::INFO)
        .pretty()
        .init();
    warn!("Session is coming");

    match dotenvy::dotenv() {
        Ok(_) => {
            info!("Import .env");
        }
        Err(err) => {
            warn!(".env not found");
        }
    }

    let db_url = std::env::var("DATABASE_URL")?;
    let pool = sqlx::PgPool::connect(&db_url).await?;

    info!("Start migration...");
    sqlx::migrate!("./migrations").run(&pool).await?;
    info!("Finish migration!");

    Ok(())
}
