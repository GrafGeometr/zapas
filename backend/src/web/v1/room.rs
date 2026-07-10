use super::dto::*;
use crate::{
    app::App,
    err::{AppErr, ToAppErr},
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
        .layer(middleware::from_fn_with_state(app.clone(), auth_middleware))
        .with_state(app)
}

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
