use crate::{
    app::App,
    domain::{
        auth::{AuthToken, Session, TokenData, UserSession},
        pag::{Pag, PagArg, PagRes},
        passwd::check_passwd,
    },
    err::{AppErr, AppErrJson, ErrorLog},
};
use axum::{
    Extension, Json, Router,
    body::Body,
    extract::{Query, Request, State},
    middleware::{self, Next},
    response::Response,
    routing::{get, post},
};
use chrono::{DateTime, Utc};
use ipnetwork::IpNetwork;
use serde::{Deserialize, Serialize};
use std::net::IpAddr;
use utoipa::{IntoParams, OpenApi, ToSchema};
use uuid::Uuid;
use validator::Validate;

pub fn router(app: App) -> Router {
    let with_ip = Router::new()
        .route("/login", post(login))
        .layer(middleware::from_fn(ip_middleware));

    let with_user = Router::new()
        .route("/me/logout", post(logout))
        .route("/me/sessions", get(list_sessions))
        .layer(middleware::from_fn_with_state(app.clone(), auth_middleware));

    let with_token = Router::new()
        .route("/prolong", post(prolong))
        .route("/gen_access", post(gen_access))
        .layer(middleware::from_fn_with_state(
            app.clone(),
            simple_auth_middleware,
        ));

    Router::new()
        .merge(with_ip)
        .merge(with_user)
        .merge(with_token)
        .with_state(app)
}

#[derive(OpenApi)]
#[openapi(paths(login, logout, list_sessions, prolong, gen_access))]
pub struct AuthApi;

#[derive(Deserialize, Validate, ToSchema)]
struct LoginDto {
    #[schema(example = "bob_ross")]
    #[validate(length(min = 4, max = 63))]
    username: String,

    #[schema(example = "Super_password!67")]
    #[validate(length(min = 1, max = 127))]
    passwd: String,
}

#[derive(Deserialize, IntoParams, ToSchema)]
struct LogoutQuery {
    #[serde(default)]
    all: bool,
}

#[derive(Clone, Serialize, ToSchema)]
struct SessionDto {
    id: Uuid,
    ip_addr: String,
    last_active_at: DateTime<Utc>,
    created_at: DateTime<Utc>,
}

#[utoipa::path(post,
    tag = "auth",
    path = "/login",
    description = "Creating session and token using username and password",
    request_body = LoginDto,
    responses(
        (status = 200, body = TokenData),
        (status = 500, body = AppErrJson, description = "Internal server error"),
        (status = 400, body = AppErrJson, description = "Invalid any data format (validation error)"),
        (status = 401, body = AppErrJson, description = "Wrong username or password")
    )
)]
async fn login(
    State(app): State<App>,
    Extension(ip): Extension<IpAddr>,
    Json(login): Json<LoginDto>,
) -> Result<Json<TokenData>, AppErr> {
    login.validate()?;

    let res = sqlx::query!(
        r#"
        select
            id,
            passwd_hash
        from users
        where username = $1"#,
        login.username
    )
    .fetch_optional(&app.pool)
    .await?
    .ok_or(AppErr::BadUsername)?;
    check_passwd(&res.passwd_hash, &login.passwd)?;

    let (session, token) = Session::new(res.id, ip);
    let token = app.jwt.encode(token)?;

    let ip = IpNetwork::new(session.ip, 32).expect("Invalid ip");
    sqlx::query!(
        r#"
        insert into sessions (
            id,
            user_id,
            ver,
            ip_addr,
            last_active_at,
            created_at
        ) values (
            $1,
            $2,
            $3,
            $4,
            $5,
            $6
        )"#,
        session.id,
        session.user,
        session.ver,
        ip,
        session.last_active_at,
        session.created_at
    )
    .execute(&app.pool)
    .await?;

    Ok(Json(token))
}

#[utoipa::path(post,
    tag = "auth",
    path = "/me/logout",
    description = "Delete current or all sessions for user",
    params(
        LogoutQuery
    ),
    responses(
        (status = 200),
        (status = 500, body = AppErrJson, description = "Internal server error"),
        (status = 401, body = AppErrJson, description = "Token is missing or not verified (maybe deleted or ip changed)"),
        (status = 403, body = AppErrJson, description = "Invalid or expired token"),
    ),
    security(
        ("jwt" = [])
    )
)]
async fn logout(
    State(app): State<App>,
    Extension(user): Extension<UserSession>,
    Query(LogoutQuery { all }): Query<LogoutQuery>,
) -> Result<(), AppErr> {
    if all {
        sqlx::query!("delete from sessions where user_id = $1", user.user)
            .execute(&app.pool)
            .await?;
    } else {
        sqlx::query!("delete from sessions where id = $1", user.session)
            .execute(&app.pool)
            .await?;
    }

    Ok(())
}

#[utoipa::path(get,
    tag = "auth",
    path = "/me/sessions",
    description = "List of sessions for user",
    params(
        Pag
    ),
    responses(
        (status = 200, body = PagRes<SessionDto>),
        (status = 500, body = AppErrJson, description = "Internal server error"),
        (status = 400, body = AppErrJson, description = "Invalid any data format (validation error)"),
        (status = 401, body = AppErrJson, description = "Token is missing or not verified (maybe deleted or ip changed)"),
        (status = 403, body = AppErrJson, description = "Invalid or expired token"),
    ),
    security(
        ("jwt" = [])
    )
)]
async fn list_sessions(
    State(app): State<App>,
    Extension(user): Extension<UserSession>,
    Query(pag): Query<Pag>,
) -> Result<Json<PagRes<SessionDto>>, AppErr> {
    pag.validate()?;

    let f = async |pag: PagArg| -> Result<_, AppErr> {
        let res: Vec<_> = sqlx::query!(
            r#"
            select
                id,
                ip_addr,
                last_active_at,
                created_at
            from sessions
            where user_id = $1
            order by last_active_at desc
            offset $2 limit $3"#,
            user.user,
            pag.offset,
            pag.limit
        )
        .fetch_all(&app.pool)
        .await?
        .into_iter()
        .map(|x| SessionDto {
            id: x.id,
            ip_addr: x.ip_addr.ip().to_string(),
            last_active_at: x.last_active_at,
            created_at: x.created_at,
        })
        .collect();

        Ok(res)
    };

    Ok(Json(PagRes::new_async_with_err(pag, f).await?))
}

#[utoipa::path(post,
    tag = "auth",
    path = "/prolong",
    description = "Creating new refresh token based on the old one",
    responses(
        (status = 200, body = TokenData),
        (status = 500, body = AppErrJson, description = "Internal server error"),
        (status = 401, body = AppErrJson, description = "Token is missing or not verified (maybe deleted or ip changed)"),
        (status = 403, body = AppErrJson, description = "Invalid or expired token"),
    ),
    security(
        ("jwt" = [])
    )
)]
async fn prolong(
    State(app): State<App>,
    Extension(ip): Extension<IpAddr>,
    Extension(token): Extension<AuthToken>,
) -> Result<Json<TokenData>, AppErr> {
    let refresh = match token {
        AuthToken::Access(_) => {
            return Err(AppErr::InvalidToken);
        }

        AuthToken::Refresh(x) => x,
    };

    let mut session = load_session(&app, refresh.session()).await?;
    let token = session.prolong(refresh, ip)?;
    let token = app.jwt.encode(token)?;
    upd_session(&app, session).await?;

    Ok(Json(token))
}

#[utoipa::path(post,
    tag = "auth",
    path = "/gen_access",
    description = "Creating new access token based on refresh",
    responses(
        (status = 200, body = TokenData),
        (status = 500, body = AppErrJson, description = "Internal server error"),
        (status = 401, body = AppErrJson, description = "Token is missing or not verified (maybe deleted or ip changed)"),
        (status = 403, body = AppErrJson, description = "Invalid or expired token"),
    ),
    security(
        ("jwt" = [])
    )
)]
async fn gen_access(
    State(app): State<App>,
    Extension(ip): Extension<IpAddr>,
    Extension(token): Extension<AuthToken>,
) -> Result<Json<TokenData>, AppErr> {
    let refresh = match token {
        AuthToken::Access(_) => {
            return Err(AppErr::InvalidToken);
        }

        AuthToken::Refresh(x) => x,
    };

    let mut session = load_session(&app, refresh.session()).await?;
    let token = session.make_access(refresh, ip)?;
    let token = app.jwt.encode(token)?;
    upd_session(&app, session).await?;

    Ok(Json(token))
}

/// Middleware с проверкой токена и генерацией итогового `UserSession`.
///
/// Дает полностью все данные, необходимые большинству функциям.
/// Скрывает всю логику с токенами и сессиями.
pub async fn auth_middleware(
    State(app): State<App>,
    mut req: Request<Body>,
    next: Next,
) -> Result<Response, AppErr> {
    let ip: IpAddr = req
        .headers()
        .get("X-Real-IP")
        .and_then(|x| x.to_str().ok())
        .unwrap_or("127.0.0.1")
        .parse()
        .log_err()?;

    let header = req
        .headers()
        .get("Authorization")
        .and_then(|x| x.to_str().ok())
        .ok_or(AppErr::Unauthorized)?;

    let session = match app.jwt.decode(header)? {
        crate::domain::auth::AuthToken::Access(access) => Session::validate_access(access, ip)?,

        crate::domain::auth::AuthToken::Refresh(refresh) => {
            let mut session = load_session(&app, refresh.session()).await?;
            let res = session.validate_refresh(refresh, ip)?;
            upd_session(&app, session).await?;
            res
        }
    };

    req.extensions_mut().insert(session);
    Ok(next.run(req).await)
}

/// Middleware для детальной работы с токенами и сессиями - дает `ip`
/// и непроверенный `auth_token`.
async fn simple_auth_middleware(
    State(app): State<App>,
    mut req: Request<Body>,
    next: Next,
) -> Result<Response, AppErr> {
    let ip: IpAddr = req
        .headers()
        .get("X-Real-IP")
        .and_then(|x| x.to_str().ok())
        .unwrap_or("127.0.0.1")
        .parse()
        .log_err()?;

    let header = req
        .headers()
        .get("Authorization")
        .and_then(|x| x.to_str().ok())
        .ok_or(AppErr::Unauthorized)?;

    let res = app.jwt.decode(header)?;
    req.extensions_mut().insert(ip);
    req.extensions_mut().insert(res);
    Ok(next.run(req).await)
}

/// Middleware для получения `ip`.
async fn ip_middleware(mut req: Request<Body>, next: Next) -> Result<Response, AppErr> {
    let ip: IpAddr = req
        .headers()
        .get("X-Real-IP")
        .and_then(|x| x.to_str().ok())
        .unwrap_or("127.0.0.1")
        .parse()
        .log_err()?;

    req.extensions_mut().insert(ip);
    Ok(next.run(req).await)
}

/// Загрузка сессии для domain логики по id.
async fn load_session(app: &App, id: Uuid) -> Result<Session, AppErr> {
    let x = sqlx::query!(
        r#"
        select
            id,
            user_id as user,
            ver,
            ip_addr as ip,
            last_active_at,
            created_at
        from sessions
        where id = $1"#,
        id,
    )
    .fetch_optional(&app.pool)
    .await?
    .ok_or(AppErr::Unauthorized)?;

    let session = Session::restore(x.id, x.user, x.ver, x.ip, x.last_active_at, x.created_at);
    Ok(session)
}

/// Обнолвение сессии или ее возможное удаление.
async fn upd_session(app: &App, session: Session) -> Result<(), AppErr> {
    if session.need_delete() {
        sqlx::query!("delete from sessions where id = $1", session.id)
            .execute(&app.pool)
            .await?;
    } else if session.need_upd() {
        let ip = IpNetwork::new(session.ip, 32).expect("Invalid ip");

        sqlx::query!(
            r#"
            update sessions set
                ver = $2,
                ip_addr = $3,
                last_active_at = $4
            where id = $1"#,
            session.id,
            session.ver,
            ip,
            session.last_active_at
        )
        .execute(&app.pool)
        .await?;
    }

    Ok(())
}
