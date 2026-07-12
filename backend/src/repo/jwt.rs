use crate::{
    domain::token::{AuthToken, Session, SlInviteToken},
    err::{AppErr, ToAppErr},
};
use chrono::{Duration, Utc};
use jsonwebtoken::{DecodingKey, EncodingKey, Header, Validation, decode, encode};
use serde::{Deserialize, Serialize};
use std::sync::Arc;
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

    pub fn from_env() -> Result<Self, Box<dyn std::error::Error>> {
        Ok(Self::new(&std::env::var("JWT_SECRET")?))
    }

    pub fn new(secret: &str) -> Self {
        Self {
            en: Arc::new(EncodingKey::from_secret(secret.as_bytes())),
            de: Arc::new(DecodingKey::from_secret(secret.as_bytes())),
        }
    }

    pub fn gen_jwt_auth(&self, session: Session) -> Result<String, AppErr> {
        let claims = AuthClaims {
            id: session.id,
            exp: (Utc::now() + Self::AUTH_EXP).timestamp(),
            user: session.user,
            token: session.token,
        };

        let token = encode(&Self::header(), &claims, &self.en).app_log()?;
        Ok(token)
    }

    pub fn parse_jwt_auth(&self, s: &str, ip: String) -> Result<AuthToken, AppErr> {
        let s = Self::remove_bearer(s);

        let valid = Validation::new(Self::ALGO);
        let claims = decode::<AuthClaims>(s, &self.de, &valid).app_log()?.claims;

        let res = AuthToken {
            id: claims.id,
            user: claims.user,
            token: claims.token,
            ip,
        };

        Ok(res)
    }

    pub fn gen_sl_invite(&self, room: Uuid, dur: Duration) -> Result<String, AppErr> {
        let claims = SlInviteClaims {
            room,
            exp: (Utc::now() + dur).timestamp(),
        };

        let token = encode(&Self::header(), &claims, &self.en).app_log()?;
        Ok(token)
    }

    pub fn parse_sl_invite(&self, s: &str) -> Result<SlInviteToken, AppErr> {
        let s = Self::remove_bearer(s);

        let valid = Validation::new(Self::ALGO);
        let claims = decode::<SlInviteClaims>(s, &self.de, &valid)
            .app_log()?
            .claims;

        let res = SlInviteToken { room: claims.room };
        Ok(res)
    }

    fn remove_bearer(s: &str) -> &str {
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
    pub id: Uuid,
    pub exp: i64,

    pub user: Uuid,
    pub token: i64,
}

#[derive(Serialize, Deserialize)]
struct SlInviteClaims {
    pub room: Uuid,
    pub exp: i64,
}
