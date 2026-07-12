use super::dto::*;
use crate::{
    app::App,
    err::{AppErr, AppErrJson, ToAppErr},
    web::v1::middlewares::auth_middleware,
};
use axum::{
    Extension, Json, Router,
    extract::{Path, State},
    middleware,
    routing::{get, post},
};
use uuid::Uuid;
use validator::Validate;

pub fn make_app(app: App) -> Router {
    Router::new()
        .route("/{member_id}", get(get_profile))
        .route("/{member_id}/origin", get(get_origin_profile))
        .route("/{member_id}/delete", post(delete))
        .route("/{member_id}/profile", post(set_profile))
        .route("/{member_id}/default_portions", get(get_def_portions))
        .route("/{member_id}/default_portions", post(set_def_portions))
        .layer(middleware::from_fn_with_state(app.clone(), auth_middleware))
        .with_state(app)
}

#[utoipa::path(get,
    path = "/api/v1/member/{member_id}",
    tag = "members",
    responses(
        (status = 200, body = MemberDto, description = "Retrieved member profile with fallback to global user info"),
        (status = 401, body = AppErrJson, description = "Unathorized. Check prolongation"),
        (status = 404, body = AppErrJson, description = "Member not found or you are not in the same room"),
        (status = 500, body = AppErrJson, description = "Internal server error")
    ))]
pub async fn get_profile(
    State(app): State<App>,
    Extension(user_id): Extension<Uuid>,
    Path(member_id): Path<Uuid>,
) -> Result<Json<MemberDto>, AppErr> {
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
        from v_members as v where v.id = $1 and exists(
            select 1 from members as m where m.room_id = v.room_id and m.user_id = $2
        )
        "#,
        member_id,
        user_id
    )
    .fetch_optional(&app.pool)
    .await
    .app_log()?
    .ok_or(AppErr::NotFound)?;

    Ok(Json(res))
}

#[utoipa::path(get,
    path = "/api/v1/member/{member_id}/origin",
    tag = "members",
    responses(
        (status = 200, body = MemberOriginDto, description = "Retrieved raw member profile"),
        (status = 401, body = AppErrJson, description = "Unathorized. Check prolongation"),
        (status = 404, body = AppErrJson, description = "Member not found or you are not in the same room"),
        (status = 500, body = AppErrJson, description = "Internal server error")
    ))]
pub async fn get_origin_profile(
    State(app): State<App>,
    Extension(user_id): Extension<Uuid>,
    Path(member_id): Path<Uuid>,
) -> Result<Json<MemberOriginDto>, AppErr> {
    let res = sqlx::query_as!(
        MemberOriginDto,
        r#"
        select
            v.id,
            v.user_id,
            v.room_id,
            v.display_name,
            v.bio,
            v.created_at
        from members as v where v.id = $1 and exists(
            select 1 from members as m where m.room_id = v.room_id and m.user_id = $2
        )
        "#,
        member_id,
        user_id
    )
    .fetch_optional(&app.pool)
    .await
    .app_log()?
    .ok_or(AppErr::NotFound)?;

    Ok(Json(res))
}

#[utoipa::path(post,
    path = "/api/v1/member/{member_id}/delete",
    tag = "members",
    responses(
        (status = 200),
        (status = 401, body = AppErrJson, description = "Unathorized. Check prolongation"),
        (status = 404, body = AppErrJson, description = "Member not found or you are not in the same room"),
        (status = 500, body = AppErrJson, description = "Internal server error")
    ))]
pub async fn delete(
    State(_app): State<App>,
    Extension(_user_id): Extension<Uuid>,
    Path(_member_id): Path<Uuid>,
) -> Result<(), AppErr> {
    Err(AppErr::NotImplemented)
}

#[utoipa::path(post,
    path = "/api/v1/member/{member_id}/profile",
    tag = "members",
    request_body = SetMemberProfileDto,
    responses(
        (status = 200, body = MemberDto, description = ""),
        (status = 401, body = AppErrJson, description = "Unathorized. Check prolongation"),
        (status = 404, body = AppErrJson, description = "Member not found or you are not in the same room"),
        (status = 500, body = AppErrJson, description = "Internal server error")
    ))]
pub async fn set_profile(
    State(app): State<App>,
    Extension(user_id): Extension<Uuid>,
    Path(member_id): Path<Uuid>,
    Json(profile): Json<SetMemberProfileDto>,
) -> Result<Json<MemberDto>, AppErr> {
    profile.validate()?;

    let res = sqlx::query_as!(
        MemberDto,
        r#"
        with upd_member as (
            update members set
                display_name = $1,
                bio = $2
            where id = $3 and user_id = $4 and user_id is not null
            returning id
        )
        select
            v.id as "id!",
            v.user_id,
            v.room_id as "room_id!",
            v.display_name as "display_name!",
            v.bio as "bio!",
            v.created_at as "created_at!"
        from v_members as v
        join upd_member as um on v.id = um.id
        "#,
        profile.display_name,
        profile.bio,
        member_id,
        user_id
    )
    .fetch_optional(&app.pool)
    .await
    .app_log()?
    .ok_or(AppErr::NotFound)?;

    Ok(Json(res))
}

#[utoipa::path(get,
    path = "/api/v1/member/{member_id}/default_portions",
    tag = "members",
    responses(
        (status = 200, body = PortionsDto, description = ""),
        (status = 401, body = AppErrJson, description = "Unathorized. Check prolongation"),
        (status = 404, body = AppErrJson, description = "Member not found or you are not in the same room"),
        (status = 500, body = AppErrJson, description = "Internal server error")
    ))]
pub async fn get_def_portions(
    State(app): State<App>,
    Extension(user_id): Extension<Uuid>,
    Path(member_id): Path<Uuid>,
) -> Result<Json<PortionsDto>, AppErr> {
    let res = sqlx::query!(
        r#"
        select v.default_portions from members as v
        where v.id = $1 and exists (
            select 1 from members as m where m.room_id = v.room_id and m.user_id = $2
        )"#,
        member_id,
        user_id
    )
    .fetch_optional(&app.pool)
    .await
    .app_log()?
    .ok_or(AppErr::NotFound)?;

    Ok(Json(PortionsDto {
        portions: res.default_portions,
    }))
}

#[utoipa::path(post,
    path = "/api/v1/member/{member_id}/default_portions",
    tag = "members",
    request_body = PortionsDto,
    responses(
        (status = 200, body = PortionsDto, description = ""),
        (status = 401, body = AppErrJson, description = "Unathorized. Check prolongation"),
        (status = 404, body = AppErrJson, description = "Member not found or you are not in the same room"),
        (status = 500, body = AppErrJson, description = "Internal server error")
    ))]
pub async fn set_def_portions(
    State(app): State<App>,
    Extension(user_id): Extension<Uuid>,
    Path(member_id): Path<Uuid>,
    Json(portions): Json<PortionsDto>,
) -> Result<(), AppErr> {
    portions.validate()?;

    sqlx::query!(
        r#"
        update members as v set
            default_portions = $1
        where v.id = $2 and exists(
            select 1 from members as m where m.room_id = v.room_id and m.user_id = $3
        )
        returning v.id"#,
        portions.portions,
        member_id,
        user_id
    )
    .fetch_optional(&app.pool)
    .await
    .app_log()?
    .ok_or(AppErr::NotFound)?;

    Ok(())
}
