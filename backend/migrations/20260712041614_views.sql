create or replace view v_members as
select
    m.id,
    m.user_id,
    m.room_id,
    coalesce(m.display_name, u.display_name, 'deleted') as display_name,
    coalesce(m.bio, u.bio, '') as bio,
    m.created_at
from members as m
left join users as u on m.user_id = u.id;
