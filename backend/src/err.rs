use axum::{Json, http::StatusCode, response::IntoResponse};
use serde::Serialize;
use thiserror::Error;
use tracing::warn;
use validator::ValidationErrors;

#[derive(Debug, Error)]
pub enum AppErr {
    #[error("Not implemented")]
    NotImplemented,

    #[error("Internal server error")]
    IntrSrvErr,

    #[error("Validation error: {0}")]
    ValidateErr(ValidationErrors),

    #[error("Username already exists")]
    UserNameAlreadyExists,

    #[error("Email already exists")]
    EmailAlreadyExists,
}

#[derive(Serialize)]
struct AppErrJson {
    code: u16,
    msg: String,
}

impl AppErr {
    pub fn trace(&self) {
        warn!("AppErr: {:?} --- {}", self, self);
    }

    fn code(&self) -> StatusCode {
        match self {
            AppErr::NotImplemented => StatusCode::NOT_IMPLEMENTED,
            AppErr::ValidateErr(_) => StatusCode::BAD_REQUEST,
            _ => StatusCode::INTERNAL_SERVER_ERROR,
        }
    }
}

impl IntoResponse for AppErr {
    fn into_response(self) -> axum::response::Response {
        self.trace();

        let code = self.code();
        let json = AppErrJson {
            code: code.as_u16(),
            msg: format!("{}", self),
        };
        (code, Json(json)).into_response()
    }
}

impl From<validator::ValidationErrors> for AppErr {
    fn from(value: validator::ValidationErrors) -> Self {
        Self::ValidateErr(value)
    }
}
