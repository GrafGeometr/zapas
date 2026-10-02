pub mod auth;

use crate::app::App;
use axum::Router;
use utoipa::OpenApi;

pub use auth::auth_middleware;

pub fn router(app: App) -> Router {
    let auth = Router::new().nest("/user", auth::router(app));

    auth
}

#[derive(OpenApi)]
#[openapi(nest((path = "/user", api = auth::AuthApi)))]
pub struct V1Api;
