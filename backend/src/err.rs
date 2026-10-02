//! Модуль с описанием всех ошибок программы.
use axum::{Json, http::StatusCode, response::IntoResponse};
use serde::Serialize;
use thiserror::Error;
use tracing::{error, warn};
use utoipa::ToSchema;

/// Глобальная ошибка всей программы. Происходит при конфигах и тд.
pub type GlobalErr = Box<dyn std::error::Error>;

/// Общий enum ошибок программы.
///
/// Активно используется в API.
#[derive(Debug, Error)]
pub enum AppErr {
    #[error("Not implemented")]
    NotImpl,

    #[error("Internal server error")]
    Internal,

    #[error("Validation error: {0}")]
    Validate(#[from] validator::ValidationErrors),
}

/// Возвращаемый тип ошибки для пользователя.
#[derive(Serialize, ToSchema)]
pub struct AppErrJson {
    code: u16,
    msg: String,
}

/// Трейт логгирования.
///
/// Необходим для того, чтобы удобно логгировать
/// все ошибки, которые не нужно обрабатывать. Пользователю
/// возвращается `INTERNAL_SERVER_ERROR`, в логи пишется
/// истинная причина.
pub trait ErrorLog<T> {
    fn log_warn(self) -> Result<T, AppErr>;

    fn log_err(self) -> Result<T, AppErr>;
}

impl<T, E: std::fmt::Display> ErrorLog<T> for Result<T, E> {
    #[track_caller]
    fn log_warn(self) -> Result<T, AppErr> {
        self.map_err(|err| {
            warn!("Internal error: {}", err);
            AppErr::Internal
        })
    }

    #[track_caller]
    fn log_err(self) -> Result<T, AppErr> {
        self.map_err(|err| {
            error!("Internal error: {}", err);
            AppErr::Internal
        })
    }
}

impl AppErr {
    /// HTTP код ошибки.
    fn code(&self) -> StatusCode {
        match self {
            AppErr::NotImpl => StatusCode::NOT_IMPLEMENTED,
            AppErr::Internal => StatusCode::INTERNAL_SERVER_ERROR,
            AppErr::Validate(_) => StatusCode::BAD_REQUEST,
        }
    }

    /// Логирование для AppErr.
    fn trace(&self) {
        warn!("AppErr: {}", self);
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
