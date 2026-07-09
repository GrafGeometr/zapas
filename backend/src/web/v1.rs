pub mod dto;
pub mod middlewares;
pub mod ping;
pub mod user;

use crate::app::App;
use axum::Router;

pub fn make_app(app: App) -> Router {
    let ping = ping::make_app();
    let user = Router::new().nest("/user", user::make_app(app));

    Router::new().nest("/v1", ping.merge(user))
}
