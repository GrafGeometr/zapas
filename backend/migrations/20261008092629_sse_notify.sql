create or replace function members_sse_notify_f()
returns trigger as
$$
declare
    payload json;
    room_id uuid;
    target_id uuid;
    event_type_val text;
begin
    event_type_val := tg_argv[0];

    if (tg_op = 'DELETE') then
        room_id := old.room_id;
        target_id := old.id;
    else
        room_id := new.room_id;
        target_id := new.id;
    end if;

    payload := json_build_object(
        'event_type', event_type_val,
        'target_id', target_id,
        'room_id', room_id
    );

    perform pg_notify('sse_notify', payload::text);
    return null;
end;
$$ language plpgsql;


create trigger members_sse_notify
after insert or update or delete on members
for each row execute function members_sse_notify_f('Member');
