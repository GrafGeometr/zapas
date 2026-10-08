use crate::{
    app::App,
    domain::auth::UserSession,
    err::{AppErr, AppErrJson},
    http::v1::auth_middleware,
};
use axum::{Extension, Json, Router, extract::State, middleware, routing::post};
use serde::Deserialize;
use utoipa::{OpenApi, ToSchema};
use validator::Validate;

pub fn router(app: App) -> Router {
    let without = Router::new().route("/forgot", post(forgot));
    let with = Router::new()
        .route("/me/email", post(upd_email))
        .route("/me/email/verify", post(verify))
        .layer(middleware::from_fn_with_state(app.clone(), auth_middleware));
    without.merge(with).with_state(app)
}

#[derive(OpenApi)]
#[openapi(paths(forgot, upd_email, verify))]
pub struct EmailApi;

#[derive(Deserialize, Validate, ToSchema)]
struct EmailDto {
    #[validate(email, length(max = 255))]
    email: String,
}

#[utoipa::path(post,
    tag = "email",
    path = "/forgot",
    description = "Send password reset link to email",
    responses(
        (status = 501, body = AppErrJson, description = "Not implemented"),
    )
)]
async fn forgot(State(_app): State<App>, Json(email): Json<EmailDto>) -> Result<(), AppErr> {
    email.validate()?;

    Err(AppErr::NotImpl)
}

#[utoipa::path(post,
    tag = "email",
    path = "/me/email",
    description = "Update email and send verification link (send not implemented)",
    responses(
        (status = 200),
        (status = 500, body = AppErrJson, description = "Internal server error"),
        (status = 400, body = AppErrJson, description = "Invalid any data format (validation error)"),
        (status = 401, body = AppErrJson, description = "Token is missing or not verified (maybe deleted or ip changed)"),
        (status = 403, body = AppErrJson, description = "Invalid or expired token"),
        (status = 404, body = AppErrJson, description = "User not found (what???)"),
    ),
    security(
        ("jwt" = [])
    )
)]
async fn upd_email(
    State(app): State<App>,
    Extension(user): Extension<UserSession>,
    Json(email): Json<EmailDto>,
) -> Result<(), AppErr> {
    email.validate()?;

    sqlx::query!(
        r#"
        update users set
            email = $2,
            verified_email = false
        where id = $1
        returning id"#,
        user.user,
        email.email
    )
    .fetch_optional(&app.pool)
    .await?
    .ok_or(AppErr::NotFound)?;

    {
        // TODO add send verify email
    }

    Ok(())
}

#[utoipa::path(post,
    tag = "email",
    path = "/me/email/verify",
    description = "Send verification link",
    responses(
        (status = 501, body = AppErrJson, description = "Not implemented"),
    )
)]
async fn verify(
    State(_app): State<App>,
    Extension(_user): Extension<UserSession>,
) -> Result<(), AppErr> {
    Err(AppErr::NotImpl)
}
