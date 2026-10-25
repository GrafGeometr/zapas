use chrono::{DateTime, Utc};
use uuid::Uuid;

#[derive(Clone)]
pub struct Member {
    pub id: Uuid,

    pub user_id: Uuid,
    pub room_id: Uuid,

    pub display_name: String,
    pub bio: String,

    pub default_portions: i64,

    pub created_at: DateTime<Utc>,
}

#[derive(Clone)]
pub struct OriginMember {
    pub id: Uuid,

    pub user_id: Uuid,
    pub room_id: Uuid,

    pub display_name: Option<String>,
    pub bio: Option<String>,

    pub default_portions: i64,

    pub created_at: DateTime<Utc>,
}

#[derive(Clone)]
pub struct SetMemberProfile {
    pub display_name: Option<String>,
    pub bio: Option<String>,
}
