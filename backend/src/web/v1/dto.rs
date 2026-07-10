use chrono::{DateTime, Duration, Utc};
use serde::{Deserialize, Serialize};
use utoipa::ToSchema;
use uuid::Uuid;
use validator::Validate;

#[derive(Serialize, Deserialize, ToSchema)]
pub struct Pagination {
    pub num: i64,
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
    #[validate(length(min = 4, max = 63))]
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

    #[validate(length(min = 12, max = 255))]
    #[schema(example = "super_password!67")]
    pub passwd: String,
}

#[derive(Serialize, Deserialize, ToSchema)]
pub struct UuidDto {
    pub id: Uuid,
}

#[derive(Deserialize, Validate, ToSchema)]
pub struct LoginDto {
    // TODO
    #[schema(example = "bob_ross")]
    pub username: String,

    // TODO
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

#[derive(Deserialize, Validate)]
pub struct MyProfileDto {
    // TODO
    pub username: Option<String>,
    pub display_name: Option<String>,
    pub bio: Option<String>,
}

#[derive(Serialize)]
pub struct SessionDto {
    pub id: Uuid,
    pub ip_addr: String,
    pub last_active_at: DateTime<Utc>,
    pub created_at: DateTime<Utc>,
}

#[derive(Deserialize, Validate)]
pub struct PasswdDto {
    // TODO
    pub old_passwd: String,
    pub passwd: String,
}

#[derive(Deserialize, Validate)]
pub struct EmailDto {
    #[validate(email)]
    pub email: String,
}

#[derive(Deserialize, Validate)]
pub struct MakeRoomDto {
    // TODO
    pub display_name: String,
    pub bio: String,
}

#[derive(Serialize)]
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
