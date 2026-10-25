use crate::{
    domain::{pag::PagArg, room::*},
    err::AppErr,
};
use uuid::Uuid;

pub trait RoomRepo {
    fn new_room(&self, room: MakeRoom) -> impl Future<Output = Result<Room, AppErr>> + Send;

    fn delete_room(&self, id: Uuid) -> impl Future<Output = Result<Option<()>, AppErr>> + Send;

    fn upd(&self, id: Uuid, upd: UpdRoom) -> impl Future<Output = Result<Room, AppErr>> + Send;

    fn upd_timing(
        &self,
        id: Uuid,
        upd: UpdTiming,
    ) -> impl Future<Output = Result<Room, AppErr>> + Send;

    fn get_by_id(&self, id: Uuid) -> impl Future<Output = Result<Option<Room>, AppErr>> + Send;

    fn list_rooms(
        &self,
        user: Uuid,
        pag: PagArg,
    ) -> impl Future<Output = Result<Vec<Room>, AppErr>> + Send;
}

pub mod pg {
    use super::*;
    use sqlx::PgPool;

    #[derive(Clone)]
    pub struct RoomRepoPg {
        pool: PgPool,
    }

    impl RoomRepoPg {
        pub fn new(pool: PgPool) -> Self {
            Self { pool }
        }
    }

    impl RoomRepo for RoomRepoPg {
        async fn new_room(&self, val: MakeRoom) -> Result<Room, AppErr> {
            let mut tx = self.pool.begin().await?;

            let room = sqlx::query_as!(
                Room,
                r#"
                insert into rooms (
                    id,
                    display_name,
                    bio
                ) values (
                    $1, $2, $3
                ) returning
                    id,
                    display_name,
                    bio,
                    review_interval,
                    expired_notify,
                    created_at"#,
                Uuid::now_v7(),
                val.display_name,
                val.bio
            )
            .fetch_one(&mut *tx)
            .await?;

            sqlx::query!(
                r#"
                insert into members (
                    id,
                    user_id,
                    room_id
                ) values (
                    $1, $2, $3
                )"#,
                Uuid::now_v7(),
                val.user,
                room.id
            )
            .execute(&mut *tx)
            .await?;

            tx.commit().await?;
            Ok(room)
        }

        async fn delete_room(&self, id: Uuid) -> Result<Option<()>, AppErr> {
            let cnt = sqlx::query!("delete from rooms where id = $1", id)
                .execute(&self.pool)
                .await?
                .rows_affected();

            Ok(if cnt == 0 { None } else { Some(()) })
        }

        async fn get_by_id(&self, id: Uuid) -> Result<Option<Room>, AppErr> {
            let res = sqlx::query_as!(
                Room,
                r#"
                select
                    id,
                    display_name,
                    bio,
                    review_interval,
                    expired_notify,
                    created_at
                from rooms where id = $1"#,
                id
            )
            .fetch_optional(&self.pool)
            .await?;
            Ok(res)
        }

        async fn list_rooms(&self, user: Uuid, pag: PagArg) -> Result<Vec<Room>, AppErr> {
            let res = sqlx::query_as!(
                Room,
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
                order by created_at desc
                offset $2 limit $3"#,
                user,
                pag.offset,
                pag.limit
            )
            .fetch_all(&self.pool)
            .await?;
            Ok(res)
        }

        async fn upd(&self, id: Uuid, upd: UpdRoom) -> Result<Room, AppErr> {
            let res = sqlx::query_as!(
                Room,
                r#"
                update rooms set
                    display_name = coalesce($2, display_name),
                    bio = coalesce($3, bio)
                where id = $1
                returning
                    id,
                    display_name,
                    bio,
                    review_interval,
                    expired_notify,
                    created_at"#,
                id,
                upd.display_name,
                upd.bio
            )
            .fetch_optional(&self.pool)
            .await?
            .ok_or(AppErr::NotFound)?;
            Ok(res)
        }

        async fn upd_timing(&self, id: Uuid, upd: UpdTiming) -> Result<Room, AppErr> {
            let res = sqlx::query_as!(
                Room,
                r#"
                update rooms set
                    review_interval = coalesce($2, review_interval),
                    expired_notify = coalesce($3, expired_notify)
                where id = $1
                returning
                    id,
                    display_name,
                    bio,
                    review_interval,
                    expired_notify,
                    created_at"#,
                id,
                upd.review_interval,
                upd.expired_notify
            )
            .fetch_optional(&self.pool)
            .await?
            .ok_or(AppErr::NotFound)?;
            Ok(res)
        }
    }
}
