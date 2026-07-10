use chrono::{DateTime, Utc};
use serde::{Deserialize, Serialize};
use utoipa::ToSchema;
use uuid::Uuid;
use validator::Validate;

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
