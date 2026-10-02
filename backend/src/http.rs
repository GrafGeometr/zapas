//! Модуль с описанием и реализацией http api.
mod v1;

use crate::{app::App, err::GlobalErr};
use axum::{Router, http::StatusCode, routing::get};
use tracing::info;
use utoipa::OpenApi;
use utoipa_scalar::{Scalar, Servable};

pub async fn server(app: App) -> Result<(), GlobalErr> {
    info!("Start http server");
    let listener = tokio::net::TcpListener::bind("127.0.0.1:9000").await?;
    axum::serve(listener, router(app)).await?;
    Ok(())
}

fn router(_app: App) -> Router {
    let scalar = Router::new().merge(Scalar::with_url("/scalar", ApiDoc::openapi()));

    let ping = Router::new()
        .route("/ping", get(ping))
        .route("/coffee", get(coffee));

    let v1 = Router::new();

    let api = Router::new().nest("/api", ping.merge(v1));

    Router::new().merge(scalar).merge(api)
}

#[derive(OpenApi)]
#[openapi(info(title = "Zapas API"), paths(ping, coffee))]
struct ApiDoc;

#[utoipa::path(get,
    tag = "ping",
    path = "/api/ping",
    responses(
        (status = 200, content_type = "text/plain")
    )
)]
async fn ping() -> &'static str {
    "pong"
}

#[utoipa::path(get,
    tag = "ping",
    path = "/api/coffee",
    responses(
        (status = 418)
    )
)]
async fn coffee() -> StatusCode {
    StatusCode::IM_A_TEAPOT
}
