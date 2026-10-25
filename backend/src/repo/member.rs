use crate::{
    domain::{member::*, pag::PagArg},
    err::AppErr,
};
use chrono::NaiveDate;
use std::collections::HashSet;
use uuid::Uuid;

pub trait MemberRepo {
    fn join_to_room(
        &self,
        room: Uuid,
        user: Uuid,
    ) -> impl Future<Output = Result<Member, AppErr>> + Send;

    fn get_by_id(&self, id: Uuid) -> impl Future<Output = Result<Option<Member>, AppErr>> + Send;

    fn list_members(
        &self,
        room: Uuid,
        pag: PagArg,
    ) -> impl Future<Output = Result<Vec<Member>, AppErr>> + Send;

    fn list_members_without_pag(
        &self,
        room: Uuid,
    ) -> impl Future<Output = Result<HashSet<Member>, AppErr>> + Send;

    fn permit_room(
        &self,
        room: Uuid,
        user: Uuid,
    ) -> impl Future<Output = Result<bool, AppErr>> + Send;

    fn is_neighbor(
        &self,
        member: Uuid,
        user: Uuid,
    ) -> impl Future<Output = Result<bool, AppErr>> + Send;

    fn set_default_portions(
        &self,
        id: Uuid,
        portions: i64,
    ) -> impl Future<Output = Result<Member, AppErr>> + Send;

    fn get_portions(
        &self,
        id: Uuid,
        l: NaiveDate,
        r: NaiveDate,
    ) -> impl Future<Output = Result<Vec<PortionWithDate>, AppErr>> + Send;
}

pub mod pg {
    use super::*;
    use sqlx::PgPool;

    #[derive(Clone)]
    pub struct MemberRepoPg {
        pool: PgPool,
    }

    impl MemberRepoPg {
        pub fn new(pool: PgPool) -> Self {
            Self { pool }
        }
    }

    impl MemberRepo for MemberRepoPg {
        async fn get_by_id(&self, id: Uuid) -> Result<Option<Member>, AppErr> {
            let res = sqlx::query_as!(
                Member,
                r#"
                select
                    m.id,
                    m.user_id,
                    m.room_id,
                    coalesce(m.display_name, u.display_name) as "display_name!",
                    coalesce(m.bio, u.bio) as "bio!",
                    m.default_portions,
                    m.created_at
                from members as m
                join users as u on m.user_id = u.id
                where m.id = $1"#,
                id
            )
            .fetch_optional(&self.pool)
            .await?;
            Ok(res)
        }

        async fn is_neighbor(&self, member: Uuid, user: Uuid) -> Result<bool, AppErr> {
            let res = sqlx::query_scalar!(
                r#"
                select
                    m.room_id
                from members as m
                where
                    m.id = $1
                        and
                    exists (
                        select 1 from members as v
                        where v.user_id = $2 and v.room_id = m.room_id
                    )"#,
                member,
                user
            )
            .fetch_optional(&self.pool)
            .await?;

            Ok(res.is_some())
        }

        async fn join_to_room(&self, room: Uuid, user: Uuid) -> Result<Member, AppErr> {
            let res = sqlx::query_as!(
                Member,
                r#"
                with inserted as (
                    insert into members (
                        id,
                        user_id,
                        room_id
                    ) values (
                        $1,
                        $3,
                        $2
                    ) returning
                        id,
                        user_id,
                        room_id,
                        display_name,
                        bio,
                        default_portions,
                        created_at
                )
                select
                    i.id,
                    i.user_id,
                    i.room_id,
                    coalesce(i.display_name, u.display_name) as "display_name!",
                    coalesce(i.bio, u.bio) as "bio!",
                    i.default_portions,
                    i.created_at
                from inserted as i
                join users as u on i.user_id = u.id"#,
                Uuid::now_v7(),
                room,
                user
            )
            .fetch_one(&self.pool)
            .await?;

            Ok(res)
        }

        async fn list_members(&self, room: Uuid, pag: PagArg) -> Result<Vec<Member>, AppErr> {
            let res = sqlx::query_as!(
                Member,
                r#"
                select
                    m.id,
                    m.user_id,
                    m.room_id,
                    coalesce(m.display_name, u.display_name) as "display_name!",
                    coalesce(m.bio, u.bio) as "bio!",
                    m.default_portions,
                    m.created_at
                from members as m
                join users as u on m.user_id = u.id
                where m.room_id = $1
                order by m.created_at desc
                offset $2 limit $3"#,
                room,
                pag.offset,
                pag.limit
            )
            .fetch_all(&self.pool)
            .await?;
            Ok(res)
        }

        async fn list_members_without_pag(&self, room: Uuid) -> Result<HashSet<Member>, AppErr> {
            let res = sqlx::query_as!(
                Member,
                r#"
                select
                    m.id,
                    m.user_id,
                    m.room_id,
                    coalesce(m.display_name, u.display_name) as "display_name!",
                    coalesce(m.bio, u.bio) as "bio!",
                    m.default_portions,
                    m.created_at
                from members as m
                join users as u on m.user_id = u.id
                where m.room_id = $1"#,
                room,
            )
            .fetch_all(&self.pool)
            .await?
            .into_iter()
            .collect();
            Ok(res)
        }

        async fn permit_room(&self, room: Uuid, user: Uuid) -> Result<bool, AppErr> {
            let res = sqlx::query_scalar!(
                "select id from members where room_id = $1 and user_id = $2",
                room,
                user
            )
            .fetch_optional(&self.pool)
            .await?;
            Ok(res.is_some())
        }

        async fn get_portions(
            &self,
            id: Uuid,
            l: NaiveDate,
            r: NaiveDate,
        ) -> Result<Vec<PortionWithDate>, AppErr> {
            let res = sqlx::query_as!(
                PortionWithDate,
                r#"
                select
                    cnt,
                    portion_date as date
                from portions
                where
                    member_id = $1 and
                    $2::date <= portion_date and
                    portion_date <= $3::date"#,
                id,
                l,
                r
            )
            .fetch_all(&self.pool)
            .await?;
            Ok(res)
        }

        async fn set_default_portions(&self, id: Uuid, portions: i64) -> Result<Member, AppErr> {
            let res = sqlx::query_as!(
                Member,
                r#"
                with updated as (
                    update members set
                        default_portions = $2
                    where id = $1
                    returning
                        id,
                        user_id,
                        room_id,
                        display_name,
                        bio,
                        default_portions,
                        created_at
                )
                select
                    i.id,
                    i.user_id,
                    i.room_id,
                    coalesce(i.display_name, u.display_name) as "display_name!",
                    coalesce(i.bio, u.bio) as "bio!",
                    i.default_portions,
                    i.created_at
                from updated as i
                join users as u on u.id = i.user_id"#,
                id,
                portions
            )
            .fetch_one(&self.pool)
            .await?;
            Ok(res)
        }
    }
}
