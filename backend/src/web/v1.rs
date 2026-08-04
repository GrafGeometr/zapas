//! API v1.
pub mod dto;
pub mod member;
pub mod middleware;
pub mod ping;
pub mod room;
pub mod user;

use crate::app::App;
use axum::Router;

/// Constructs the application router (api v1).
///
/// # Arguments
///
/// * `app` - The application state to be used by the server.
///
/// # Returns
///
/// A new router with all routes for api v1 registered.
pub fn make_app(app: App) -> Router {
    let ping = ping::make_app();
    let user = Router::new().nest("/user", user::make_app(app.clone()));
    let member = Router::new().nest("/member", member::make_app(app.clone()));
    let room = Router::new().nest("/room", room::make_app(app));

    Router::new().nest("/v1", ping.merge(user).merge(room).merge(member))
}
