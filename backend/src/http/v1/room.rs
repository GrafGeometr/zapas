use super::*;
use crate::{
    app::App,
    domain::{
        auth::UserSession,
        pag::{Pag, PagArg, PagRes},
    },
    err::{AppErr, AppErrJson},
};
use axum::{
    Extension, Json, Router,
    extract::{Path, Query, State},
    middleware,
    routing::{delete, get, post},
};
use chrono::{DateTime, Utc};
use utoipa::OpenApi;

pub fn router(app: App) -> Router {
    Router::new()
        .route("/make", post(make))
        .route("/all", get(list_rooms))
        .route("/{room_id}", get(room_by_id))
        .route("/{room_id}/delete", delete(room_delete))
        .route("/{room_id}/profile", post(upd_profile))
        .route("/{room_id}/timing", post(upd_timing))
        .layer(middleware::from_fn_with_state(app.clone(), auth_middleware))
        .with_state(app)
}

#[derive(OpenApi)]
#[openapi(paths(make, list_rooms, room_by_id, room_delete, upd_profile, upd_timing))]
pub struct RoomApi;

#[derive(Clone, Serialize, ToSchema)]
struct RoomDto {
    id: Uuid,

    display_name: String,
    bio: String,

    review_interval: i64,
    expired_notify: i64,

    created_at: DateTime<Utc>,
}

#[utoipa::path(post,
    tag = "room",
    path = "/make",
    description = "Make new room",
    request_body = ProfileDto,
    responses(
        (status = 200, body = UuidDto),
        (status = 500, body = AppErrJson, description = "Internal server error"),
        (status = 400, body = AppErrJson, description = "Invalid any data format (validation error)"),
        (status = 401, body = AppErrJson, description = "Token is missing or not verified (maybe deleted or ip changed)"),
        (status = 403, body = AppErrJson, description = "Invalid or expired token"),
    ),
    security(
        ("jwt" = [])
    )
)]
async fn make(
    State(app): State<App>,
    Extension(_user): Extension<UserSession>,
    Json(profile): Json<ProfileDto>,
) -> Result<Json<UuidDto>, AppErr> {
    profile.validate()?;

    let id = Uuid::now_v7();
    sqlx::query!(
        r#"
        insert into rooms (
            id,
            display_name,
            bio
        ) values (
            $1, $2, $3
        )"#,
        id,
        profile.display_name,
        profile.bio
    )
    .execute(&app.pool)
    .await?;

    Ok(Json(UuidDto { id }))
}

#[utoipa::path(get,
    tag = "room",
    path = "/all",
    description = "List of rooms for user",
    params(
        Pag
    ),
    responses(
        (status = 200, body = PagRes<RoomDto>),
        (status = 500, body = AppErrJson, description = "Internal server error"),
        (status = 401, body = AppErrJson, description = "Token is missing or not verified (maybe deleted or ip changed)"),
        (status = 403, body = AppErrJson, description = "Invalid or expired token"),
    ),
    security(
        ("jwt" = [])
    )
)]
async fn list_rooms(
    State(app): State<App>,
    Extension(user): Extension<UserSession>,
    Query(pag): Query<Pag>,
) -> Result<Json<PagRes<RoomDto>>, AppErr> {
    let f = async |pag: PagArg| -> Result<_, AppErr> {
        let res = sqlx::query_as!(
            RoomDto,
            r#"
        select
            r.id,
            r.display_name,
            r.bio,
            r.review_interval,
            r.expired_notify,
            r.created_at
        from members as m
        join rooms as r on m.room_id = r.id
        where m.user_id = $1
        order by m.created_at desc
        offset $2 limit $3"#,
            user.user,
            pag.offset,
            pag.limit
        )
        .fetch_all(&app.pool)
        .await?;
        Ok(res)
    };

    Ok(Json(PagRes::new_async_with_err(pag, f).await?))
}

#[utoipa::path(get,
    tag = "room",
    path = "/{room_id}",
    description = "Get data about room",
    responses(
        (status = 200, body = RoomDto),
        (status = 500, body = AppErrJson, description = "Internal server error"),
        (status = 401, body = AppErrJson, description = "Token is missing or not verified (maybe deleted or ip changed)"),
        (status = 403, body = AppErrJson, description = "Invalid or expired token"),
        (status = 404, body = AppErrJson, description = "Room not found"),
    ),
    security(
        ("jwt" = [])
    )
)]
async fn room_by_id(
    State(app): State<App>,
    Extension(user): Extension<UserSession>,
    Path(room_id): Path<Uuid>,
) -> Result<Json<RoomDto>, AppErr> {
    let res = sqlx::query_as!(
        RoomDto,
        r#"
        select
            id,
            display_name,
            bio,
            review_interval,
            expired_notify,
            created_at
        from rooms
        where id = $2 and permit_room($1, $2)"#,
        user.user,
        room_id
    )
    .fetch_optional(&app.pool)
    .await?
    .ok_or(AppErr::NotFound)?;

    Ok(Json(res))
}

#[utoipa::path(delete,
    tag = "room",
    path = "/{room_id}/delete",
    description = "Delete room (not implemented)",
    responses(
        (status = 500, body = AppErrJson, description = "Internal server error"),
        (status = 501, body = AppErrJson, description = "Not implemented"),
    ),
    security(
        ("jwt" = [])
    )
)]
async fn room_delete(
    State(_app): State<App>,
    Extension(_user): Extension<UserSession>,
    Path(_room_id): Path<Uuid>,
) -> Result<(), AppErr> {
    Err(AppErr::NotImpl)
}

#[utoipa::path(post,
    tag = "room",
    path = "/{room_id}/profile",
    description = "Update room profile",
    request_body = UpdProfileDto,
    responses(
        (status = 200, body = RoomDto),
        (status = 500, body = AppErrJson, description = "Internal server error"),
        (status = 400, body = AppErrJson, description = "Invalid any data format (validation error)"),
        (status = 401, body = AppErrJson, description = "Token is missing or not verified (maybe deleted or ip changed)"),
        (status = 403, body = AppErrJson, description = "Invalid or expired token"),
        (status = 404, body = AppErrJson, description = "Room not found"),
    ),
    security(
        ("jwt" = [])
    )
)]
async fn upd_profile(
    State(app): State<App>,
    Extension(user): Extension<UserSession>,
    Path(room_id): Path<Uuid>,
    Json(profile): Json<UpdProfileDto>,
) -> Result<Json<RoomDto>, AppErr> {
    profile.validate()?;

    let res = sqlx::query_as!(
        RoomDto,
        r#"
        update rooms set
            display_name = coalesce($3, display_name),
            bio = coalesce($4, bio)
        where id = $2 and permit_room($1, $2)
        returning
            id,
            display_name,
            bio,
            review_interval,
            expired_notify,
            created_at"#,
        user.user,
        room_id,
        profile.display_name,
        profile.bio
    )
    .fetch_optional(&app.pool)
    .await?
    .ok_or(AppErr::NotFound)?;

    Ok(Json(res))
}

#[utoipa::path(post,
    tag = "room",
    path = "/{room_id}/timing",
    description = "Update room timing",
    request_body = Timing,
    responses(
        (status = 200, body = RoomDto),
        (status = 500, body = AppErrJson, description = "Internal server error"),
        (status = 400, body = AppErrJson, description = "Invalid any data format (validation error)"),
        (status = 401, body = AppErrJson, description = "Token is missing or not verified (maybe deleted or ip changed)"),
        (status = 403, body = AppErrJson, description = "Invalid or expired token"),
        (status = 404, body = AppErrJson, description = "Room not found"),
    ),
    security(
        ("jwt" = [])
    )
)]
async fn upd_timing(
    State(app): State<App>,
    Extension(user): Extension<UserSession>,
    Path(room_id): Path<Uuid>,
    Json(tim): Json<Timing>,
) -> Result<Json<RoomDto>, AppErr> {
    tim.validate()?;

    let res = sqlx::query_as!(
        RoomDto,
        r#"
        update rooms set
            review_interval = coalesce($3, review_interval),
            expired_notify = coalesce($4, expired_notify)
        where id = $2 and permit_room($1, $2)
        returning
            id,
            display_name,
            bio,
            review_interval,
            expired_notify,
            created_at"#,
        user.user,
        room_id,
        tim.review_interval,
        tim.expired_notify
    )
    .fetch_optional(&app.pool)
    .await?
    .ok_or(AppErr::NotFound)?;

    Ok(Json(res))
}
