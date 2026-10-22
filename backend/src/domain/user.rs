use chrono::{DateTime, Utc};
use uuid::Uuid;

#[derive(Clone)]
pub struct User {
    pub id: Uuid,

    pub username: String,
    pub email: Option<String>,
    pub verified_email: bool,

    pub display_name: String,
    pub bio: String,

    pub created_at: DateTime<Utc>,
}

#[derive(Clone)]
pub struct MakeUser {
    pub id: Uuid,

    pub username: String,
    pub email: Option<String>,

    pub passwd_hash: String,

    pub display_name: String,
    pub bio: String,
}

#[derive(Clone)]
pub struct UpdUser {
    pub username: Option<String>,
    pub display_name: Option<String>,
    pub bio: Option<String>,
}

#[derive(Clone)]
pub struct Email {
    pub email: Option<String>,
}
