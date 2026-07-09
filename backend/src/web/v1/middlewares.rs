use crate::err::AppErr;
use axum::{body::Body, extract::Request, middleware::Next, response::Response};

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
