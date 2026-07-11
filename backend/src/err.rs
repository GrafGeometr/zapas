//! Error description module.
use axum::{Json, http::StatusCode, response::IntoResponse};
use serde::Serialize;
use thiserror::Error;
use tracing::{error, warn};
use utoipa::ToSchema;
use validator::ValidationErrors;

/// Application error enum.
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

    #[error("Wrong username or password")]
    WrongUserOrPasswd,

    #[error("Unauthorized")]
    Unauthorized,

    #[error("Need prolongation token")]
    NeedProlong,

    #[error("Permission denied")]
    PermissionDenied,
}

/// Error JSON structure for responses.
#[derive(Serialize, ToSchema)]
pub struct AppErrJson {
    code: u16,
    msg: String,
}

impl AppErr {
    /// Traces the error.
    pub fn trace(&self) {
        warn!("AppErr: {:?} --- {}", self, self);
    }

    /// Returns the HTTP status code for the error.
    fn code(&self) -> StatusCode {
        match self {
            AppErr::NotImplemented => StatusCode::NOT_IMPLEMENTED,

            AppErr::ValidateErr(_) => StatusCode::BAD_REQUEST,

            AppErr::UserNameAlreadyExists => StatusCode::CONFLICT,
            AppErr::EmailAlreadyExists => StatusCode::CONFLICT,

            AppErr::WrongUserOrPasswd => StatusCode::UNAUTHORIZED,
            AppErr::Unauthorized => StatusCode::UNAUTHORIZED,
            AppErr::NeedProlong => StatusCode::UNAUTHORIZED,

            AppErr::PermissionDenied => StatusCode::FORBIDDEN,

            _ => StatusCode::INTERNAL_SERVER_ERROR,
        }
    }
}

impl IntoResponse for AppErr {
    /// Converts the error into an HTTP response.
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

/// Trait to convert a `Result` into an application-specific error with logging.
pub trait ToAppErr<T> {
    /// Maps an error to `AppErr` and logs it at the place where the `app_log` function is called.
    fn app_log(self) -> Result<T, AppErr>;
}

impl<T, E: std::fmt::Display> ToAppErr<T> for Result<T, E> {
    #[track_caller]
    fn app_log(self) -> Result<T, AppErr> {
        self.map_err(|e| {
            error!("Internal error: {}", e);
            AppErr::IntrSrvErr
        })
    }
}
