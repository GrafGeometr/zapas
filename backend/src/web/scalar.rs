//! Scalar route.
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
        crate::web::v1::room::make_storage,
        crate::web::v1::room::all_storages,
        crate::web::v1::room::all_items,
        crate::web::v1::room::new_transaction,
        crate::web::v1::room::new_credit,
        crate::web::v1::room::all_transactions,
        crate::web::v1::member::get_profile,
        crate::web::v1::member::get_origin_profile,
        crate::web::v1::member::delete,
        crate::web::v1::member::set_profile,
        crate::web::v1::member::get_def_portions,
        crate::web::v1::member::set_def_portions,
        crate::web::v1::member::set_portion,
        crate::web::v1::member::portion_calendar,
        crate::web::v1::member::all_transactions_for_member,
        crate::web::v1::storage::storage_profile,
        crate::web::v1::storage::upd_profile,
        crate::web::v1::storage::set_storage_timing,
        crate::web::v1::storage::delete_storage,
        crate::web::v1::storage::make_item,
        crate::web::v1::storage::all_items,
        crate::web::v1::item::get_profile,
        crate::web::v1::item::upd_profile,
        crate::web::v1::item::set_timing,
        crate::web::v1::item::need_review,
        crate::web::v1::item::review,
        crate::web::v1::item::delete_item,
        crate::web::v1::transaction::get_transaction,
        crate::web::v1::transaction::delete_transaction,
    ),
    info(title = "Zapas API")
)]
struct ApiDoc;

// Scalar route.
pub fn make_app() -> Router {
    Router::new().merge(Scalar::with_url("/scalar", ApiDoc::openapi()))
}
