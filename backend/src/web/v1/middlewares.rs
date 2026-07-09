use crate::{
    app::App,
    domain::token::{CheckRes, Session},
    err::{AppErr, ToAppErr},
};
use axum::{
    body::Body,
    extract::{Request, State},
    middleware::Next,
    response::Response,
};
use chrono::Utc;

pub async fn login_middleware(mut req: Request<Body>, next: Next) -> Result<Response, AppErr> {
    let ip = req
        .headers()
        .get("X-Real-IP")
        .and_then(|x| x.to_str().ok())
        .map(|s| s.to_string())
        .unwrap_or_else(|| "127.0.0.1".to_string());

    req.extensions_mut().insert(ip);
    Ok(next.run(req).await)
}

pub async fn auth_middleware(
    State(app): State<App>,
    mut req: Request<Body>,
    next: Next,
) -> Result<Response, AppErr> {
    let ip = req
        .headers()
        .get("X-Real-IP")
        .and_then(|x| x.to_str().ok())
        .map(|s| s.to_string())
        .unwrap_or_else(|| "127.0.0.1".to_string());

    let header = req
        .headers()
        .get("Authorization")
        .and_then(|x| x.to_str().ok())
        .ok_or(AppErr::Unauthorized)?;

    let auth = app.jwt.parse_jwt_auth(header, ip)?;

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

    match session.check(auth) {
        CheckRes::Ok(session) => {
            sqlx::query!(
                "update sessions set token_id = $1, last_token_id = $2, ip_addr = $3, last_active_at = $4",
                session.token,
                session.last_token,
                session.ip,
                session.last_active_at
            ).execute(&app.pool).await.app_log()?;

            req.extensions_mut().insert(session.user);
            Ok(next.run(req).await)
        }

        CheckRes::NeedProlong => Err(AppErr::NeedProlong),

        CheckRes::Delete(id) => {
            let _ = sqlx::query!("delete from sessions where id = $1", id)
                .execute(&app.pool)
                .await
                .app_log();

            Err(AppErr::Unauthorized)
        }
    }
}
