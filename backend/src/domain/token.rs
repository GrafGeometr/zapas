use chrono::{DateTime, Utc};
use uuid::Uuid;

pub struct Session {
    pub id: Uuid,
    pub user: Uuid,

    pub token: i64,
    pub last_token: Option<i64>,

    pub ip: String,
    pub last_active_at: DateTime<Utc>,
}

pub struct AuthToken {
    pub id: Uuid,
    pub user: Uuid,

    pub token: i64,
    pub ip: String,
}

pub enum CheckRes {
    Ok(Session),
    NeedProlong,
    Delete,
}

impl Session {
    pub fn new(user: Uuid, ip: String) -> (Self, AuthToken) {
        let id = Uuid::now_v7();
        let token = rand::random();

        let this = Self {
            id,
            user,
            token,
            last_token: None,
            ip: ip.clone(),
            last_active_at: Utc::now(),
        };

        let auth = AuthToken {
            id,
            user,
            token,
            ip,
        };

        (this, auth)
    }

    fn upd(&mut self, ip: String) {
        self.ip = ip;
        self.last_active_at = Utc::now();
    }

    // В случае None следует удалить сессию.
    pub fn prolongation(mut self, auth: AuthToken) -> Option<Self> {
        if self.id != auth.id || self.user != auth.user {
            return None; // always self.id == auth.id???
        }

        if self.token == auth.token {
            self.last_token = Some(self.token);
            self.token = rand::random();
        } else if self.last_token == Some(auth.token) {
            self.token = rand::random();
        } else {
            return None;
        }

        self.upd(auth.ip);
        Some(self)
    }

    pub fn check(mut self, auth: AuthToken) -> CheckRes {
        if self.id != auth.id || self.user != auth.user {
            return CheckRes::Delete;
        }
        if self.ip != auth.ip {
            return CheckRes::NeedProlong;
        }

        if self.token == auth.token {
            self.last_token = None;
        } else if self.last_token == Some(auth.token) {
            return CheckRes::NeedProlong;
        } else {
            return CheckRes::Delete;
        }

        self.upd(auth.ip);
        CheckRes::Ok(self)
    }
}
