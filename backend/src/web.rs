pub mod scalar;
pub mod v1;

use axum::Router;

pub async fn server(pool: sqlx::PgPool) -> Result<(), Box<dyn std::error::Error>> {
    let listener = tokio::net::TcpListener::bind("0.0.0.0:8000").await?;
    axum::serve(listener, make_app()).await?;

    Ok(())
}

fn make_app() -> Router {
    let v1 = v1::make_app();
    let scalar = scalar::make_app();

    Router::new().nest("/api", v1).merge(scalar)
}
