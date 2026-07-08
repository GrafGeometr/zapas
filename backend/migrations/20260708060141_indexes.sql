create index idx_sessions_user_id on sessions(user_id);

create index idx_members_room_id on members(room_id, user_id);

create index idx_storage_room_id on storages(room_id);

create index idx_items_storage_id on items(storage_id);

create index idx_items_history_item_id on items_history(item_id, id);

create index idx_goods_waiter_id on goods(waiter_id, executor_id);
create index idx_goods_item_id on goods(item_id, completed_at);

create index idx_transactions_from_id on transactions(from_member_id, to_member_id);
create index idx_transactions_to_id on transactions(to_member_id, from_member_id);

create index idx_purchases_member_id on purchases(member_id, created_at);
