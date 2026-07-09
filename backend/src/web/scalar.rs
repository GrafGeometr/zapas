use axum::Router;
use utoipa::OpenApi;
use utoipa_scalar::{Scalar, Servable};

#[derive(OpenApi)]
#[openapi(
    paths(
        crate::web::v1::ping::ping,
        crate::web::v1::ping::coffee,
        crate::web::v1::user::register,
        crate::web::v1::user::delete,
        crate::web::v1::user::login,
    ),
    info(title = "Zapas API")
)]
struct ApiDoc;

pub fn make_app() -> Router {
    Router::new().merge(Scalar::with_url("/scalar", ApiDoc::openapi()))
}
