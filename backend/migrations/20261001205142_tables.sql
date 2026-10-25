create table users (
    id uuid not null,
    username text not null,
    email text null default null,

    verified_email bool not null default false,

    display_name display_name_t not null,
    bio bio_t not null default '',

    passwd_hash text not null,

    created_at timestamptz not null default now(),

    constraint users_pk primary key (id),

    constraint users_username_unique unique (username),
    constraint users_username_check check (
        4 <= length(username) and length(username) < 64 and
        username ~ '^[0-9a-z_]+$'
    ),

    constraint users_email_unique unique (email),
    constraint users_email_valid check (
        email is null or (
            email like '%@%' and length(email) < 256
        )
    ),
    constraint users_verified_email check (email is not null or not verified_email)
);


create table sessions (
    id uuid not null,

    user_id uuid not null,

    ver bigint not null,

    ip_addr inet not null,
    last_active_at timestamptz not null default now(),

    created_at timestamptz not null default now(),

    constraint sessions_pk primary key (id),

    constraint sessions_user_id_fk foreign key
        (user_id)
    references users(id)
        on delete cascade,

    constraint sessions_time_check check (last_active_at >= created_at)
);

create index idx_sessions_user_id on sessions(user_id);


create table rooms (
    id uuid not null,

    display_name display_name_t not null,
    bio bio_t not null default '',

    review_interval duration_t not null default 0,
    expired_notify duration_t not null default 0,

    created_at timestamptz not null default now(),

    constraint rooms_pk primary key (id)
);


create table members (
    id uuid not null,

    user_id uuid not null,
    room_id uuid not null,

    display_name display_name_t null default null,
    bio bio_t null default null,

    default_portions bigint not null default 0,

    created_at timestamptz not null default now(),

    constraint members_pk primary key (id),

    constraint members_user_id_fk foreign key
        (user_id)
    references users(id)
        on delete cascade,
    constraint members_room_id_fk foreign key
        (room_id)
    references rooms(id)
        on delete cascade,

    constraint members_unique_room_id_and_id unique (room_id, id),

    constraint members_default_portions_check check (default_portions >= 0)
);


create unique index idx_unique_members_user_room on members(user_id, room_id) where user_id is not null;


create table portions (
    member_id uuid not null,
    portion_date date not null,

    cnt bigint not null,

    constraint portions_pk primary key (member_id, portion_date),

    constraint portions_member_id_fk foreign key
        (member_id)
    references members(id)
        on delete cascade
);


create table storages (
    id uuid not null,

    room_id uuid not null,

    display_name display_name_t not null,
    bio bio_t not null default '',

    review_interval duration_t null default null,
    expired_notify duration_t null default null,

    created_at timestamptz not null default now(),

    constraint storages_pk primary key (id),

    constraint storages_room_id_fk foreign key
        (room_id)
    references rooms(id)
        on delete cascade,

    constraint storages_unique_room_id_and_id unique (room_id, id),

    constraint storages_unique_name unique (room_id, display_name)
);


create table items (
    id uuid not null,

    room_id uuid not null,
    storage_id uuid not null,

    display_name display_name_t not null,
    bio bio_t not null default '',

    amount bigint not null,
    amount_type amount_t not null,

    review_interval duration_t null default null,
    needed_review bool not null default false,
    last_review_at timestamptz not null default now(),

    expired_notify duration_t null default null,
    expired_at timestamptz null default null,

    created_at timestamptz not null default now(),

    constraint items_pk primary key (id),

    constraint items_room_id_storage_id_fk foreign key
        (room_id, storage_id)
    references storages(room_id, id)
        on delete cascade,

    constraint items_unique_room_id_and_id unique (room_id, id),

    constraint items_amount_check check (
        (amount > 0 and amount_type not in ('none', 'few', 'mid', 'many'))
        or
        amount = 0
    ),

    constraint items_unique_name unique (storage_id, display_name)
);

create index idx_items_room_id_storage_id on items(room_id, storage_id);
create index idx_items_unreviewed on items(room_id) where needed_review = true;



create table goods (
    id uuid not null,

    room_id uuid not null,
    waiter_id uuid not null,
    executor_id uuid null default null,

    item_id uuid not null,

    bio bio_t not null default '',

    amount bigint not null,
    amount_type amount_t not null,

    completed_at timestamptz null default null,
    created_at timestamptz not null default now(),

    constraint goods_pk primary key (id),

    constraint goods_room_id_waiter_id_fk foreign key
        (room_id, waiter_id)
    references members(room_id, id)
        on delete cascade,
    constraint goods_room_id_executor_id_fk foreign key
        (room_id, executor_id)
    references members(room_id, id)
        on delete cascade,

    constraint goods_room_id_item_id_fk foreign key
        (room_id, item_id)
    references items(room_id, id)
        on delete cascade,

    constraint goods_amount_check check (
        (amount > 0 and amount_type not in ('none', 'few', 'mid', 'many'))
        or
        amount = 0
    ),

    constraint goods_time_check check (completed_at is null or completed_at >= created_at),

    constraint goods_uncompleted_check check (
        (executor_id is null and completed_at is null)
        or
        (executor_id is not null and completed_at is not null)
    )
);

create index idx_goods_room_id_waiter_id on goods(room_id, waiter_id);
create index idx_goods_room_id_executor_id on goods(room_id, executor_id) where executor_id is not null;
create index idx_goods_uncompleted on goods(room_id) where completed_at is null;


create table transactions (
    id uuid not null,

    room_id uuid not null,
    from_member_id uuid not null,
    to_member_id uuid not null,

    money bigint not null,
    bio bio_t not null default '',

    created_at timestamptz not null default now(),

    constraint transactions_pk primary key (id),

    constraint transactions_room_id_from_member_id_fk foreign key
        (room_id, from_member_id)
    references members(room_id, id)
        on delete cascade,
    constraint transactions_room_id_to_member_id_fk foreign key
        (room_id, to_member_id)
    references members(room_id, id)
        on delete cascade,

    constraint transactions_money_check check (money > 0),

    constraint transactions_for_self check (from_member_id != to_member_id)
);

create index idx_transactions_room_id_from_member_id on transactions(room_id, from_member_id);
create index idx_transactions_room_id_to_member_id on transactions(room_id, to_member_id);


create table purchases (
    id uuid not null,

    room_id uuid not null,
    member_id uuid not null,

    money bigint not null, -- Может быть отрицательным - значит заработок, а не трата.
    bio bio_t not null default '',

    is_food bool not null,

    created_at timestamptz not null default now(),

    constraint purchases_pk primary key (id),

    constraint purchases_room_id_member_id_fk foreign key
        (room_id, member_id)
    references members(room_id, id)
        on delete cascade,

    constraint purchases_not_zero check (money != 0)
);

create index idx_purchases_room_id_member_id on purchases(room_id, member_id);
create index idx_purchases_room_id_created_at on purchases(room_id, created_at);
