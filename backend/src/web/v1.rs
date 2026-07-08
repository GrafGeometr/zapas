use axum::Router;

pub mod ping;

pub fn make_app() -> Router {
    let ping = ping::make_app();

    Router::new().nest("/v1", ping)
}
