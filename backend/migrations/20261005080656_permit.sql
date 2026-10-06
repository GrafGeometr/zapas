create or replace function permit_room(p_user_id uuid, p_room_id uuid)
    returns bool as
$$
    select exists (
        select 1
        from members
        where user_id = p_user_id and room_id = p_room_id
    );
$$ language sql stable parallel safe;
