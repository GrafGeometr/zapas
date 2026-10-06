use super::auth_middleware;
use crate::{
    app::App,
    domain::{
        auth::UserSession,
        passwd::{check_passwd, gen_passwd_hash},
    },
    err::{AppErr, AppErrJson},
    http::v1::UuidDto,
};
use axum::{
    Extension, Json, Router,
    extract::{Path, State},
    middleware,
    routing::{delete, get, post},
};
use chrono::{DateTime, Utc};
use serde::{Deserialize, Serialize};
use utoipa::{OpenApi, ToSchema};
use uuid::Uuid;
use validator::{Validate, ValidationError};

pub fn router(app: App) -> Router {
    let without_middleware = Router::new().route("/register", post(register));

    let with = Router::new()
        .route("/delete", delete(delete_user))
        .route("/me", get(me))
        .route("/by_id/{id}", get(other_user_by_id))
        .route("/by_username/{username}", get(other_user_by_username))
        .route("/me/passwd", post(change_passwd))
        .route("/me/profile", post(upd_profile))
        .layer(middleware::from_fn_with_state(app.clone(), auth_middleware));

    without_middleware.merge(with).with_state(app)
}

#[derive(OpenApi)]
#[openapi(paths(
    register,
    delete_user,
    me,
    other_user_by_id,
    other_user_by_username,
    change_passwd,
    upd_profile
))]
pub struct UserApi;

#[derive(Deserialize, Validate, ToSchema)]
struct RegisterDto {
    #[schema(example = "bob_ross")]
    #[validate(length(min = 4, max = 63), custom(function = validate_username))]
    username: String,

    #[schema(example = "bob.ross@example.com")]
    #[validate(email, length(max = 255))]
    email: Option<String>,

    #[schema(example = "super_password!67")]
    #[validate(length(min = 12, max = 127), custom(function = validate_passwd))]
    passwd: String,

    #[schema(example = "bob ross")]
    #[validate(length(min = 1, max = 127))]
    display_name: String,

    #[schema(example = "")]
    #[validate(length(max = 1024))]
    bio: String,
}

#[derive(Serialize, ToSchema)]
struct UserMe {
    id: Uuid,
    username: String,

    email: Option<String>,
    verified_email: bool,

    display_name: String,
    bio: String,

    created_at: DateTime<Utc>,
}

#[derive(Serialize, ToSchema)]
struct UserOther {
    id: Uuid,
    username: String,

    display_name: String,
    bio: String,

    created_at: DateTime<Utc>,
}

#[derive(Deserialize, Validate, ToSchema)]
struct UserNamePath {
    #[validate(length(min = 4, max = 63), custom(function = validate_username))]
    username: String,
}

#[derive(Deserialize, Validate, ToSchema)]
#[validate(schema(function = validate_change_passwd))]
struct ChangePasswd {
    #[schema(example = "super_password!67")]
    #[validate(length(min = 12, max = 127))]
    old_passwd: String,

    #[schema(example = "super_puper_password!67")]
    #[validate(length(min = 12, max = 127), custom(function = validate_passwd))]
    passwd: String,
}

#[derive(Deserialize, Validate, ToSchema)]
struct UpdUserProfileDto {
    #[validate(length(min = 4, max = 63), custom(function = validate_username))]
    username: Option<String>,

    #[validate(length(min = 1, max = 127))]
    display_name: Option<String>,

    #[validate(length(max = 1024))]
    bio: Option<String>,
}

fn validate_username(username: &str) -> Result<(), ValidationError> {
    if username
        .chars()
        .all(|c| c.is_ascii_lowercase() || c.is_ascii_digit() || c == '_')
    {
        Ok(())
    } else {
        Err(ValidationError::new("invalid username chars"))
    }
}

fn validate_passwd(passwd: &str) -> Result<(), ValidationError> {
    const SPECIAL_CHARS: &str = "!@#$%^&*()_-+=?/.,><;:";

    if !passwd.chars().any(|c| c.is_ascii_alphabetic()) {
        Err(ValidationError::new("not found alphabetic is passwd"))
    } else if !passwd.chars().any(|c| c.is_ascii_digit()) {
        Err(ValidationError::new("not found digit in passwd"))
    } else if !passwd.chars().any(|c| c.is_ascii_lowercase()) {
        Err(ValidationError::new("not found lowercase in passwd"))
    } else if !passwd.chars().any(|c| c.is_ascii_uppercase()) {
        Err(ValidationError::new("not found uppercase in passwd"))
    } else if !passwd.chars().any(|c| SPECIAL_CHARS.contains(c)) {
        Err(ValidationError::new("not found special chars in passwd"))
    } else {
        Ok(())
    }
}

fn validate_change_passwd(cp: &ChangePasswd) -> Result<(), ValidationError> {
    if cp.old_passwd == cp.passwd {
        return Err(ValidationError::new("same passwords"));
    }
    Ok(())
}

#[utoipa::path(post,
    tag = "user",
    path = "/register",
    description = "Register new user",
    request_body = RegisterDto,
    responses(
        (status = 200, body = UuidDto),
        (status = 500, body = AppErrJson, description = "Internal server error"),
        (status = 400, body = AppErrJson, description = "Invalid any data format (validation error)"),
        (status = 409, body = AppErrJson, description = "Already exists username or email")
    )
)]
async fn register(
    State(app): State<App>,
    Json(reg): Json<RegisterDto>,
) -> Result<Json<UuidDto>, AppErr> {
    reg.validate()?;

    let id = Uuid::now_v7();
    let passwd_hash = gen_passwd_hash(&reg.passwd)?;

    sqlx::query!(
        r#"
        insert into users (
            id,
            username,
            email,
            display_name,
            bio,
            passwd_hash
        ) values (
            $1, $2, $3, $4, $5, $6
        )"#,
        id,
        reg.username,
        reg.email,
        reg.display_name,
        reg.bio,
        passwd_hash
    )
    .execute(&app.pool)
    .await
    .map_err(|err| {
        if let Some(e) = err.as_database_error() {
            if e.message().contains("users_username_unique") {
                AppErr::AlreadyExists("username")
            } else if e.message().contains("users_email_unique") {
                AppErr::AlreadyExists("email")
            } else {
                err.into()
            }
        } else {
            err.into()
        }
    })?;

    Ok(Json(UuidDto { id }))
}

#[utoipa::path(delete,
    tag = "user",
    path = "/delete",
    description = "Delete user (not implemented)",
    responses(
        (status = 500, body = AppErrJson, description = "Internal server error"),
        (status = 501, body = AppErrJson, description = "Not implemented"),
    ),
    security(
        ("jwt" = [])
    )
)]
async fn delete_user(
    State(_app): State<App>,
    Extension(_user): Extension<UserSession>,
) -> Result<(), AppErr> {
    Err(AppErr::NotImpl)
}

#[utoipa::path(get,
    tag = "user",
    path = "/me",
    description = "All data about yourself",
    responses(
        (status = 200, body = UserMe),
        (status = 500, body = AppErrJson, description = "Internal server error"),
        (status = 401, body = AppErrJson, description = "Token is missing or not verified (maybe deleted or ip changed)"),
        (status = 403, body = AppErrJson, description = "Invalid or expired token"),
        (status = 404, body = AppErrJson, description = "User not found (what?)")
    ),
    security(
        ("jwt" = [])
    )
)]
async fn me(
    State(app): State<App>,
    Extension(user): Extension<UserSession>,
) -> Result<Json<UserMe>, AppErr> {
    let res = sqlx::query_as!(
        UserMe,
        r#"
        select
            id,
            username,
            email,
            verified_email,
            display_name,
            bio,
            created_at
        from users where id = $1"#,
        user.user
    )
    .fetch_optional(&app.pool)
    .await?
    .ok_or(AppErr::NotFound)?;

    Ok(Json(res))
}

#[utoipa::path(get,
    tag = "user",
    path = "/by_id/{id}",
    description = "Data about other user",
    responses(
        (status = 200, body = UserOther),
        (status = 500, body = AppErrJson, description = "Internal server error"),
        (status = 401, body = AppErrJson, description = "Token is missing or not verified (maybe deleted or ip changed)"),
        (status = 403, body = AppErrJson, description = "Invalid or expired token"),
        (status = 404, body = AppErrJson, description = "User not found")
    ),
    security(
        ("jwt" = [])
    )
)]
async fn other_user_by_id(
    State(app): State<App>,
    Extension(_user): Extension<UserSession>,
    Path(UuidDto { id }): Path<UuidDto>,
) -> Result<Json<UserOther>, AppErr> {
    let res = sqlx::query_as!(
        UserOther,
        r#"
        select
            id,
            username,
            display_name,
            bio,
            created_at
        from users where id = $1"#,
        id
    )
    .fetch_optional(&app.pool)
    .await?
    .ok_or(AppErr::NotFound)?;

    Ok(Json(res))
}

#[utoipa::path(get,
    tag = "user",
    path = "/by_username/{username}",
    description = "Data about other user",
    responses(
        (status = 200, body = UserOther),
        (status = 500, body = AppErrJson, description = "Internal server error"),
        (status = 400, body = AppErrJson, description = "Invalid any data format (validation error)"),
        (status = 401, body = AppErrJson, description = "Token is missing or not verified (maybe deleted or ip changed)"),
        (status = 403, body = AppErrJson, description = "Invalid or expired token"),
        (status = 404, body = AppErrJson, description = "User not found")
    ),
    security(
        ("jwt" = [])
    )
)]
async fn other_user_by_username(
    State(app): State<App>,
    Extension(_user): Extension<UserSession>,
    Path(username): Path<String>,
) -> Result<Json<UserOther>, AppErr> {
    let path = UserNamePath { username };
    path.validate()?;
    let username = &path.username;

    let res = sqlx::query_as!(
        UserOther,
        r#"
        select
            id,
            username,
            display_name,
            bio,
            created_at
        from users where username = $1"#,
        username
    )
    .fetch_optional(&app.pool)
    .await?
    .ok_or(AppErr::NotFound)?;

    Ok(Json(res))
}

#[utoipa::path(post,
    tag = "user",
    path = "/me/passwd",
    description = "Change password and logout all",
    request_body = ChangePasswd,
    responses(
        (status = 200),
        (status = 500, body = AppErrJson, description = "Internal server error"),
        (status = 400, body = AppErrJson, description = "Invalid any data format (validation error)"),
        (status = 401, body = AppErrJson, description = "Token is missing or not verified (maybe deleted or ip changed)"),
        (status = 403, body = AppErrJson, description = "Invalid or expired token"),
        (status = 404, body = AppErrJson, description = "User not found (what?)"),
    ),
    security(
        ("jwt" = [])
    )
)]
async fn change_passwd(
    State(app): State<App>,
    Extension(user): Extension<UserSession>,
    Json(cp): Json<ChangePasswd>,
) -> Result<(), AppErr> {
    cp.validate()?;
    let new_hash = gen_passwd_hash(&cp.passwd)?;

    let mut tx = app.pool.begin().await?;

    let res = sqlx::query!(
        r#"
        select
            passwd_hash
        from users
        where id = $1
        for update"#,
        user.user
    )
    .fetch_optional(&mut *tx)
    .await?
    .ok_or(AppErr::NotFound)?;
    check_passwd(&res.passwd_hash, &cp.old_passwd)?;

    sqlx::query!(
        r#"
        update users set
            passwd_hash = $2
        where id = $1"#,
        user.user,
        new_hash,
    )
    .execute(&mut *tx)
    .await?;

    sqlx::query!("delete from sessions where user_id = $1", user.user)
        .execute(&mut *tx)
        .await?;

    tx.commit().await?;

    Ok(())
}

#[utoipa::path(post,
    tag = "user",
    path = "/me/profile",
    description = "Change profile (username, display_name, bio)",
    request_body = UpdUserProfileDto,
    responses(
        (status = 200, body = UserMe),
        (status = 500, body = AppErrJson, description = "Internal server error"),
        (status = 400, body = AppErrJson, description = "Invalid any data format (validation error)"),
        (status = 401, body = AppErrJson, description = "Token is missing or not verified (maybe deleted or ip changed)"),
        (status = 403, body = AppErrJson, description = "Invalid or expired token"),
        (status = 404, body = AppErrJson, description = "User not found (what?)"),
        (status = 409, body = AppErrJson, description = "Already exists username")
    ),
    security(
        ("jwt" = [])
    )
)]
async fn upd_profile(
    State(app): State<App>,
    Extension(user): Extension<UserSession>,
    Json(upd): Json<UpdUserProfileDto>,
) -> Result<Json<UserMe>, AppErr> {
    upd.validate()?;

    let res = sqlx::query_as!(
        UserMe,
        r#"
        update users set
            username = coalesce($2, username),
            display_name = coalesce($3, display_name),
            bio = coalesce($4, bio)
        where id = $1
        returning
            id,
            username,
            email,
            verified_email,
            display_name,
            bio,
            created_at"#,
        user.user,
        upd.username,
        upd.display_name,
        upd.bio
    )
    .fetch_optional(&app.pool)
    .await
    .map_err(|err| {
        if let Some(e) = err.as_database_error() {
            if e.message().contains("users_username_unique") {
                AppErr::AlreadyExists("username")
            } else if e.message().contains("users_email_unique") {
                AppErr::AlreadyExists("email")
            } else {
                err.into()
            }
        } else {
            err.into()
        }
    })?
    .ok_or(AppErr::NotFound)?;

    Ok(Json(res))
}
