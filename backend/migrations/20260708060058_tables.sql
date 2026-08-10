-- Таблица с пользователями.
create table users (
    id uuid not null,
    username text not null,
    email text null default null, -- Можно и без почты.

    verified_email bool not null default false, -- Прошла ли почта верификацию (false в том числе если почты нет).

    display_name text not null,
    bio text not null,

    passwd_hash text not null, -- argon2 хеш пароля.

    created_at timestamptz not null default now(),

    constraint users_pk primary key (id),
    constraint users_username_unique unique (username),
    constraint users_email_unique unique (email),
    constraint users_email_valid check (email is null or (email like '%@%' and length(email) < 256)),
    constraint users_verified_email check (email is not null or not verified_email),
    constraint users_username_check check (4 <= length(username) and length(username) < 64 and username ~ '^[0-9a-z_]+$'),
    constraint users_display_name_check check (1 <= length(display_name) and length(display_name) < 128),
    constraint users_bio_check check (length(bio) < 1024)
);


-- Таблица с активными сессиями (ввели логин и пароль) пользователей.
create table sessions (
    id uuid not null,

    user_id uuid not null,

    token_id bigint not null, -- id токена пользователя (обновляется при продлении сессии).
    last_token_id bigint null, -- предыдущий токен (существет при попытке продления).
    ip_addr text not null, -- ip устройства с сессией.

    created_at timestamptz not null default now(),
    last_active_at timestamptz not null default now(), -- Последнее время, когда сессия была в одном из запросов к api.

    constraint sessions_pk primary key (id),
    constraint sessions_user_id_fk foreign key (user_id) references users(id) on delete cascade,
    constraint sessions_time_check check (last_active_at >= created_at)
);
-- Логика работы token_id.
-- Необходима при проблемах с интернетом.
-- Токен живет некоторое ограниченное время. Изначально токен получается по логину и паролю. Дальше его нужно периодически продлевать
-- (а точнее получать новый на основе старого). Для этого отправляется соотв. запрос. Сервер может создать новый токен,
-- но вот овтвет сервера может не дойти до пользователя. Если запретить после этого использовать старый токен, то
-- пользователю придется заново вводить пароль. Если же разрешить, то злоумышленник может использовать его в неограниченном количестве.
-- Тогда идея. При таком запросе мы сохраняем старый токен и пишем новый. Если новый хоть раз используется - стираем старый, больше
-- его использовать нельзя. Если же просят опять токен от старого, то честно создаем новый (то есть обновляем toke_id, а last_token_id остается).
-- Теперь тот, кто получил прошлый новый токен и попытается его использовать вызовет просто удаление всей сессии.
-- Тем самым после сотни попыток создать новый токен на основе одного и того же старого "выживет" и будет использоваться только 1.
-- При нарушении правила просто удаляем сессию (тем самым выходит как обычный пользователь, так и злоумышленник).


-- Комнаты (логическое объединение нескольких людей для деления общих расходов).
create table rooms (
    id uuid not null,

    display_name text not null,
    bio text not null,

    review_interval bigint null, -- null -> не задано, 0 -> нет, иначе значение.
    expired_notify bigint null, -- null -> не задано, 0 -> нет, иначе значение.

    created_at timestamptz not null default now(),

    constraint rooms_pk primary key (id),
    constraint rooms_display_name_check check (1 <= length(display_name) and length(display_name) < 128),
    constraint rooms_bio_check check (length(bio) < 1024),
    constraint rooms_review_interval_check check (review_interval >= 0),
    constraint rooms_expired_notify_check check (expired_notify >= 0)
);


-- Хранилища внутри комнаты.
create table storages (
    id uuid not null,

    room_id uuid not null,

    display_name text not null,
    bio text not null,

    review_interval bigint null, -- null -> не задано, 0 -> нет, иначе значение.
    expired_notify bigint null, -- null -> не задано, 0 -> нет, иначе значение.

    created_at timestamptz not null default now(),

    constraint storages_pk primary key (id),
    constraint storages_unique_name unique (room_id, display_name),
    constraint storages_room_id_fk foreign key (room_id) references rooms(id) on delete cascade,
    constraint storages_review_interval_check check (review_interval >= 0),
    constraint storages_expired_notify_check check (expired_notify >= 0),
    constraint storages_display_name_check check (1 <= length(display_name) and length(display_name) < 128),
    constraint storages_bio_check check (length(bio) < 1024)
);


create type amount_tp as enum ('none', 'few', 'mid', 'many', 'g', 'ml', 'pcs');


-- Продукты внутри комнаты.
create table items (
    id uuid not null,

    storage_id uuid not null,

    display_name text not null,
    bio text not null,


    amount bigint not null,
    amount_type amount_tp not null,

    review_interval bigint null, -- null -> default, 0 -> нет, иначе значение.
    needed_review bool not null,
    last_review_at timestamptz not null,

    expired_notify bigint null, -- null -> default, 0 -> нет, иначе значение.
    expired_at timestamptz null,

    created_at timestamptz not null default now(),

    constraint items_pk primary key (id),
    constraint items_unique_name unique (storage_id, display_name),
    constraint items_storage_id_fk foreign key (storage_id) references storages(id) on delete cascade,
    constraint items_amount_check check ((amount > 0 and amount_type not in ('none', 'few', 'mid', 'many')) or amount = 0),
    constraint items_amount_type_enum check (amount_type in ('g', 'ml', 'pcs', 'none', 'few', 'mid', 'many')),
    constraint items_review_interval_check check (review_interval >= 0),
    constraint items_expired_notify_check check (expired_notify >= 0),
    constraint items_display_name_check check (1 <= length(display_name) and length(display_name) < 64),
    constraint items_bio_check check (length(bio) < 1024)
);


-- Пользователи конкретной комнаты.
create table members (
    id uuid not null,

    user_id uuid null, -- Если null, значит пользователя нет, осталась только его история.
    room_id uuid not null, -- Пользователя без комнаты нет.

    display_name text null default null, -- Локальное имя пользователя внутри одной комнаты.
    bio text null default null, -- Аналогично display_name.

    default_portions bigint not null default 0, -- Стандартное число порций каждый день.

    precalc_portions bigint not null default 0, -- Число порций.
    precalc_food_purchases bigint not null default 0, -- Затраты на еду.
    precalc_nf_purchases bigint not null default 0, -- Затраты на не еду.
    precalc_transactions bigint not null default 0, -- Итог по переводам.

    created_at timestamptz not null default now(),

    constraint members_pk primary key (id),
    constraint members_user_id_fk foreign key (user_id) references users(id) on delete set null,
    constraint members_room_id_fk foreign key (room_id) references rooms(id) on delete cascade,
    constraint members_default_portions_check check (default_portions >= 0),
    constraint members_precalc_portions_check check (precalc_portions >= 0),
    constraint members_display_name_check check ((1 <= length(display_name) and length(display_name) < 64) or display_name is null),
    constraint members_bio_check check ((length(bio) < 1024) or bio is null)
);
-- Такая логика с user_id позволяет хранить историю покупок, трат и тд внутри комнаты (что крайне важно для расчетов).
-- При этом не нужно обрабатывать случаи, когда member_id == null - таких просто нет. Для пользователей такой
-- meber будет подписан что-то вида "Удаленный пользователь".
--
-- precalc - подсчет суммы от с самого начала по конец прошлого месяца. В конце каждого месяца полностью пересчитывается.


create unique index members_user_room_unique on members(user_id, room_id) where user_id is not null;
-- Нужно для того, чтобы гарантированно сделать пары (user_id, room_id) уникальными, при этом не запрещать одновременно
-- много удаленных пользователей внутри одной комнаты.


-- Система порций пользователей. Позволяет понимать, кто сколько съел еды.
create table portions (
    member_id uuid not null, -- Пользователь, чья порция указана.
    portion_date date not null, -- День, для которого указано число порций.

    cnt bigint not null,

    constraint portions_pk primary key (member_id, portion_date),
    constraint portions_member_id_fk foreign key (member_id) references members(id) on delete cascade,
    constraint portions_cnt_check check (cnt >= 0)
);


-- Хранит изменения у каждого продукта, а именно: количество, необходимость ревью и просроченность.
create table items_history (
    id uuid not null, -- В том числе версия за счет uuidv7.
    item_id uuid not null,

    amount bigint not null,
    amount_type text not null,

    needed_review bool not null,
    expired bool not null,

    created_at timestamptz not null default now(),

    constraint items_history_pk primary key (id),
    constraint items_history_item_id_fk foreign key (item_id) references items(id) on delete cascade,
    constraint items_history_amount_check check ((amount > 0 and amount_type not in ('none', 'few', 'mid', 'many')) or amount = 0),
    constraint items_history_amount_type_enum check (amount_type in ('g', 'ml', 'pcs', 'none', 'few', 'mid', 'many')) -- TODO
);


-- Список покупок.
create table goods (
    id uuid not null,

    waiter_id uuid not null, -- Кто добавил в список.
    executor_id uuid null, -- Кто отметил как выполненное (исполнитель). null если не исполнено.
    item_id uuid not null, -- Что нужно купить.

    bio text not null, -- Коментарий.

    amount bigint not null, -- Сколько нужно купить.
    amount_type text not null,

    completed_at timestamptz null, -- Время, когда помечено как исполнено. null  если не исполнено.
    created_at timestamptz not null default now(),

    constraint goods_pk primary key (id),
    constraint goods_waiter_id_fk foreign key (waiter_id) references members(id) on delete cascade,
    constraint goods_executor_id_fk foreign key (executor_id) references members(id) on delete cascade, -- В целом без разницы, потому что все members удаляются вместе с комнатой.
    constraint goods_item_id_fk foreign key (item_id) references items(id) on delete cascade,
    constraint goods_amount_check check ((amount > 0 and amount_type not in ('none', 'few', 'mid', 'many')) or amount = 0),
    constraint goods_amount_type_enum check (amount_type in ('g', 'ml', 'pcs', 'none', 'few', 'mid', 'many')), -- TODO
    constraint goods_time_check check (completed_at is null or completed_at >= created_at),
    constraint goods_bio_check check (length(bio) < 1024),
    constraint goods_uncompleted_check check ((executor_id is null and completed_at is null) or (executor_id is not null and completed_at is not null))
);


-- Переводы между пользователями.
create table transactions (
    id uuid not null,

    from_member_id uuid not null,
    to_member_id uuid not null,

    money bigint not null,
    bio text not null,

    created_at timestamptz not null default now(),

    constraint transactions_pk primary key (id),
    constraint transactions_from_member_id_fk foreign key (from_member_id) references members(id) on delete cascade,
    constraint transactions_to_member_id_fk foreign key (to_member_id) references members(id) on delete cascade,
    constraint transactions_money_check check (money > 0),
    constraint transactions_bio_check check (length(bio) < 1024),
    constraint transactions_for_self check (from_member_id != to_member_id)
);


-- Траты пользователей во благо комнаты.
create table purchases (
    id uuid not null,

    member_id uuid not null,

    money bigint not null, -- Может быть отрицательным - значит заработок, а не трата.
    bio text not null,

    is_food bool not null, -- Трата по еде (учитываются порции) или нет.

    created_at timestamptz not null default now(),

    constraint purchases_pk primary key (id),
    constraint purchases_member_id_fk foreign key (member_id) references members(id) on delete cascade,
    constraint purchases_bio_check check (length(bio) < 1024),
    constraint purchases_not_zero check (money != 0)
);
