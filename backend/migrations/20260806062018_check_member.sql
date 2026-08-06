create or replace function check_member_exists(
    p_user_id uuid,
    p_room_id uuid
) returns bool language sql stable as $$
    select exists (
        select 1 from members
        where user_id = p_user_id and room_id = p_room_id
    );
$$;


-- check_storage_member_exists
create or replace function check_sm_exists(
    p_user_id uuid,
    p_storage_id uuid
) returns bool language sql stable as $$
    select exists (
        select 1 from storages as s
        join rooms as r on s.room_id = r.id
        join members as m on m.room_id = r.id
        where p_user_id = m.user_id and p_storage_id = s.id
    );
$$;
