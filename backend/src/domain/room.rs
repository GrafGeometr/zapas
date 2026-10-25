use chrono::{DateTime, Utc};
use uuid::Uuid;

#[derive(Clone)]
pub struct MakeRoom {
    pub user: Uuid,

    pub display_name: String,
    pub bio: String,
}

#[derive(Clone)]
pub struct Room {
    pub id: Uuid,

    pub display_name: String,
    pub bio: String,

    pub review_interval: i64,
    pub expired_notify: i64,

    pub created_at: DateTime<Utc>,
}

#[derive(Clone)]
pub struct UpdRoom {
    pub display_name: Option<String>,
    pub bio: Option<String>,
}

#[derive(Clone)]
pub struct UpdTiming {
    pub review_interval: Option<i64>,
    pub expired_notify: Option<i64>,
}
