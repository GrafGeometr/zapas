//! Модуль с проверкой приглашений и их созданием.
use crate::{
    domain::auth::TokenData,
    err::{AppErr, ErrorLog},
};
use chrono::{Duration, Utc};
use jsonwebtoken::{DecodingKey, EncodingKey, Header, Validation, decode, encode};
use serde::{Deserialize, Serialize};
use uuid::Uuid;

/// Сервис для создания и обработки приглашений.
#[derive(Clone)]
pub struct InviteService {
    en: EncodingKey,
    de: DecodingKey,
}

/// Токен-приглашение.
///
/// Stateless token.
/// Можно пригласить как любого, так и конкретного
/// человека. Из-за своей природы нельзя
/// отозвать или зафиксировать число участников,
/// которое можно макс. пригласить.
#[derive(Clone, Serialize, Deserialize)]
pub struct InviteToken {
    room: Uuid,
    user: Option<Uuid>,

    ver: u64,

    exp: i64,
}

impl InviteService {
    const ALGO: jsonwebtoken::Algorithm = jsonwebtoken::Algorithm::HS512;

    /// Создание сервиса по секрету.
    pub fn new(secret: &str) -> Self {
        Self {
            en: EncodingKey::from_secret(secret.as_bytes()),
            de: DecodingKey::from_secret(secret.as_bytes()),
        }
    }

    /// Создание приглашения.
    pub fn make_invite(
        &self,
        room: Uuid,
        for_user: Option<Uuid>,
        dur: Duration,
    ) -> Result<TokenData, AppErr> {
        let token = InviteToken {
            room,
            user: for_user,
            ver: rand::random(),
            exp: (Utc::now() + dur).timestamp(),
        };

        let token = encode(&Self::header(), &token, &self.en).log_err()?;

        Ok(TokenData { token })
    }

    /// Проверка приглашения.
    ///
    /// Возвращает `id` комнаты, в которую нужно пригласить `user`.
    pub fn check_invite(&self, token: &str, user: Uuid) -> Result<Uuid, AppErr> {
        let token: InviteToken = decode(token.as_bytes(), &self.de, &Self::validation())?.claims;

        if let Some(permit_user) = token.user
            && permit_user != user
        {
            return Err(AppErr::PermDenied);
        }

        Ok(token.room)
    }

    fn header() -> Header {
        Header {
            alg: Self::ALGO,
            ..Default::default()
        }
    }

    fn validation() -> Validation {
        Validation::new(Self::ALGO)
    }
}
