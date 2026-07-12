pub mod dto;
pub mod member;
pub mod middlewares;
pub mod ping;
pub mod room;
pub mod user;

use crate::app::App;
use axum::Router;

pub fn make_app(app: App) -> Router {
    let ping = ping::make_app();
    let user = Router::new().nest("/user", user::make_app(app.clone()));
    let member = Router::new().nest("/member", member::make_app(app.clone()));
    let room = Router::new().nest("/room", room::make_app(app));

    Router::new().nest("/v1", ping.merge(user).merge(room).merge(member))
}
