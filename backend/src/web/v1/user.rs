use super::{dto::*, middlewares::*};
use crate::{
    app::App,
    domain::token::Session,
    err::{AppErr, ToAppErr},
};
use argon2::{
    Argon2, PasswordHasher,
    password_hash::{SaltString, rand_core},
};
use axum::{
    Extension, Json, Router,
    extract::State,
    middleware,
    routing::{get, post},
};
use tracing::{debug, error};
use uuid::Uuid;
use validator::Validate;

pub fn make_app(app: App) -> Router {
    let login_router = Router::new()
        .route("/login", post(login))
        .layer(middleware::from_fn(login_middleware));

    let with_auth = Router::new()
        .route("/me", get(me))
        .layer(middleware::from_fn_with_state(app.clone(), auth_middleware));

    Router::new()
        .route("/register", post(register))
        .route("/delete", post(delete))
        .merge(login_router)
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

    let (session, auth) = Session::new(user.id, ip);

    sqlx::query!(
        "insert into sessions (id, user_id, token_id, last_token_id, ip_addr) values ($1, $2, $3, $4, $5)",
        session.id,
        session.user,
        session.token,
        session.last_token,
        session.ip
    ).execute(&app.pool).await.app_log()?;

    let token = app.jwt.gen_jwt_auth(auth)?;
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
