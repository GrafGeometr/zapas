pub mod scalar;
pub mod v1;

use crate::app::App;
use axum::Router;

pub async fn server(app: App) -> Result<(), Box<dyn std::error::Error>> {
    let listener = tokio::net::TcpListener::bind("0.0.0.0:8000").await?;
    axum::serve(listener, make_app(app)).await?;

    Ok(())
}

fn make_app(app: App) -> Router {
    let v1 = v1::make_app(app);
    let scalar = scalar::make_app();

    Router::new().nest("/api", v1).merge(scalar)
}
