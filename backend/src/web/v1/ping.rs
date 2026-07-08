use axum::{Router, http::StatusCode, routing::get};

pub fn make_app() -> Router {
    Router::new()
        .route("/ping", get(ping))
        .route("/coffee", get(coffee))
}

#[utoipa::path(get, path = "/api/v1/ping", responses((status = 200, content_type = "text/plain")))]
pub async fn ping() -> &'static str {
    "pong"
}

#[utoipa::path(get, path = "/api/v1/coffee", responses((status = 418)))]
pub async fn coffee() -> StatusCode {
    StatusCode::IM_A_TEAPOT
}
