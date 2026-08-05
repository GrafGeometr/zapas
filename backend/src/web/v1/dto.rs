use chrono::{DateTime, Duration, Utc};
use serde::{Deserialize, Serialize};
use utoipa::{IntoParams, ToSchema};
use uuid::Uuid;
use validator::{Validate, ValidationError};

#[derive(Serialize, Validate, Deserialize, IntoParams)]
pub struct Pagination {
    #[validate(range(min = 0))]
    pub num: i64,

    #[validate(range(min = 1, max = 10_000))]
    pub size: i64,
}

impl Pagination {
    pub fn limit(&self) -> i64 {
        self.size
    }

    pub fn offset(&self) -> i64 {
        self.num * self.size
    }
}

#[derive(Deserialize, Validate, ToSchema)]
pub struct RegisterDto {
    #[validate(length(min = 4, max = 63), custom(function = "validate_username"))]
    #[schema(example = "bob_ross")]
    pub username: String,

    #[validate(email)]
    #[schema(example = "bob.ross@example.com")]
    pub email: Option<String>,

    #[validate(length(min = 1, max = 127))]
    #[schema(example = "bob ross")]
    pub display_name: String,

    #[validate(length(max = 1023))]
    #[schema(example = "")]
    pub bio: String,

    #[validate(length(min = 0, max = 255))] // TODO
    #[schema(example = "super_password!67")]
    pub passwd: String,
}

#[derive(Serialize, Deserialize, ToSchema)]
pub struct UuidDto {
    pub id: Uuid,
}

#[derive(Deserialize, Validate, ToSchema)]
pub struct LoginDto {
    #[validate(length(min = 4, max = 63), custom(function = "validate_username"))]
    #[schema(example = "bob_ross")]
    pub username: String,

    #[validate(length(min = 0, max = 255))] // TODO
    #[schema(example = "super_password!67")]
    pub passwd: String,
}

#[derive(Serialize, Deserialize, ToSchema)]
pub struct TokenDto {
    pub token: String,
}

#[derive(Serialize, ToSchema)]
pub struct UserMeDto {
    pub id: Uuid,

    #[schema(example = "bob_ross")]
    pub username: String,

    #[schema(example = "bob.ross@example.com")]
    pub email: Option<String>,

    #[schema(example = true)]
    pub verified_email: bool,

    #[schema(example = "bob ross")]
    pub display_name: String,

    #[schema(example = "")]
    pub bio: String,

    pub created_at: DateTime<Utc>,
}

#[derive(Deserialize, Validate, ToSchema)]
pub struct MyProfileDto {
    #[validate(length(min = 4, max = 63), custom(function = "validate_username"))]
    #[schema(example = "bob_ross")]
    pub username: Option<String>,

    #[validate(length(min = 1, max = 127))]
    #[schema(example = "bob ross")]
    pub display_name: Option<String>,

    #[validate(length(max = 1023))]
    #[schema(example = "")]
    pub bio: Option<String>,
}

#[derive(Serialize, ToSchema)]
pub struct SessionDto {
    pub id: Uuid,
    pub ip_addr: String,
    pub last_active_at: DateTime<Utc>,
    pub created_at: DateTime<Utc>,
}

#[derive(Deserialize, Validate, ToSchema)]
pub struct PasswdDto {
    #[validate(length(max = 255))]
    pub old_passwd: String,

    #[validate(length(min = 0, max = 255))] // TODO
    pub passwd: String,
}

#[derive(Deserialize, Validate, ToSchema)]
pub struct EmailDto {
    #[validate(email)]
    pub email: String,
}

#[derive(Deserialize, Validate, ToSchema)]
pub struct MakeRoomDto {
    #[validate(length(min = 1, max = 127))]
    #[schema(example = "502")]
    pub display_name: String,

    #[validate(length(max = 1023))]
    #[schema(example = "")]
    pub bio: String,
}

#[derive(Serialize, ToSchema)]
pub struct RoomProfileDto {
    pub id: Uuid,

    pub display_name: String,
    pub bio: String,

    pub review_interval: Option<Duration>,
    pub expired_notify: Option<Duration>,

    pub created_at: DateTime<Utc>,
}

pub struct RoomProfileDb {
    pub id: Uuid,

    pub display_name: String,
    pub bio: String,

    pub review_interval: Option<i64>,
    pub expired_notify: Option<i64>,

    pub created_at: DateTime<Utc>,
}

impl From<RoomProfileDb> for RoomProfileDto {
    fn from(x: RoomProfileDb) -> Self {
        Self {
            id: x.id,
            display_name: x.display_name,
            bio: x.bio,
            review_interval: x.review_interval.map(|x| Duration::seconds(x)),
            expired_notify: x.expired_notify.map(|x| Duration::seconds(x)),
            created_at: x.created_at,
        }
    }
}

#[derive(Deserialize, Validate, ToSchema)]
pub struct UpdRoomProfileDto {
    #[validate(length(min = 1, max = 127))]
    pub display_name: Option<String>,

    #[validate(length(max = 1023))]
    pub bio: Option<String>,
}

#[derive(Deserialize, Validate, ToSchema)]
pub struct SetTimingDto {
    #[validate(range(min = 0))]
    pub review_interval: Option<i64>,
    #[validate(range(min = 0))]
    pub expired_notify: Option<i64>,
}

#[derive(Serialize, ToSchema)]
pub struct MemberDto {
    pub id: Uuid,
    pub user_id: Option<Uuid>,
    pub room_id: Uuid,

    pub display_name: String,
    pub bio: String,

    pub created_at: DateTime<Utc>,
}

#[derive(Serialize, ToSchema)]
pub struct MemberOriginDto {
    pub id: Uuid,
    pub user_id: Option<Uuid>,
    pub room_id: Uuid,

    pub display_name: Option<String>,
    pub bio: Option<String>,

    pub created_at: DateTime<Utc>,
}

#[derive(Deserialize, ToSchema, Validate)]
pub struct SetMemberProfileDto {
    #[validate(length(min = 1, max = 63))]
    pub display_name: Option<String>,

    #[validate(length(max = 1023))]
    pub bio: Option<String>,
}

#[derive(Serialize, Deserialize, ToSchema, Validate)]
pub struct PortionsDto {
    #[validate(range(min = 0))]
    pub portions: i64,
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

#[derive(Deserialize, Validate, ToSchema)]
pub struct MakeStorageDto {
    #[validate(length(min = 1, max = 127))]
    #[schema(example = "fridge")]
    pub display_name: String,

    #[validate(length(max = 1023))]
    #[schema(example = "")]
    pub bio: String,
}

#[derive(Serialize, ToSchema)]
pub struct StorageDto {
    pub id: Uuid,
    pub room_id: Uuid,
    pub display_name: String,
    pub bio: String,

    pub review_interval: Option<Duration>,
    pub expired_notify: Option<Duration>,

    pub created_at: DateTime<Utc>,
}

pub struct StorageDb {
    pub id: Uuid,
    pub room_id: Uuid,
    pub display_name: String,
    pub bio: String,

    pub review_interval: Option<i64>,
    pub expired_notify: Option<i64>,

    pub created_at: DateTime<Utc>,
}

impl From<StorageDb> for StorageDto {
    fn from(x: StorageDb) -> Self {
        Self {
            id: x.id,
            room_id: x.room_id,
            display_name: x.display_name,
            bio: x.bio,
            review_interval: x.review_interval.map(|x| Duration::seconds(x)),
            expired_notify: x.expired_notify.map(|x| Duration::seconds(x)),
            created_at: x.created_at,
        }
    }
}

#[derive(Deserialize, Validate, ToSchema)]
pub struct UpdStorageProfileDto {
    #[validate(length(min = 1, max = 127))]
    pub display_name: Option<String>,

    #[validate(length(max = 1023))]
    pub bio: Option<String>,
}
