use crate::{
    domain::{member::*, pag::PagArg},
    err::AppErr,
};
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
    ) -> impl Future<Output = Result<Vec<Member>, AppErr>> + Send;

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
}
