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
        crate::web::v1::user::upd_my_profile,
        crate::web::v1::user::logout_all,
        crate::web::v1::user::list_sessions,
        crate::web::v1::room::make_room,
        crate::web::v1::room::room_profile,
        crate::web::v1::room::rooms,
        crate::web::v1::room::upd_profile,
        crate::web::v1::room::set_room_timing,
        crate::web::v1::room::delete_room,
        crate::web::v1::room::all_members,
        crate::web::v1::room::gen_invite,
        crate::web::v1::room::invite,
        crate::web::v1::member::get_profile,
        crate::web::v1::member::get_origin_profile,
        crate::web::v1::member::delete,
        crate::web::v1::member::set_profile,
        crate::web::v1::member::get_def_portions,
        crate::web::v1::member::set_def_portions,
    ),
    info(title = "Zapas API")
)]
struct ApiDoc;

pub fn make_app() -> Router {
    Router::new().merge(Scalar::with_url("/scalar", ApiDoc::openapi()))
}
