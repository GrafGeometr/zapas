mod auth;
mod room;
mod sse;
mod user;

use crate::app::App;
use axum::Router;
use serde::{Deserialize, Serialize};
use utoipa::{OpenApi, ToSchema};
use uuid::Uuid;

pub fn router(app: App) -> Router {
    let auth = Router::new().nest("/user", auth::router(app.clone()));
    let user = Router::new().nest("/user", user::router(app.clone()));
    let room = Router::new().nest("/room", room::router(app.clone()));
    let sse = Router::new().nest("/sse", sse::router(app.clone()));

    auth.merge(user).merge(room).merge(sse)
}

#[derive(OpenApi)]
#[openapi(
    nest(
        (path = "/user", api = auth::AuthApi),
        (path = "/user", api = user::UserApi),
        (path = "/room", api = room::RoomApi),
        (path = "/sse", api = sse::SseApi),
    )
)]
pub struct V1Api;

pub use auth::auth_middleware;
use validator::Validate;

#[derive(Serialize, Deserialize, ToSchema)]
struct UuidDto {
    id: Uuid,
}

#[derive(Serialize, Deserialize, Validate, ToSchema)]
struct ProfileDto {
    #[validate(length(min = 1, max = 127))]
    display_name: String,

    #[validate(length(max = 1024))]
    bio: String,
}

#[derive(Deserialize, Validate, ToSchema)]
struct UpdProfileDto {
    #[validate(length(min = 1, max = 127))]
    display_name: Option<String>,

    #[validate(length(max = 1024))]
    bio: Option<String>,
}

#[derive(Serialize, Deserialize, Validate, ToSchema)]
struct Timing {
    #[validate(range(min = 0))]
    review_interval: Option<i64>,

    #[validate(range(min = 0))]
    expired_notify: Option<i64>,
}
