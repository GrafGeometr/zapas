use chrono::{DateTime, Duration, Utc};
use uuid::Uuid;

pub struct Session {
    pub id: Uuid,
    pub user: Uuid,

    pub token: i64,
    pub last_token: Option<i64>,

    pub ip: String,
    pub last_active_at: DateTime<Utc>,
}

#[derive(Clone)]
pub struct AuthToken {
    pub id: Uuid,
    pub user: Uuid,

    pub token: i64,
    pub ip: String,
}

pub enum ProlongRes {
    Ok(Session),
    Delete(Uuid),
}

pub enum CheckRes {
    Ok(Session),
    OkWithoutWrite(Session),
    NeedProlong,
    Delete(Uuid),
}

impl Session {
    pub fn new(user: Uuid, ip: String) -> Self {
        let id = Uuid::now_v7();
        let token = rand::random();

        Self {
            id,
            user,
            token,
            last_token: None,
            ip: ip,
            last_active_at: Utc::now(),
        }
    }

    fn upd(&mut self, ip: String) {
        self.ip = ip;
        self.last_active_at = Utc::now();
    }

    pub fn prolongation(mut self, auth: AuthToken) -> ProlongRes {
        if self.id != auth.id || self.user != auth.user {
            return ProlongRes::Delete(self.id); // always self.id == auth.id???
        }

        if self.token == auth.token {
            self.last_token = Some(self.token);
            self.token = rand::random();
        } else if self.last_token == Some(auth.token) {
            self.token = rand::random();
        } else {
            return ProlongRes::Delete(self.id);
        }

        self.upd(auth.ip);
        ProlongRes::Ok(self)
    }

    pub fn check(mut self, auth: AuthToken) -> CheckRes {
        if self.id != auth.id || self.user != auth.user {
            return CheckRes::Delete(self.id);
        }
        if self.ip != auth.ip {
            return CheckRes::NeedProlong;
        }

        let mut write = false;

        if self.token == auth.token {
            write |= self.last_token.is_some();
            self.last_token = None;
        } else if self.last_token == Some(auth.token) {
            return CheckRes::NeedProlong;
        } else {
            return CheckRes::Delete(self.id);
        }

        if self.last_active_at + Duration::minutes(15) < Utc::now() {
            self.upd(auth.ip);
            write = true;
        }

        if write {
            CheckRes::Ok(self)
        } else {
            CheckRes::OkWithoutWrite(self)
        }
    }
}

// Stateless
pub struct SlInviteToken {
    pub room: Uuid,
}
