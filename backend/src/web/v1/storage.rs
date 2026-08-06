use crate::{
    app::App,
    err::{AppErr, AppErrJson, ToAppErr},
    web::v1::{dto::*, middleware::auth_middleware},
};
use axum::{
    Extension, Json, Router,
    extract::{Path, Query, State},
    middleware::{self},
    routing::{get, post},
};
use chrono::Utc;
use uuid::Uuid;
use validator::Validate;

pub fn make_app(app: App) -> Router {
    Router::new()
        .route("/{storage_id}", get(storage_profile))
        .route("/{storage_id}/profile", post(upd_profile))
        .route("/{storage_id}/timing", post(set_storage_timing))
        .route("/{storage_id}/delete", post(delete_storage))
        .route("/{storage_id}/new_item", post(make_item))
        .route("/{storage_id}/items", get(all_items))
        .layer(middleware::from_fn_with_state(app.clone(), auth_middleware))
        .with_state(app)
}

#[utoipa::path(get,
    path = "/api/v1/storage/{storage_id}",
    tag = "rooms",
    responses(
        (status = 200, body = RoomProfileDto, description = "Storage profile"),
        (status = 401, body = AppErrJson, description = "Unathorized. Check prolongation"),
        (status = 403, body = AppErrJson, description = "Not a member"),
        (status = 500, body = AppErrJson, description = "Internal server error")
    ))]
pub async fn storage_profile(
    State(app): State<App>,
    Extension(user_id): Extension<Uuid>,
    Path(storage_id): Path<Uuid>,
) -> Result<Json<StorageDto>, AppErr> {
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
        where s.id = $2 and exists (
            select 1
            from members as m
            where m.user_id = $1 and m.room_id = s.room_id
        )"#,
        user_id,
        storage_id
    )
    .fetch_optional(&app.pool)
    .await
    .app_log()?
    .ok_or(AppErr::NotFound)?;

    Ok(Json(res.into()))
}

#[utoipa::path(post,
    path = "/api/v1/storage/{storage_id}/profile",
    tag = "storages",
    request_body = UpdStorageProfileDto,
    responses(
        (status = 200, body = StorageDto, description = "Successfully updated profile"),
        (status = 401, body = AppErrJson, description = "Unathorized. Check prolongation"),
        (status = 500, body = AppErrJson, description = "Internal server error")
    ))]
pub async fn upd_profile(
    State(app): State<App>,
    Extension(user_id): Extension<Uuid>,
    Path(storage_id): Path<Uuid>,
    Json(profile): Json<UpdStorageProfileDto>,
) -> Result<Json<StorageDto>, AppErr> {
    profile.validate()?;

    let res = sqlx::query_as!(
        StorageDb,
        r#"
        update storages as s set
            display_name = coalesce($1, s.display_name),
            bio = coalesce($2, s.bio)
        from members as m
        where m.user_id = $3 and m.room_id = s.room_id and s.id = $4
        returning
            s.id,
            s.room_id,
            s.display_name,
            s.bio,
            s.review_interval,
            s.expired_notify,
            s.created_at"#,
        profile.display_name,
        profile.bio,
        user_id,
        storage_id
    )
    .fetch_optional(&app.pool)
    .await
    .app_log()?
    .ok_or(AppErr::NotFound)?;

    Ok(Json(res.into()))
}

#[utoipa::path(post,
    path = "/api/v1/storage/{storage_id}/timing",
    tag = "storages",
    request_body = SetTimingDto,
    responses(
        (status = 200, body = StorageDto, description = "Successfully set timing for items"),
        (status = 401, body = AppErrJson, description = "Unathorized. Check prolongation"),
        (status = 500, body = AppErrJson, description = "Internal server error")
    ))]
pub async fn set_storage_timing(
    State(app): State<App>,
    Extension(user_id): Extension<Uuid>,
    Path(storage_id): Path<Uuid>,
    Json(profile): Json<SetTimingDto>,
) -> Result<Json<StorageDto>, AppErr> {
    profile.validate()?;

    let res = sqlx::query_as!(
        StorageDb,
        r#"
        update storages as s set
            review_interval = $1,
            expired_notify = $2
        from members as m
        where m.user_id = $3 and m.room_id = s.room_id and s.id = $4
        returning
            s.id,
            s.room_id,
            s.display_name,
            s.bio,
            s.review_interval,
            s.expired_notify,
            s.created_at"#,
        profile.review_interval,
        profile.expired_notify,
        user_id,
        storage_id
    )
    .fetch_optional(&app.pool)
    .await
    .app_log()?
    .ok_or(AppErr::NotFound)?;

    Ok(Json(res.into()))
}

#[utoipa::path(post,
    path = "/api/v1/storage/{storage_id}/delete",
    tag = "storages",
    responses(
        (status = 200, description = "Successfully deleted storage"),
        (status = 401, body = AppErrJson, description = "Unathorized. Check prolongation"),
        (status = 500, body = AppErrJson, description = "Internal server error")
    ))]
pub async fn delete_storage(
    State(app): State<App>,
    Extension(user_id): Extension<Uuid>,
    Path(storage_id): Path<Uuid>,
) -> Result<(), AppErr> {
    sqlx::query!(
        r#"
        delete from storages as s where s.id = $2 and exists (
            select 1
            from members as m
            where m.user_id = $1 and m.room_id = s.room_id
        )
        returning s.id"#,
        user_id,
        storage_id,
    )
    .fetch_optional(&app.pool)
    .await
    .app_log()?
    .ok_or(AppErr::NotFound)?;

    Ok(())
}

#[utoipa::path(post,
    path = "/api/v1/storage/{storage_id}/new_item",
    tag = "items",
    request_body = NewItemDto,
    responses(
        (status = 200, body = UuidDto, description = "Item successfully created"),
        (status = 401, body = AppErrJson, description = "Unathorized. Check prolongation"),
        (status = 500, body = AppErrJson, description = "Internal server error")
    ))]
pub async fn make_item(
    State(app): State<App>,
    Extension(user_id): Extension<Uuid>,
    Path(storage_id): Path<Uuid>,
    Json(profile): Json<NewItemDto>,
) -> Result<Json<UuidDto>, AppErr> {
    profile.validate()?;

    let res = sqlx::query_as!(
        UuidDto,
        r#"
        insert into items (
            id,
            storage_id,
            display_name,
            bio,
            amount,
            amount_type,
            needed_review
        ) select
            $3,
            $2,
            $4,
            $5,
            0,
            'none',
            false
        from storages as s
        where check_member_exists($1, s.room_id)
        returning id
        "#,
        user_id,
        storage_id,
        Uuid::now_v7(),
        profile.display_name,
        profile.bio,
    )
    .fetch_optional(&app.pool)
    .await
    .app_log()?
    .ok_or(AppErr::NotFound)?;

    Ok(Json(res))
}

#[utoipa::path(get,
    path = "/api/v1/storage/{storage_id}/items",
    tag = "items",
    params(
        ItemFindPagination
    ),
    responses(
        (status = 200, body = [ItemDto], description = "List of items in storage"),
        (status = 401, body = AppErrJson, description = "Unathorized. Check prolongation"),
        (status = 404, body = AppErrJson, description = "Storage not found or user is not a member"),
        (status = 500, body = AppErrJson, description = "Internal server error")
    ))]
pub async fn all_items(
    State(app): State<App>,
    Extension(user_id): Extension<Uuid>,
    Path(storage_id): Path<Uuid>,
    Query(pag): Query<ItemFindPagination>,
) -> Result<Json<Vec<ItemDto>>, AppErr> {
    use crate::domain::search::*;
    pag.validate()?;

    let mut res = sqlx::query_as!(
        ItemDto,
        r#"
        select
            id,
            storage_id,
            display_name,
            bio,
            amount,
            amount_type,
            review_interval,
            needed_review,
            last_review_at,
            expired_notify,
            expired_at,
            created_at
        from items as i
        where i.storage_id = storage_id and check_sm_exists($1, $2)"#,
        user_id,
        storage_id
    )
    .fetch_all(&app.pool)
    .await
    .app_log()?;

    match pag.filter {
        ItemFindFilter::All => {}

        ItemFindFilter::NoneOnly => {
            res = res
                .into_iter()
                .filter(|x| x.amount_type == "none")
                .collect();
        }

        ItemFindFilter::NotNoneOnly => {
            res = res
                .into_iter()
                .filter(|x| x.amount_type != "none")
                .collect();
        }

        ItemFindFilter::ReviewOnly => res = res.into_iter().filter(|x| x.needed_review).collect(),

        ItemFindFilter::ExpiredOnly => {
            let now = Utc::now();
            res = res
                .into_iter()
                .filter(|x| {
                    if let Some(exp) = x.expired_at {
                        now >= exp
                    } else {
                        false
                    }
                })
                .collect();
        }
    }

    sort_array(&pag.search, &mut res);
    if res.len() > pag.limit as usize {
        res.truncate(pag.limit as usize);
    }

    Ok(Json(res))
}
