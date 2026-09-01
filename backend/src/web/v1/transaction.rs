use crate::{
    app::App,
    err::{AppErr, AppErrJson, ToAppErr},
    web::v1::{dto::TransactionDto, middleware::auth_middleware},
};
use axum::{
    Extension, Json, Router,
    extract::{Path, State},
    middleware,
    routing::post,
};
use uuid::Uuid;

pub fn make_app(app: App) -> Router {
    Router::new()
        .route("/{transaction_id}/delete", post(delete_transaction))
        .layer(middleware::from_fn_with_state(app.clone(), auth_middleware))
        .with_state(app)
}

#[utoipa::path(get,
    path = "/api/v1/transaction/{transaction_id}",
    tag = "transactions",
    responses(
        (status = 200, body = TransactionDto, description = "..."),
        (status = 401, body = AppErrJson, description = "Unathorized. Check prolongation"),
        (status = 500, body = AppErrJson, description = "Internal server error")
    ))]
pub async fn get_transaction(
    State(app): State<App>,
    Extension(user_id): Extension<Uuid>,
    Path(transaction_id): Path<Uuid>,
) -> Result<Json<TransactionDto>, AppErr> {
    let res = sqlx::query_as!(
        TransactionDto,
        r#"
        select
            t.id,
            t.from_member_id as member_from,
            t.to_member_id as member_to,
            t.money,
            t.bio,
            t.created_at
        from transactions as t
        where t.id = $2 and exists (
            select 1 from members as m
            where
                m.user_id = $1 and
                (m.id = t.from_member_id or m.id = t.to_member_id)
        )"#,
        user_id,
        transaction_id
    )
    .fetch_optional(&app.pool)
    .await
    .app_log()?
    .ok_or(AppErr::NotFound)?;

    Ok(Json(res))
}

#[utoipa::path(post,
    path = "/api/v1/transaction/{transaction_id}/delete",
    tag = "transactions",
    responses(
        (status = 200, description = "..."),
        (status = 401, body = AppErrJson, description = "Unathorized. Check prolongation"),
        (status = 500, body = AppErrJson, description = "Internal server error")
    ))]
pub async fn delete_transaction(
    State(app): State<App>,
    Extension(user_id): Extension<Uuid>,
    Path(transaction_id): Path<Uuid>,
) -> Result<(), AppErr> {
    sqlx::query!(
        r#"
        delete from transactions as t where t.id = $1 and exists (
            select 1 from members as m
            where
                m.user_id = $2 and
                (m.id = t.from_member_id or m.id = t.to_member_id)
        )
        returning t.id"#,
        transaction_id,
        user_id
    )
    .fetch_optional(&app.pool)
    .await
    .app_log()?
    .ok_or(AppErr::NotFound)?;
    Ok(())
}
