use crate::app::App;
use std::time::Duration;
use tracing::{error, info};

pub async fn item_review_timer(app: App) {
    loop {
        tokio::time::sleep(Duration::from_secs(600)).await;

        let res = sqlx::query!(
            r#"
            with for_review as (
                select
                    i.id,
                    coalesce(
                        i.review_interval,
                        s.review_interval,
                        r.review_interval,
                        0
                    ) as review_interval,
                    i.last_review_at
                from items as i
                join storages as s on i.storage_id = s.id
                join rooms as r on s.room_id = r.id
                where not i.needed_review
            )
            update items as i set
                needed_review = true
            from for_review as f
            where
                f.id = i.id and
                f.review_interval != 0 and
                f.last_review_at + (f.review_interval || ' seconds')::interval <= now()"#
        )
        .execute(&app.pool)
        .await;

        match res {
            Err(e) => {
                error!("Error update item review: {}", e);
            }
            Ok(cnt) => {
                info!("Update {} items review", cnt.rows_affected());
            }
        }
    }
}
