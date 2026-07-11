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
        crate::web::v1::user::me,
        crate::web::v1::user::upd_passwd,
        crate::web::v1::user::forgot_passwd,
        crate::web::v1::user::verify_email,
        crate::web::v1::user::set_email,
        crate::web::v1::user::prolongation,
        crate::web::v1::room::make_room,
        crate::web::v1::room::room_profile,
        crate::web::v1::room::rooms,
    ),
    info(title = "Zapas API")
)]
struct ApiDoc;

pub fn make_app() -> Router {
    Router::new().merge(Scalar::with_url("/scalar", ApiDoc::openapi()))
}
