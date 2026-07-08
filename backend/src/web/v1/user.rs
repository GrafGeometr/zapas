use crate::{app::App, err::AppErr};
use argon2::{
    Argon2, PasswordHasher,
    password_hash::{SaltString, rand_core},
};
use axum::{Json, Router, extract::State, routing::post};
use serde::{Deserialize, Serialize};
use tracing::{debug, error};
use utoipa::ToSchema;
use uuid::Uuid;
use validator::Validate;

pub fn make_app(app: App) -> Router {
    Router::new()
        .route("/register", post(register))
        .route("/delete", post(delete))
        .with_state(app)
}

#[derive(Deserialize, Validate, ToSchema)]
pub struct RegisterDto {
    #[validate(length(min = 4, max = 63))]
    #[schema(example = "bob_ross")]
    pub username: String,

    #[validate(email)]
    #[schema(example = "bob.ross@example.com")]
    pub email: Option<String>,

    #[validate(length(min = 1, max = 127))]
    #[schema(example = "bob ross")]
    pub display_name: String,

    #[validate(length(max = 1023))]
    #[schema(example = "")]
    pub bio: String,

    #[validate(length(min = 12, max = 255))]
    #[schema(example = "super_password!67")]
    pub passwd: String,
}

#[derive(Serialize, Deserialize, ToSchema)]
pub struct UuidDto {
    pub id: Uuid,
}

#[utoipa::path(post,
    path = "/api/v1/user/register",
    request_body = RegisterDto,
    responses(
        (status = 200, body = UuidDto),
    )
)]
pub async fn register(
    State(app): State<App>,
    Json(reg): Json<RegisterDto>,
) -> Result<Json<UuidDto>, AppErr> {
    reg.validate()?;
    let passwd = gen_passwd_hash(&reg.passwd)?;
    let user_id = Uuid::now_v7();

    sqlx::query!(
        "insert into users (id, username, email, display_name, bio, passwd_hash) values ($1, $2, $3, $4, $5, $6)",
        user_id,
        reg.username,
        reg.email,
        reg.display_name,
        reg.bio,
        passwd
    ).execute(&app.pool).await.map_err(|e| {
        if let Some(e) = e.as_database_error() {
            if e.message().contains("users_username_unique") {
                AppErr::UserNameAlreadyExists
            } else if e.message().contains("users_email_unique") {
                AppErr::EmailAlreadyExists
            }else {
                AppErr::IntrSrvErr
            }
        } else {
            AppErr::IntrSrvErr
        }
    })?;

    debug!("Register user: {}, {}", reg.username, user_id);
    Ok(Json(UuidDto { id: user_id }))
}

pub async fn delete(State(_): State<App>, Json(_): Json<UuidDto>) -> Result<(), AppErr> {
    Err(AppErr::NotImplemented)
}

fn gen_passwd_hash(passwd: &str) -> Result<String, AppErr> {
    let salt = SaltString::generate(&mut rand_core::OsRng);
    let argon2 = Argon2::default();

    argon2
        .hash_password(passwd.as_bytes(), &salt)
        .map_err(|e| {
            error!("Error argon2: {}", e);
            AppErr::IntrSrvErr
        })
        .map(|hash| hash.to_string())
}
