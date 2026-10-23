use crate::{
    domain::{auth::*, pag::PagArg, passwd::*},
    err::AppErr,
};
use std::net::IpAddr;
use uuid::Uuid;

pub trait SessionRepo {
    fn new_session(
        &self,
        username: &str,
        passwd: &str,
        ip: IpAddr,
    ) -> impl Future<Output = Result<RefreshToken, AppErr>> + Send;

    fn delete_session(&self, id: Uuid) -> impl Future<Output = Result<Option<()>, AppErr>> + Send;

    fn delete_all_sessions(
        &self,
        user: Uuid,
    ) -> impl Future<Output = Result<Option<()>, AppErr>> + Send;

    fn list_sessions(
        &self,
        user: Uuid,
        pag: PagArg,
    ) -> impl Future<Output = Result<Vec<Session>, AppErr>> + Send;

    fn get_by_id(&self, id: Uuid) -> impl Future<Output = Result<Option<Session>, AppErr>> + Send;

    fn upd(&self, session: Session) -> impl Future<Output = Result<(), AppErr>> + Send;
}

pub mod pg {
    use std::net::IpAddr;

    use super::*;
    use ipnetwork::IpNetwork;
    use sqlx::PgPool;

    #[derive(Clone)]
    pub struct SessionRepoPg {
        pool: PgPool,
    }

    impl SessionRepoPg {
        pub fn new(pool: PgPool) -> Self {
            Self { pool }
        }
    }

    impl SessionRepo for SessionRepoPg {
        async fn new_session(
            &self,
            username: &str,
            passwd: &str,
            ip: IpAddr,
        ) -> Result<RefreshToken, AppErr> {
            let ip_net = IpNetwork::new(ip, 32).expect("Invalid ip");

            let mut tx = self.pool.begin().await?;

            let val = sqlx::query!(
                "select id, passwd_hash from users where username = $1",
                username
            )
            .fetch_optional(&mut *tx)
            .await?
            .ok_or(AppErr::BadUsername)?;

            check_passwd(&val.passwd_hash, passwd)?;
            let (s, token) = Session::new(val.id, ip);

            sqlx::query!(
                r#"
                insert into sessions (
                    id,
                    user_id,
                    ver,
                    ip_addr
                ) values (
                    $1,
                    $2,
                    $3,
                    $4
                )"#,
                s.id,
                s.user,
                s.ver,
                ip_net
            )
            .execute(&mut *tx)
            .await?;

            tx.commit().await?;
            Ok(token)
        }

        async fn delete_session(&self, id: Uuid) -> Result<Option<()>, AppErr> {
            let cnt = sqlx::query!("delete from sessions where id = $1", id)
                .execute(&self.pool)
                .await?
                .rows_affected();

            Ok(if cnt != 0 { Some(()) } else { None })
        }

        async fn delete_all_sessions(&self, user: Uuid) -> Result<Option<()>, AppErr> {
            sqlx::query!("delete from sessions where user_id = $1", user)
                .execute(&self.pool)
                .await?;
            Ok(Some(())) // TODO add check user exists
        }

        async fn get_by_id(&self, id: Uuid) -> Result<Option<Session>, AppErr> {
            let res = sqlx::query!(
                r#"
                select
                    id,
                    user_id,
                    ver,
                    ip_addr,
                    last_active_at,
                    created_at
                from sessions
                where id = $1"#,
                id
            )
            .fetch_optional(&self.pool)
            .await?
            .map(|x| {
                Session::restore(
                    x.id,
                    x.user_id,
                    x.ver,
                    x.ip_addr,
                    x.last_active_at,
                    x.created_at,
                )
            });

            Ok(res)
        }

        async fn list_sessions(&self, user: Uuid, pag: PagArg) -> Result<Vec<Session>, AppErr> {
            let res = sqlx::query!(
                r#"
                select
                    id,
                    user_id,
                    ver,
                    ip_addr,
                    last_active_at,
                    created_at
                from sessions
                where user_id = $1
                order by last_active_at desc
                offset $2 limit $3"#,
                user,
                pag.offset,
                pag.limit
            )
            .fetch_all(&self.pool)
            .await?
            .into_iter()
            .map(|x| {
                Session::restore(
                    x.id,
                    x.user_id,
                    x.ver,
                    x.ip_addr,
                    x.last_active_at,
                    x.created_at,
                )
            })
            .collect();

            Ok(res)
        }

        async fn upd(&self, session: Session) -> Result<(), AppErr> {
            if session.need_delete() {
                self.delete_session(session.id).await?;
            } else if session.need_upd() {
                let ip = IpNetwork::new(session.ip, 32).expect("Invalid ip");

                sqlx::query!(
                    r#"
                    update sessions set
                        ver = $2,
                        ip_addr = $3,
                        last_active_at = $4
                    where id = $1"#,
                    session.id,
                    session.ver,
                    ip,
                    session.last_active_at
                )
                .execute(&self.pool)
                .await?;
            }

            Ok(())
        }
    }
}
