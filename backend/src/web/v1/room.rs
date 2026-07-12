use super::dto::*;
use crate::{
    app::App,
    err::{AppErr, AppErrJson, ToAppErr},
    web::v1::middlewares::auth_middleware,
};
use axum::{
    Extension, Json, Router,
    extract::{Path, Query, State},
    middleware,
    routing::{get, post},
};
use uuid::Uuid;

pub fn make_app(app: App) -> Router {
    Router::new()
        .route("/make", post(make_room))
        .route("/{room_id}", get(room_profile))
        .route("/all", get(rooms))
        .route("/{room_id}/profile", post(upd_profile))
        .route("/{room_id}/timing", post(set_room_timing))
        .route("/{room_id}/delete", post(delete_room))
        .layer(middleware::from_fn_with_state(app.clone(), auth_middleware))
        .with_state(app)
}

#[utoipa::path(post,
    path = "/api/v1/room/make",
    tag = "rooms",
    request_body = MakeRoomDto,
    responses(
        (status = 200, body = UuidDto, description = "Rooms successfully created"),
        (status = 401, body = AppErrJson, description = "Unathorized. Check prolongation"),
        (status = 500, body = AppErrJson, description = "Internal server error")
    ))]
pub async fn make_room(
    State(app): State<App>,
    Extension(user_id): Extension<Uuid>,
    Json(profile): Json<MakeRoomDto>,
) -> Result<Json<UuidDto>, AppErr> {
    let room_id = Uuid::now_v7();

    let mut tx = app.pool.begin().await.app_log()?;

    sqlx::query!(
        "insert into rooms (id, display_name, bio) values ($1, $2, $3)",
        room_id,
        profile.display_name,
        profile.bio
    )
    .execute(&mut *tx)
    .await
    .app_log()?;

    sqlx::query!(
        "insert into members (id, user_id, room_id) values ($1, $2, $3)",
        Uuid::now_v7(),
        user_id,
        room_id
    )
    .execute(&mut *tx)
    .await
    .app_log()?;

    tx.commit().await.app_log()?;

    Ok(Json(UuidDto { id: room_id }))
}

#[utoipa::path(get,
    path = "/api/v1/room/{room_id}",
    tag = "rooms",
    responses(
        (status = 200, body = UuidDto, description = "Rooms successfully created"),
        (status = 401, body = AppErrJson, description = "Unathorized. Check prolongation"),
        (status = 403, body = AppErrJson, description = "Not a member"),
        (status = 500, body = AppErrJson, description = "Internal server error")
    ))]
pub async fn room_profile(
    State(app): State<App>,
    Extension(user_id): Extension<Uuid>,
    Path(room_id): Path<Uuid>,
) -> Result<Json<RoomProfileDto>, AppErr> {
    let res = sqlx::query_as!(
        RoomProfileDb,
        r#"
        select
            r.id,
            r.display_name,
            r.bio,
            r.review_interval,
            r.expired_notify,
            r.created_at
        from rooms as r
        join members as m on m.room_id = r.id
        where m.user_id = $1 and m.room_id = $2"#,
        user_id,
        room_id
    )
    .fetch_optional(&app.pool)
    .await
    .app_log()?
    .ok_or(AppErr::PermissionDenied)?;

    Ok(Json(res.into()))
}

#[utoipa::path(get,
    path = "/api/v1/room/all",
    tag = "rooms",
    params(
        Pagination
    ),
    responses(
        (status = 200, body = UuidDto, description = "Rooms successfully created"),
        (status = 401, body = AppErrJson, description = "Unathorized. Check prolongation"),
        (status = 500, body = AppErrJson, description = "Internal server error")
    ))]
pub async fn rooms(
    State(app): State<App>,
    Extension(user_id): Extension<Uuid>,
    Query(pag): Query<Pagination>,
) -> Result<Json<Vec<RoomProfileDto>>, AppErr> {
    let res = sqlx::query_as!(
        RoomProfileDb,
        r#"
        select
            r.id,
            r.display_name,
            r.bio,
            r.review_interval,
            r.expired_notify,
            r.created_at
        from rooms as r
        join members as m on m.room_id = r.id
        where m.user_id = $1
        order by m.created_at
        offset $2 limit $3"#,
        user_id,
        pag.offset(),
        pag.limit()
    )
    .fetch_all(&app.pool)
    .await
    .app_log()?;

    Ok(Json(res.into_iter().map(|x| x.into()).collect()))
}

#[utoipa::path(post,
    path = "/api/v1/room/{room_id}/profile",
    tag = "rooms",
    request_body = UpdRoomProfileDto,
    responses(
        (status = 200, body = RoomProfileDto, description = "Successfully updated profile"),
        (status = 401, body = AppErrJson, description = "Unathorized. Check prolongation"),
        (status = 500, body = AppErrJson, description = "Internal server error")
    ))]
pub async fn upd_profile(
    State(app): State<App>,
    Extension(user_id): Extension<Uuid>,
    Path(room_id): Path<Uuid>,
    Json(profile): Json<UpdRoomProfileDto>,
) -> Result<Json<RoomProfileDto>, AppErr> {
    let res = sqlx::query_as!(
        RoomProfileDb,
        r#"
        update rooms as r set
            display_name = coalesce($1, r.display_name),
            bio = coalesce($2, r.bio)
        from members as m
        where m.user_id = $3 and m.room_id = $4 and r.id = $4
        returning
            r.id,
            r.display_name,
            r.bio,
            r.review_interval,
            r.expired_notify,
            r.created_at
        "#,
        profile.display_name,
        profile.bio,
        user_id,
        room_id
    )
    .fetch_optional(&app.pool)
    .await
    .app_log()?
    .ok_or(AppErr::PermissionDenied)?;

    Ok(Json(res.into()))
}

#[utoipa::path(post,
    path = "/api/v1/room/{room_id}/timing",
    tag = "rooms",
    request_body = SetRoomTimingDto,
    responses(
        (status = 200, body = RoomProfileDto, description = "Successfully set timing for items"),
        (status = 401, body = AppErrJson, description = "Unathorized. Check prolongation"),
        (status = 500, body = AppErrJson, description = "Internal server error")
    ))]
pub async fn set_room_timing(
    State(app): State<App>,
    Extension(user_id): Extension<Uuid>,
    Path(room_id): Path<Uuid>,
    Json(profile): Json<SetRoomTimingDto>,
) -> Result<Json<RoomProfileDto>, AppErr> {
    let res = sqlx::query_as!(
        RoomProfileDb,
        r#"
        update rooms as r set
            review_interval = $1,
            expired_notify = $2
        from members as m
        where m.user_id = $3 and m.room_id = $4 and r.id = $4
        returning
            r.id,
            r.display_name,
            r.bio,
            r.review_interval,
            r.expired_notify,
            r.created_at
        "#,
        profile.review_interval,
        profile.expired_notify,
        user_id,
        room_id
    )
    .fetch_optional(&app.pool)
    .await
    .app_log()?
    .ok_or(AppErr::PermissionDenied)?;

    Ok(Json(res.into()))
}

#[utoipa::path(post,
    path = "/api/v1/room/{room_id}/delete",
    tag = "rooms",
    responses(
        (status = 200, description = "Successfully deleted room"),
        (status = 401, body = AppErrJson, description = "Unathorized. Check prolongation"),
        (status = 500, body = AppErrJson, description = "Internal server error")
    ))]
pub async fn delete_room(
    State(_app): State<App>,
    Extension(_user_id): Extension<Uuid>,
    Path(_room_id): Path<Uuid>,
) -> Result<(), AppErr> {
    Err(AppErr::NotImplemented)
}
