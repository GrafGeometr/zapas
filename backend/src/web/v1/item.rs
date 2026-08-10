use crate::{
    app::App,
    err::{AppErr, AppErrJson, ToAppErr},
    web::v1::{dto::*, middleware::auth_middleware},
};
use axum::{
    Extension, Json, Router,
    extract::{Path, State},
    middleware,
    routing::{get, post},
};
use chrono::Utc;
use uuid::Uuid;
use validator::Validate;

pub fn make_app(app: App) -> Router {
    Router::new()
        .route("/{item_id}", get(get_profile))
        .route("/{item_id}/profile", post(upd_profile))
        .route("/{item_id}/timing", post(set_timing))
        .route("/{item_id}/need_review", post(need_review))
        .route("/{item_id}/review", post(review))
        .route("/{item_id}/delete", post(delete_item))
        .layer(middleware::from_fn_with_state(app.clone(), auth_middleware))
        .with_state(app)
}

#[utoipa::path(get,
    path = "/api/v1/item/{item_id}",
    tag = "items",
    responses(
        (status = 200, body = ItemDto, description = "Item profile"),
        (status = 401, body = AppErrJson, description = "Unathorized. Check prolongation"),
        (status = 403, body = AppErrJson, description = "Not a member"),
        (status = 500, body = AppErrJson, description = "Internal server error")
    ))]
pub async fn get_profile(
    State(app): State<App>,
    Extension(user_id): Extension<Uuid>,
    Path(item_id): Path<Uuid>,
) -> Result<Json<ItemDto>, AppErr> {
    let res = sqlx::query_as!(
        ItemDto,
        r#"
        select
            id,
            storage_id,
            display_name,
            bio,
            amount,
            amount_type as "amount_type: AmountType",
            review_interval,
            needed_review,
            last_review_at,
            expired_notify,
            expired_at,
            created_at
        from items as i
        where i.id = $2 and check_sm_exists($1, i.storage_id)"#,
        user_id,
        item_id
    )
    .fetch_optional(&app.pool)
    .await
    .app_log()?
    .ok_or(AppErr::NotFound)?;

    Ok(Json(res))
}

#[utoipa::path(post,
    path = "/api/v1/item/{item_id}/profile",
    tag = "items",
    request_body = UpdRoomProfileDto,
    responses(
        (status = 200, body = ItemDto, description = "Successfully updated profile"),
        (status = 401, body = AppErrJson, description = "Unathorized. Check prolongation"),
        (status = 500, body = AppErrJson, description = "Internal server error")
    ))]
pub async fn upd_profile(
    State(app): State<App>,
    Extension(user_id): Extension<Uuid>,
    Path(item_id): Path<Uuid>,
    Json(profile): Json<UpdRoomProfileDto>,
) -> Result<Json<ItemDto>, AppErr> {
    profile.validate()?;

    let res = sqlx::query_as!(
        ItemDto,
        r#"
        update items as i set
            display_name = coalesce($3, i.display_name),
            bio = coalesce($4, i.bio)
        where i.id = $2 and check_sm_exists($1, i.storage_id)
        returning
            id,
            storage_id,
            display_name,
            bio,
            amount,
            amount_type as "amount_type: AmountType",
            review_interval,
            needed_review,
            last_review_at,
            expired_notify,
            expired_at,
            created_at"#,
        user_id,
        item_id,
        profile.display_name,
        profile.bio,
    )
    .fetch_optional(&app.pool)
    .await
    .app_log()?
    .ok_or(AppErr::NotFound)?;

    Ok(Json(res))
}

#[utoipa::path(post,
    path = "/api/v1/item/{item_id}/timing",
    tag = "items",
    request_body = SetTimingDto,
    responses(
        (status = 200, body = ItemDto, description = "Successfully set timing for items"),
        (status = 401, body = AppErrJson, description = "Unathorized. Check prolongation"),
        (status = 500, body = AppErrJson, description = "Internal server error")
    ))]
pub async fn set_timing(
    State(app): State<App>,
    Extension(user_id): Extension<Uuid>,
    Path(item_id): Path<Uuid>,
    Json(profile): Json<SetTimingDto>,
) -> Result<Json<ItemDto>, AppErr> {
    profile.validate()?;

    let res = sqlx::query_as!(
        ItemDto,
        r#"
        update items as i set
            review_interval = $3,
            expired_notify = $4
        where i.id = $2 and check_sm_exists($1, i.storage_id)
        returning
            id,
            storage_id,
            display_name,
            bio,
            amount,
            amount_type as "amount_type: AmountType",
            review_interval,
            needed_review,
            last_review_at,
            expired_notify,
            expired_at,
            created_at"#,
        user_id,
        item_id,
        profile.review_interval,
        profile.expired_notify
    )
    .fetch_optional(&app.pool)
    .await
    .app_log()?
    .ok_or(AppErr::NotFound)?;

    Ok(Json(res))
}

#[utoipa::path(post,
    path = "/api/v1/item/{item_id}/need_review",
    tag = "items",
    responses(
        (status = 200, body = ItemDto, description = "Successfully set need review"),
        (status = 401, body = AppErrJson, description = "Unathorized. Check prolongation"),
        (status = 500, body = AppErrJson, description = "Internal server error")
    ))]
pub async fn need_review(
    State(app): State<App>,
    Extension(user_id): Extension<Uuid>,
    Path(item_id): Path<Uuid>,
) -> Result<Json<ItemDto>, AppErr> {
    let res = sqlx::query_as!(
        ItemDto,
        r#"
        update items as i set
            needed_review = true
        where i.id = $2 and check_sm_exists($1, i.storage_id)
        returning
            id,
            storage_id,
            display_name,
            bio,
            amount,
            amount_type as "amount_type: AmountType",
            review_interval,
            needed_review,
            last_review_at,
            expired_notify,
            expired_at,
            created_at"#,
        user_id,
        item_id
    )
    .fetch_optional(&app.pool)
    .await
    .app_log()?
    .ok_or(AppErr::NotFound)?;

    Ok(Json(res))
}

#[utoipa::path(post,
    path = "/api/v1/item/{item_id}/review",
    tag = "items",
    request_body = ReviewDto,
    responses(
        (status = 200, body = ItemDto, description = "Successfully set reviewed"),
        (status = 401, body = AppErrJson, description = "Unathorized. Check prolongation"),
        (status = 500, body = AppErrJson, description = "Internal server error")
    ))]
pub async fn review(
    State(app): State<App>,
    Extension(user_id): Extension<Uuid>,
    Path(item_id): Path<Uuid>,
    Json(review): Json<ReviewDto>,
) -> Result<Json<ItemDto>, AppErr> {
    review.validate()?;

    let res = sqlx::query_as::<_, ItemDto>(
        r#"
        update items as i set
            amount = $3,
            amount_type = $4,
            expired_at = $5,
            needed_review = false,
            last_review_at = $6
        where i.id = $2 and check_sm_exists($1, i.storage_id)
        returning
            id,
            storage_id,
            display_name,
            bio,
            amount,
            amount_type as "amount_type: AmountType",
            review_interval,
            needed_review,
            last_review_at,
            expired_notify,
            expired_at,
            created_at"#,
    )
    .bind(user_id)
    .bind(item_id)
    .bind(review.amount)
    .bind(review.amount_type)
    .bind(review.expired_at)
    .bind(Utc::now())
    .fetch_optional(&app.pool)
    .await
    .app_log()?
    .ok_or(AppErr::NotFound)?;

    Ok(Json(res))
}

#[utoipa::path(post,
    path = "/api/v1/item/{item_id}/delete",
    tag = "items",
    responses(
        (status = 200, description = "Successfully deleted item"),
        (status = 401, body = AppErrJson, description = "Unathorized. Check prolongation"),
        (status = 500, body = AppErrJson, description = "Internal server error")
    ))]
pub async fn delete_item(
    State(app): State<App>,
    Extension(user_id): Extension<Uuid>,
    Path(item_id): Path<Uuid>,
) -> Result<(), AppErr> {
    sqlx::query!(
        r#"
        delete from items
            where id = $2 and check_sm_exists($1, storage_id)
        returning id"#,
        user_id,
        item_id
    )
    .fetch_optional(&app.pool)
    .await
    .app_log()?
    .ok_or(AppErr::NotFound)?;

    Ok(())
}
