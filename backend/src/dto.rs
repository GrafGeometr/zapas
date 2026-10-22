//! Модуль со структурами, используемыми в API.
//! Data Transfer Object.
use chrono::{DateTime, NaiveDate, Utc};
use serde::{Deserialize, Serialize};
use utoipa::{IntoParams, ToSchema};
use uuid::Uuid;
use validator::{Validate, ValidationError};

#[derive(Serialize, Deserialize, ToSchema)]
pub struct UuidDto {
    pub id: Uuid,
}

#[derive(Serialize, Deserialize, Validate, ToSchema)]
pub struct ProfileDto {
    #[validate(length(min = 1, max = 127))]
    pub display_name: String,

    #[validate(length(max = 1024))]
    pub bio: String,
}

#[derive(Deserialize, Validate, ToSchema)]
pub struct UpdProfileDto {
    #[validate(length(min = 1, max = 127))]
    pub display_name: Option<String>,

    #[validate(length(max = 1024))]
    pub bio: Option<String>,
}

#[derive(Serialize, Deserialize, Validate, ToSchema)]
pub struct Timing {
    #[validate(range(min = 0))]
    pub review_interval: Option<i64>,

    #[validate(range(min = 0))]
    pub expired_notify: Option<i64>,
}

#[derive(Clone, Serialize, ToSchema)]
pub struct StorageDto {
    pub id: Uuid,
    pub room_id: Uuid,

    #[schema(example = "Fridge")]
    pub display_name: String,

    #[schema(example = "Fridge")]
    pub bio: String,

    pub review_interval: Option<i64>,
    pub expired_notify: Option<i64>,

    pub created_at: DateTime<Utc>,
}

#[derive(Clone, Serialize, ToSchema)]
pub struct RoomDto {
    pub id: Uuid,

    #[schema(example = "502")]
    pub display_name: String,

    #[schema(example = "")]
    pub bio: String,

    pub review_interval: i64,
    pub expired_notify: i64,

    pub created_at: DateTime<Utc>,
}

#[derive(Clone, Copy, Deserialize, Validate, IntoParams)]
pub struct InviteQuery {
    /// None if for all
    pub for_user: Option<Uuid>,

    #[serde(default = "default_secs")]
    #[validate(range(min = 1))]
    pub secs: i64,
}

#[derive(Clone, Serialize, ToSchema)]
pub struct MemberDto {
    pub id: Uuid,

    pub user_id: Option<Uuid>,
    pub room_id: Uuid,

    pub display_name: String,
    pub bio: String,

    pub created_at: DateTime<Utc>,
}

#[derive(Serialize, ToSchema)]
pub struct OriginMemberDto {
    pub id: Uuid,

    pub user_id: Option<Uuid>,
    pub room_id: Uuid,

    pub display_name: Option<String>,
    pub bio: Option<String>,

    pub created_at: DateTime<Utc>,
}

#[derive(Serialize, Deserialize, Validate, ToSchema)]
pub struct DefPortionsDto {
    #[validate(range(min = 0))]
    pub portions: i64,
}

#[derive(Serialize, Deserialize, Validate, ToSchema)]
pub struct PortionDateDto {
    pub date: NaiveDate,

    #[validate(range(min = 0))]
    pub portions: i64,
}

#[derive(Deserialize, Validate, ToSchema)]
#[validate(schema(function = "check_two_date"))]
pub struct TwoDateDto {
    pub begin: NaiveDate,
    pub end: NaiveDate,
}

#[derive(Deserialize, Validate, ToSchema)]
pub struct EmailDto {
    #[validate(email, length(max = 255))]
    pub email: String,
}

#[derive(Deserialize, Validate, ToSchema)]
pub struct LoginDto {
    #[schema(example = "bob_ross")]
    #[validate(length(min = 4, max = 63))]
    pub username: String,

    #[schema(example = "Super_password!67")]
    #[validate(length(min = 1, max = 127))]
    pub passwd: String,
}

#[derive(Deserialize, IntoParams, ToSchema)]
pub struct LogoutQuery {
    #[serde(default)]
    pub all: bool,
}

#[derive(Clone, Serialize, ToSchema)]
pub struct SessionDto {
    pub id: Uuid,
    pub ip_addr: String,
    pub last_active_at: DateTime<Utc>,
    pub created_at: DateTime<Utc>,
}

#[derive(Deserialize, Validate, ToSchema)]
pub struct MakeTransactionDto {
    pub member_to: Uuid,

    #[validate(range(min = 1))]
    pub money: i64,

    #[validate(length(max = 1023))]
    pub bio: String,
}

#[derive(Clone, Serialize, ToSchema)]
pub struct TransactionDto {
    pub id: Uuid,

    pub room_id: Uuid,
    pub member_from: Uuid,
    pub member_to: Uuid,

    pub money: i64,
    pub bio: String,

    pub created_at: DateTime<Utc>,
}

#[derive(Clone, Copy, Deserialize, Validate, IntoParams)]
pub struct TransactionQueryDto {
    pub member_from: Option<Uuid>,
    pub member_to: Option<Uuid>,

    #[validate(nested)]
    pub pag: crate::domain::pag::Pag,
}

#[derive(Deserialize, Validate, ToSchema)]
pub struct UpdTransactionDto {
    #[validate(range(min = 1))]
    pub money: Option<i64>,

    #[validate(length(max = 1023))]
    pub bio: Option<String>,
}

const fn default_secs() -> i64 {
    16 * 60
}

fn check_two_date(x: &TwoDateDto) -> Result<(), ValidationError> {
    if x.begin > x.end {
        Err(validator::ValidationError::new("invalid seq"))
    } else {
        Ok(())
    }
}
