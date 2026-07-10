use super::{dto::*, middlewares::*};
use crate::{
    app::App,
    domain::token::{AuthToken, ProlongRes, Session},
    err::{AppErr, ToAppErr},
};
use argon2::{
    Argon2, PasswordHash, PasswordHasher, PasswordVerifier,
    password_hash::{SaltString, rand_core},
};
use axum::{
    Extension, Json, Router,
    extract::State,
    middleware,
    routing::{get, post},
};
use chrono::Utc;
use tracing::{debug, error, warn};
use uuid::Uuid;
use validator::Validate;

pub fn make_app(app: App) -> Router {
    let login_router = Router::new()
        .route("/login", post(login))
        .route("/forgot", post(forgot_passwd))
        .layer(middleware::from_fn(login_middleware));

    let prolong_router =
        Router::new()
            .route("/prolong", post(prolongation))
            .layer(middleware::from_fn_with_state(
                app.clone(),
                prolongation_middleware,
            ));

    let with_auth = Router::new()
        .route("/me", get(me))
        .route("/me/profile", post(upd_my_profile))
        .route("/me/passwd", post(upd_passwd))
        .route("/me/email", post(set_email))
        .route("/me/email/verify", post(verify_email))
        .route("/me/sessions", get(list_sessions))
        .route("/me/logout", post(logout_all))
        .layer(middleware::from_fn_with_state(app.clone(), auth_middleware));

    Router::new()
        .route("/register", post(register))
        .route("/delete", post(delete))
        .merge(login_router)
        .merge(prolong_router)
        .merge(with_auth)
        .with_state(app)
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

    if let Some(email) = &reg.email {
        let _ = app
            .email
            .send_verify(email)
            .await
            .map_err(|e| error!("Error send msg email {}, err: {} --- {:?}", email, e, e));
    }

    debug!("Register user: {}, {}", reg.username, user_id);
    Ok(Json(UuidDto { id: user_id }))
}

#[utoipa::path(post,
    path = "/api/v1/user/delete",
    request_body = UuidDto,
    responses(
        (status = 200),
    )
)]
pub async fn delete(State(_app): State<App>, Json(_user): Json<UuidDto>) -> Result<(), AppErr> {
    Err(AppErr::NotImplemented)
}

#[utoipa::path(post,
    path = "/api/v1/user/login",
    request_body = LoginDto,
    responses(
        (status = 200, body = TokenDto),
    )
)]
pub async fn login(
    State(app): State<App>,
    Extension(ip): Extension<String>,
    Json(login): Json<LoginDto>,
) -> Result<Json<TokenDto>, AppErr> {
    login.validate()?;

    let user = sqlx::query!(
        "select id, passwd_hash from users where username = $1",
        login.username
    )
    .fetch_optional(&app.pool)
    .await
    .app_log()?
    .ok_or(AppErr::WrongUserOrPasswd)?;
    check_passwd_hash(&user.passwd_hash, &login.passwd)?;

    let session = Session::new(user.id, ip);

    sqlx::query!(
        "insert into sessions (id, user_id, token_id, last_token_id, ip_addr) values ($1, $2, $3, $4, $5)",
        session.id,
        session.user,
        session.token,
        session.last_token,
        session.ip
    ).execute(&app.pool).await.app_log()?;

    let token = app.jwt.gen_jwt_auth(session)?;
    Ok(Json(TokenDto { token }))
}

#[utoipa::path(get,
    path = "/api/v1/user/me",
    responses(
        (status=200, body = UserMeDto)
    ),
    security(
        ("jwt" = [])
    )
)]
pub async fn me(
    State(app): State<App>,
    Extension(user_id): Extension<Uuid>,
) -> Result<Json<UserMeDto>, AppErr> {
    let user = sqlx::query_as!(UserMeDto,
        "select id, username, email, verified_email, display_name, bio, created_at from users where id = $1",
        user_id
    ).fetch_one(&app.pool).await.app_log()?;

    Ok(Json(user))
}

pub async fn upd_my_profile(
    State(app): State<App>,
    Extension(user_id): Extension<Uuid>,
    Json(new_profile): Json<MyProfileDto>,
) -> Result<Json<UserMeDto>, AppErr> {
    new_profile.validate()?;

    let user = sqlx::query_as!(
        UserMeDto,
        r#"
        update users set
            username = coalesce($1, username),
            display_name = coalesce($2, display_name),
            bio = coalesce($3, bio)
        where id = $4
        returning id, username, email, verified_email, display_name, bio, created_at"#,
        new_profile.username,
        new_profile.display_name,
        new_profile.bio,
        user_id
    )
    .fetch_one(&app.pool)
    .await
    .app_log()?;

    Ok(Json(user))
}

pub async fn list_sessions(
    State(app): State<App>,
    Extension(user_id): Extension<Uuid>,
) -> Result<Json<Vec<SessionDto>>, AppErr> {
    let sessions = sqlx::query_as!(
        SessionDto,
        "select id, ip_addr, last_active_at, created_at from sessions where user_id = $1",
        user_id
    )
    .fetch_all(&app.pool)
    .await
    .app_log()?;

    Ok(Json(sessions))
}

pub async fn logout_all(
    State(app): State<App>,
    Extension(user_id): Extension<Uuid>,
) -> Result<(), AppErr> {
    sqlx::query!("delete from sessions where user_id = $1", user_id)
        .execute(&app.pool)
        .await
        .app_log()?;

    Ok(())
}

pub async fn upd_passwd(
    State(app): State<App>,
    Extension(user_id): Extension<Uuid>,
    Json(passwd): Json<PasswdDto>,
) -> Result<(), AppErr> {
    let hash = sqlx::query!("select passwd_hash from users where id = $1", user_id)
        .fetch_one(&app.pool)
        .await
        .app_log()?;
    check_passwd_hash(&hash.passwd_hash, &passwd.old_passwd)?;

    let new_hash = gen_passwd_hash(&passwd.passwd)?;
    sqlx::query!(
        "update users set passwd_hash = $1 where id = $2",
        new_hash,
        user_id
    )
    .execute(&app.pool)
    .await
    .app_log()?;

    Ok(())
}

pub async fn forgot_passwd(
    State(_app): State<App>,
    Extension(_ip): Extension<String>,
    Json(email): Json<EmailDto>,
) -> Result<(), AppErr> {
    email.validate()?;

    Err(AppErr::NotImplemented) // TODO
}

pub async fn verify_email(
    State(_app): State<App>,
    Extension(_user_id): Extension<Uuid>,
) -> Result<(), AppErr> {
    Err(AppErr::NotImplemented) // TODO
}

pub async fn set_email(
    State(_app): State<App>,
    Extension(_user_id): Extension<Uuid>,
    Json(email): Json<EmailDto>,
) -> Result<(), AppErr> {
    email.validate()?;

    Err(AppErr::NotImplemented)
}

pub async fn prolongation(
    State(app): State<App>,
    Extension(auth): Extension<AuthToken>,
) -> Result<Json<TokenDto>, AppErr> {
    let session_row = sqlx::query!(
        "select id, user_id, token_id, last_token_id, ip_addr from sessions where id = $1",
        auth.id
    )
    .fetch_optional(&app.pool)
    .await
    .app_log()?
    .ok_or(AppErr::Unauthorized)?;

    let session = Session {
        id: session_row.id,
        user: session_row.user_id,
        token: session_row.token_id,
        last_token: session_row.last_token_id,
        ip: session_row.ip_addr,
        last_active_at: Utc::now(),
    };

    match session.prolongation(auth) {
        ProlongRes::Ok(session) => {
            sqlx::query!(
                "update sessions set token_id = $1, last_token_id = $2, ip_addr = $3, last_active_at = $4",
                session.token,
                session.last_token,
                session.ip,
                session.last_active_at
            ).execute(&app.pool).await.app_log()?;

            let token = app.jwt.gen_jwt_auth(session)?;
            Ok(Json(TokenDto { token }))
        }
        ProlongRes::Delete(uuid) => {
            let _ = sqlx::query!("delete from sessions where id = $1", uuid)
                .execute(&app.pool)
                .await
                .app_log();

            Err(AppErr::Unauthorized)
        }
    }
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

fn check_passwd_hash(passwd_hash: &str, passwd: &str) -> Result<(), AppErr> {
    let hash = PasswordHash::new(passwd_hash).app_log()?;
    let argon2 = Argon2::default();

    argon2
        .verify_password(passwd.as_bytes(), &hash)
        .map_err(|e| {
            warn!("Check passwd err: {}", e);
            AppErr::WrongUserOrPasswd
        })?;

    Ok(())
}
