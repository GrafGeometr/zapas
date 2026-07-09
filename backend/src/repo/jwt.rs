use crate::{domain::token::AuthToken, err::AppErr};
use chrono::{Duration, Utc};
use jsonwebtoken::{DecodingKey, EncodingKey, Header, Validation, decode, encode};
use serde::{Deserialize, Serialize};
use std::sync::Arc;
use tracing::error;
use uuid::Uuid;

#[derive(Clone)]
pub struct JwtRepo {
    en: Arc<EncodingKey>,
    de: Arc<DecodingKey>,
}

impl JwtRepo {
    const ALGO: jsonwebtoken::Algorithm = jsonwebtoken::Algorithm::HS512;
    const AUTH_EXP: Duration = Duration::days(30);
    const BEARER: &'static str = "Bearer";

    pub fn new(secret: &str) -> Self {
        Self {
            en: Arc::new(EncodingKey::from_secret(secret.as_bytes())),
            de: Arc::new(DecodingKey::from_secret(secret.as_bytes())),
        }
    }

    pub fn gen_jwt_auth(&self, auth: AuthToken) -> Result<String, AppErr> {
        let claims = AuthClaims {
            sub: auth.id,
            exp: (Utc::now() + Self::AUTH_EXP).timestamp(),
            user: auth.user,
            token: auth.token,
        };

        let token = encode(&Self::header(), &claims, &self.en).map_err(|e| {
            error!("Jwt gen err: {}", e);
            AppErr::IntrSrvErr
        })?;

        Ok(token)
    }

    pub fn parse_jw_auth(&self, s: &str, ip: String) -> Result<AuthToken, AppErr> {
        let s = Self::remove_bearer(s);

        let valid = Validation::new(Self::ALGO);
        let claims = decode::<AuthClaims>(s, &self.de, &valid)
            .map_err(|e| {
                error!("Jwt parse err: {}", e);
                AppErr::IntrSrvErr
            })?
            .claims;

        let res = AuthToken {
            id: claims.sub,
            user: claims.user,
            token: claims.token,
            ip,
        };

        Ok(res)
    }

    fn remove_bearer<'a>(s: &'a str) -> &'a str {
        if s.starts_with(Self::BEARER) {
            &s[Self::BEARER.len() + 1..]
        } else {
            s
        }
        .trim()
    }

    fn header() -> Header {
        Header {
            alg: Self::ALGO,
            ..Default::default()
        }
    }
}

#[derive(Serialize, Deserialize)]
struct AuthClaims {
    // session_id.
    pub sub: Uuid,
    pub exp: i64,

    pub user: Uuid,
    pub token: u32,
}
