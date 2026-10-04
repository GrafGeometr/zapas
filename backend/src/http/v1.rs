mod auth;
mod user;

use crate::app::App;
use axum::Router;
use serde::{Deserialize, Serialize};
use utoipa::{OpenApi, ToSchema};
use uuid::Uuid;

pub fn router(app: App) -> Router {
    let auth = Router::new().nest("/user", auth::router(app.clone()));
    let user = Router::new().nest("/user", user::router(app));

    auth.merge(user)
}

#[derive(OpenApi)]
#[openapi(
    nest(
        (path = "/user", api = auth::AuthApi),
        (path = "/user", api = user::UserApi)
    )
)]
pub struct V1Api;

pub use auth::auth_middleware;

#[derive(Serialize, Deserialize, ToSchema)]
struct UuidDto {
    id: Uuid,
}
