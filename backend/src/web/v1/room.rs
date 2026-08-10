use super::dto::*;
use crate::{
    app::App,
    err::{AppErr, AppErrJson, ToAppErr},
    web::v1::middleware::auth_middleware,
};
use axum::{
    Extension, Json, Router,
    extract::{Path, Query, State},
    middleware,
    routing::{get, post},
};
use chrono::Duration;
use uuid::Uuid;
use validator::Validate;

pub fn make_app(app: App) -> Router {
    Router::new()
        .route("/make", post(make_room))
        .route("/{room_id}", get(room_profile))
        .route("/all", get(rooms))
        .route("/{room_id}/profile", post(upd_profile))
        .route("/{room_id}/timing", post(set_room_timing))
        .route("/{room_id}/delete", post(delete_room))
        .route("/{room_id}/members", get(all_members))
        .route("/{room_id}/invite/gen", post(gen_invite))
        .route("/invite/{token}", post(invite))
        .route("/{room_id}/new_storage", post(make_storage))
        .route("/{room_id}/storages", get(all_storages))
        .route("/{room_id}/items", get(all_items))
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
    profile.validate()?;

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
        (status = 200, body = RoomProfileDto, description = "Rooms successfully created"),
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
    .ok_or(AppErr::NotFound)?;

    Ok(Json(res.into()))
}

#[utoipa::path(get,
    path = "/api/v1/room/all",
    tag = "rooms",
    params(
        Pagination
    ),
    responses(
        (status = 200, body = [UuidDto], description = "List of rooms"),
        (status = 401, body = AppErrJson, description = "Unathorized. Check prolongation"),
        (status = 500, body = AppErrJson, description = "Internal server error")
    ))]
pub async fn rooms(
    State(app): State<App>,
    Extension(user_id): Extension<Uuid>,
    Query(pag): Query<Pagination>,
) -> Result<Json<Vec<RoomProfileDto>>, AppErr> {
    pag.validate()?;

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
    profile.validate()?;

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
    .ok_or(AppErr::NotFound)?;

    Ok(Json(res.into()))
}

#[utoipa::path(post,
    path = "/api/v1/room/{room_id}/timing",
    tag = "rooms",
    request_body = SetTimingDto,
    responses(
        (status = 200, body = RoomProfileDto, description = "Successfully set timing for items"),
        (status = 401, body = AppErrJson, description = "Unathorized. Check prolongation"),
        (status = 500, body = AppErrJson, description = "Internal server error")
    ))]
pub async fn set_room_timing(
    State(app): State<App>,
    Extension(user_id): Extension<Uuid>,
    Path(room_id): Path<Uuid>,
    Json(profile): Json<SetTimingDto>,
) -> Result<Json<RoomProfileDto>, AppErr> {
    profile.validate()?;

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
    .ok_or(AppErr::NotFound)?;

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

#[utoipa::path(get,
    path = "/api/v1/room/{room_id}/members",
    tag = "rooms",
    params(
        Pagination
    ),
    responses(
        (status = 200, body = [MemberDto], description = "List of room memberships"),
        (status = 401, body = AppErrJson, description = "Unathorized. Check prolongation"),
        (status = 404, body = AppErrJson, description = "Room not found or user is not a member"),
        (status = 500, body = AppErrJson, description = "Internal server error")
    ))]
pub async fn all_members(
    State(app): State<App>,
    Extension(user_id): Extension<Uuid>,
    Path(room_id): Path<Uuid>,
    Query(pag): Query<Pagination>,
) -> Result<Json<Vec<MemberDto>>, AppErr> {
    pag.validate()?;

    let res = sqlx::query_as!(
        MemberDto,
        r#"
        select
            v.id as "id!",
            v.user_id,
            v.room_id as "room_id!",
            v.display_name as "display_name!",
            v.bio as "bio!",
            v.created_at as "created_at!"
        from v_members as v
        where v.room_id = $2 and exists (
            select 1 from members where user_id = $1 and room_id = $2
        )
        order by v.created_at
        offset $3 limit $4
        "#,
        user_id,
        room_id,
        pag.offset(),
        pag.limit()
    )
    .fetch_all(&app.pool)
    .await
    .app_log()?;

    if res.is_empty() {
        return Err(AppErr::NotFound);
    }

    Ok(Json(res))
}

#[utoipa::path(post,
    path = "/api/v1/room/{room_id}/invite/gen",
    tag = "rooms",
    responses(
        (status = 200, body = TokenDto, description = "Invite token"),
        (status = 401, body = AppErrJson, description = "Unathorized. Check prolongation"),
        (status = 404, body = AppErrJson, description = "Room not found or user is not a member"),
        (status = 500, body = AppErrJson, description = "Internal server error")
    ))]
pub async fn gen_invite(
    State(app): State<App>,
    Extension(user_id): Extension<Uuid>,
    Path(room_id): Path<Uuid>,
) -> Result<Json<TokenDto>, AppErr> {
    sqlx::query!(
        "select id from members where user_id = $1 and room_id = $2",
        user_id,
        room_id
    )
    .fetch_optional(&app.pool)
    .await
    .app_log()?
    .ok_or(AppErr::NotFound)?;

    let token = app.jwt.gen_sl_invite(room_id, Duration::minutes(10))?;

    Ok(Json(TokenDto { token }))
}

#[utoipa::path(post,
    path = "/api/v1/room/invite/{token}",
    tag = "rooms",
    responses(
        (status = 200, body = UuidDto, description = "Successfully joined the room"),
        (status = 401, body = AppErrJson, description = "Unathorized. Check prolongation"),
        (status = 409, body = AppErrJson, description = "User is already a member or room not found"),
        (status = 500, body = AppErrJson, description = "Internal server error")
    ))]
pub async fn invite(
    State(app): State<App>,
    Extension(user_id): Extension<Uuid>,
    Path(token): Path<String>,
) -> Result<Json<UuidDto>, AppErr> {
    let token = app.jwt.parse_sl_invite(&token)?;

    sqlx::query!(
        r#"insert into members (id, user_id, room_id) values ($1, $2, $3)
        on conflict (user_id, room_id) where user_id is not null do nothing
        returning id"#,
        Uuid::now_v7(),
        user_id,
        token.room
    )
    .fetch_optional(&app.pool)
    .await
    .app_log()?
    .ok_or(AppErr::Exists)?;

    Ok(Json(UuidDto { id: token.room }))
}

#[utoipa::path(post,
    path = "/api/v1/room/{room_id}/new_storage",
    tag = "storages",
    request_body = MakeStorageDto,
    responses(
        (status = 200, body = UuidDto, description = "Storage successfully created"),
        (status = 401, body = AppErrJson, description = "Unathorized. Check prolongation"),
        (status = 500, body = AppErrJson, description = "Internal server error")
    ))]
pub async fn make_storage(
    State(app): State<App>,
    Extension(user_id): Extension<Uuid>,
    Path(room_id): Path<Uuid>,
    Json(profile): Json<MakeStorageDto>,
) -> Result<Json<UuidDto>, AppErr> {
    profile.validate()?;

    let res = sqlx::query!(
        r#"
        insert into storages (id, room_id, display_name, bio)
        select $1, m.room_id, $2, $3
        from members as m
        where m.user_id = $4 and m.room_id = $5
        returning id
        "#,
        Uuid::now_v7(),
        profile.display_name,
        profile.bio,
        user_id,
        room_id
    )
    .fetch_optional(&app.pool)
    .await
    .app_log()?
    .ok_or(AppErr::NotFound)?;

    Ok(Json(UuidDto { id: res.id }))
}

#[utoipa::path(get,
    path = "/api/v1/room/{room_id}/storages",
    tag = "storages",
    params(
        Pagination
    ),
    responses(
        (status = 200, body = [StorageDto], description = "List of room's storages"),
        (status = 401, body = AppErrJson, description = "Unathorized. Check prolongation"),
        (status = 404, body = AppErrJson, description = "Room not found or user is not a member"),
        (status = 500, body = AppErrJson, description = "Internal server error")
    ))]
pub async fn all_storages(
    State(app): State<App>,
    Extension(user_id): Extension<Uuid>,
    Path(room_id): Path<Uuid>,
    Query(pag): Query<Pagination>,
) -> Result<Json<Vec<StorageDto>>, AppErr> {
    pag.validate()?;

    let res = sqlx::query_as!(
        StorageDb,
        r#"
        select
            s.id,
            s.room_id,
            s.display_name,
            s.bio,
            s.review_interval,
            s.expired_notify,
            s.created_at
        from storages as s
        where s.room_id = $2 and exists (
            select 1
            from members as m
            where m.user_id = $1 and m.room_id = $2
        order by s.created_at
        offset $3 limit $4
        )"#,
        user_id,
        room_id,
        pag.offset(),
        pag.limit()
    )
    .fetch_all(&app.pool)
    .await
    .app_log()?;

    Ok(Json(res.into_iter().map(|x| x.into()).collect()))
}

#[utoipa::path(get,
    path = "/api/v1/room/{room_id}/items",
    tag = "items",
    params(
        ItemFindPagination
    ),
    responses(
        (status = 200, body = [ItemDto], description = "List of items in room"),
        (status = 401, body = AppErrJson, description = "Unathorized. Check prolongation"),
        (status = 404, body = AppErrJson, description = "Storage not found or user is not a member"),
        (status = 500, body = AppErrJson, description = "Internal server error")
    ))]
pub async fn all_items(
    State(app): State<App>,
    Extension(user_id): Extension<Uuid>,
    Path(room_id): Path<Uuid>,
    Query(pag): Query<ItemFindPagination>,
) -> Result<Json<Vec<ItemDto>>, AppErr> {
    pag.validate()?;

    let res = sqlx::query_as!(
        ItemDto,
        r#"
        select
            i.id,
            i.storage_id,
            i.display_name,
            i.bio,
            i.amount,
            i.amount_type as "amount_type: AmountType",
            i.review_interval,
            i.needed_review,
            i.last_review_at,
            i.expired_notify,
            i.expired_at,
            i.created_at
        from items as i
        join storages as s on s.id = i.storage_id
        where s.room_id = $2 and check_member_exists($1, $2)
        "#,
        user_id,
        room_id
    )
    .fetch_all(&app.pool)
    .await
    .app_log()?;
    let res = pag.filter(res);

    Ok(Json(res))
}
