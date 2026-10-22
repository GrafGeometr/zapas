use crate::{
    domain::{pag::PagArg, passwd::*, user::*},
    err::AppErr,
};
use chrono::{DateTime, Utc};
use uuid::Uuid;

pub trait UserRepo {
    fn new_user(&self, val: MakeUser) -> impl Future<Output = Result<User, AppErr>> + Send;

    fn delete_user(&self, id: Uuid) -> impl Future<Output = Result<Option<()>, AppErr>> + Send;

    fn upd(&self, id: Uuid, upd: UpdUser) -> impl Future<Output = Result<User, AppErr>> + Send;

    fn reset_passwd(
        &self,
        id: Uuid,
        new_passwd_hash: &str,
    ) -> impl Future<Output = Result<(), AppErr>> + Send;

    fn upd_passwd(
        &self,
        id: Uuid,
        old_passwd: &str,
        new_passwd_hash: &str,
    ) -> impl Future<Output = Result<(), AppErr>> + Send;

    /// Return true if changed
    fn set_email(&self, id: Uuid, email: &str)
    -> impl Future<Output = Result<bool, AppErr>> + Send;

    fn get_by_id(&self, id: Uuid) -> impl Future<Output = Result<Option<User>, AppErr>> + Send;

    fn list_users(&self, pag: PagArg) -> impl Future<Output = Result<Vec<User>, AppErr>> + Send;

    fn last_active_at(
        &self,
        id: Uuid,
    ) -> impl Future<Output = Result<Option<DateTime<Utc>>, AppErr>> + Send;

    fn get_by_username(
        &self,
        username: &str,
    ) -> impl Future<Output = Result<Option<User>, AppErr>> + Send;

    fn get_by_email(
        &self,
        email: &str,
    ) -> impl Future<Output = Result<Option<User>, AppErr>> + Send;
}

pub mod pg {
    use super::*;
    use sqlx::PgPool;

    #[derive(Clone)]
    pub struct UserRepoPg {
        pool: PgPool,
    }

    impl UserRepoPg {
        pub fn new(pool: PgPool) -> Self {
            Self { pool }
        }
    }

    impl UserRepo for UserRepoPg {
        async fn new_user(&self, val: MakeUser) -> Result<User, AppErr> {
            let user = sqlx::query_as!(
                User,
                r#"
                insert into users (
                    id,
                    username,
                    email,
                    passwd_hash,
                    display_name,
                    bio
                ) values (
                    $1, $2, $3, $4, $5, $6
                )
                returning
                    id,
                    username,
                    email,
                    verified_email,
                    display_name,
                    bio,
                    created_at
                "#,
                val.id,
                val.username,
                val.email,
                val.passwd_hash,
                val.display_name,
                val.bio
            )
            .fetch_one(&self.pool)
            .await
            .map_err(|err| {
                if let Some(e) = err.as_database_error() {
                    if e.message().contains("users_username_unique") {
                        AppErr::AlreadyExists("username")
                    } else if e.message().contains("users_email_unique") {
                        AppErr::AlreadyExists("email")
                    } else {
                        err.into()
                    }
                } else {
                    err.into()
                }
            })?;

            Ok(user)
        }

        // TODO check sqlx errors
        async fn delete_user(&self, id: Uuid) -> Result<Option<()>, AppErr> {
            let cnt = sqlx::query!("delete from users where id = $1", id)
                .execute(&self.pool)
                .await?
                .rows_affected();

            Ok(if cnt > 0 { Some(()) } else { None })
        }

        async fn upd(&self, id: Uuid, upd: UpdUser) -> Result<User, AppErr> {
            let res = sqlx::query_as!(
                User,
                r#"
                update users set
                    username = coalesce($2, username),
                    display_name = coalesce($3, display_name),
                    bio = coalesce($4, bio)
                where id = $1
                    returning
                    id,
                    username,
                    email,
                    verified_email,
                    display_name,
                    bio,
                    created_at"#,
                id,
                upd.username,
                upd.display_name,
                upd.bio
            )
            .fetch_optional(&self.pool)
            .await
            .map_err(|err| {
                if let Some(e) = err.as_database_error() {
                    if e.message().contains("users_username_unique") {
                        AppErr::AlreadyExists("username")
                    } else if e.message().contains("users_email_unique") {
                        AppErr::AlreadyExists("email")
                    } else {
                        err.into()
                    }
                } else {
                    err.into()
                }
            })?
            .ok_or(AppErr::NotFound)?;

            Ok(res)
        }

        async fn reset_passwd(&self, id: Uuid, new_passwd_hash: &str) -> Result<(), AppErr> {
            let mut tx = self.pool.begin().await?;

            sqlx::query!(
                "update users set passwd_hash = $2 where id = $1 returning id",
                id,
                new_passwd_hash
            )
            .fetch_optional(&mut *tx)
            .await?
            .ok_or(AppErr::NotFound)?;

            sqlx::query!("delete from sessions where user_id = $1", id)
                .execute(&mut *tx)
                .await?;

            tx.commit().await?;
            Ok(())
        }

        async fn upd_passwd(
            &self,
            id: Uuid,
            old_passwd: &str,
            new_passwd_hash: &str,
        ) -> Result<(), AppErr> {
            let mut tx = self.pool.begin().await?;

            let passwd_hash = sqlx::query!(
                r#"
                select
                    passwd_hash
                from users
                where id = $1
                for update"#,
                id
            )
            .fetch_optional(&mut *tx)
            .await?
            .ok_or(AppErr::NotFound)?
            .passwd_hash;

            check_passwd(&passwd_hash, old_passwd)?;

            sqlx::query!(
                r#"
                update users set
                    passwd_hash = $2
                where id = $1"#,
                id,
                new_passwd_hash,
            )
            .execute(&mut *tx)
            .await?;

            sqlx::query!("delete from sessions where user_id = $1", id)
                .execute(&mut *tx)
                .await?;

            tx.commit().await?;
            Ok(())
        }

        async fn get_by_id(&self, id: Uuid) -> Result<Option<User>, AppErr> {
            let res = sqlx::query_as!(
                User,
                r#"
                select
                    id,
                    username,
                    email,
                    verified_email,
                    display_name,
                    bio,
                    created_at
                from users
                where id = $1"#,
                id
            )
            .fetch_optional(&self.pool)
            .await?;

            Ok(res)
        }

        async fn get_by_username(&self, username: &str) -> Result<Option<User>, AppErr> {
            let res = sqlx::query_as!(
                User,
                r#"
                select
                    id,
                    username,
                    email,
                    verified_email,
                    display_name,
                    bio,
                    created_at
                from users
                where username = $1"#,
                username
            )
            .fetch_optional(&self.pool)
            .await?;

            Ok(res)
        }

        async fn get_by_email(&self, email: &str) -> Result<Option<User>, AppErr> {
            let res = sqlx::query_as!(
                User,
                r#"
                select
                    id,
                    username,
                    email,
                    verified_email,
                    display_name,
                    bio,
                    created_at
                from users
                where email = $1"#,
                email
            )
            .fetch_optional(&self.pool)
            .await?;

            Ok(res)
        }

        async fn list_users(&self, pag: PagArg) -> Result<Vec<User>, AppErr> {
            let res = sqlx::query_as!(
                User,
                r#"
                select
                    id,
                    username,
                    email,
                    verified_email,
                    display_name,
                    bio,
                    created_at
                from users
                order by created_at asc
                offset $1 limit $2"#,
                pag.offset,
                pag.limit
            )
            .fetch_all(&self.pool)
            .await?;

            Ok(res)
        }

        async fn last_active_at(&self, id: Uuid) -> Result<Option<DateTime<Utc>>, AppErr> {
            let res = sqlx::query!("select created_at from users where id = $1", id)
                .fetch_optional(&self.pool)
                .await?;

            Ok(res.map(|x| x.created_at))
        }

        async fn set_email(&self, id: Uuid, email: &str) -> Result<bool, AppErr> {
            let equal = sqlx::query!(
                r#"
                update users set
                    email = $2,
                    verified_email = (email != $2)
                where id = $1
                returning verified_email"#,
                id,
                email
            )
            .fetch_optional(&self.pool)
            .await?
            .ok_or(AppErr::NotFound)?
            .verified_email;

            Ok(!equal)
        }
    }
}
