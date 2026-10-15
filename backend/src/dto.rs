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

#[derive(Deserialize, Validate, ToSchema)]
pub struct RegisterDto {
    #[schema(example = "bob_ross")]
    #[validate(length(min = 4, max = 63), custom(function = validate_username))]
    pub username: String,

    #[schema(example = "bob.ross@example.com")]
    #[validate(email, length(max = 255))]
    pub email: Option<String>,

    #[schema(example = "Super_password!67")]
    #[validate(length(min = 12, max = 127), custom(function = validate_passwd))]
    pub passwd: String,

    #[schema(example = "bob ross")]
    #[validate(length(min = 1, max = 127))]
    pub display_name: String,

    #[schema(example = "")]
    #[validate(length(max = 1024))]
    pub bio: String,
}

#[derive(Serialize, ToSchema)]
pub struct UserMe {
    pub id: Uuid,

    #[schema(example = "bob_ross")]
    pub username: String,

    #[schema(example = "bob.ross@example.com")]
    pub email: Option<String>,

    #[schema(example = false)]
    pub verified_email: bool,

    #[schema(example = "bob ross")]
    pub display_name: String,

    #[schema(example = "")]
    pub bio: String,

    pub created_at: DateTime<Utc>,
}

#[derive(Serialize, ToSchema)]
pub struct UserOther {
    pub id: Uuid,

    #[schema(example = "omega")]
    pub username: String,

    #[schema(example = "omega bob")]
    pub display_name: String,

    #[schema(example = "omega plus 1 bob")]
    pub bio: String,

    pub created_at: DateTime<Utc>,
}

#[derive(Deserialize, Validate)]
pub struct UserNamePath {
    #[validate(length(min = 4, max = 63), custom(function = validate_username))]
    pub username: String,
}

#[derive(Deserialize, Validate, ToSchema)]
#[validate(schema(function = validate_change_passwd))]
pub struct ChangePasswd {
    #[schema(example = "Super_password!67")]
    #[validate(length(min = 12, max = 127))]
    pub old_passwd: String,

    #[schema(example = "Super_puper_password!67")]
    #[validate(length(min = 12, max = 127), custom(function = validate_passwd))]
    pub passwd: String,
}

#[derive(Deserialize, Validate, ToSchema)]
pub struct UpdUserProfileDto {
    #[validate(length(min = 4, max = 63), custom(function = validate_username))]
    pub username: Option<String>,

    #[validate(length(min = 1, max = 127))]
    pub display_name: Option<String>,

    #[schema(example = "Bio")]
    #[validate(length(max = 1024))]
    pub bio: Option<String>,
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
