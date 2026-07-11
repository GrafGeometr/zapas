//! Provides routes for different API versions and utilities.
pub mod scalar;
pub mod v1;

use crate::app::App;
use axum::Router;

/// Starts the server with the provided application state.
///
/// # Arguments
///
/// * `app` - The application state to be used by the server.
///
/// # Errors
///
/// Returns an error if binding to the socket fails or starting the server fails.
pub async fn server(app: App) -> Result<(), Box<dyn std::error::Error>> {
    let listener = tokio::net::TcpListener::bind("0.0.0.0:8000").await?;
    axum::serve(listener, make_app(app)).await?;

    Ok(())
}

/// Constructs the application router.
///
/// # Arguments
///
/// * `app` - The application state to be used by the server.
///
/// # Returns
///
/// A new router with all routes registered.
fn make_app(app: App) -> Router {
    let v1 = v1::make_app(app);
    let scalar = scalar::make_app();

    Router::new().nest("/api", v1).merge(scalar)
}
