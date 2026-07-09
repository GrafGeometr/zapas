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
    #[schema(example = "bob_ross")]
    pub username: String,

    #[schema(example = "super_password!67")]
    pub passwd: String,
}

#[derive(Serialize, Deserialize, ToSchema)]
pub struct TokenDto {
    pub token: String,
}
