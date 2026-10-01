create domain display_name_t as text
    constraint domain_display_name_check
        check (1 <= length(value) and length(value) < 128);


create domain bio_t as text
    constraint domain_bio_check
        check (length(value) < 1024);


-- null -> не задано (брать значение с уровня выше), 0 -> значения нет, иначе значение.
create domain duration_t as bigint
    constraint domain_duration_check
        check (value >= 0);


create type amount_t as enum ('none', 'few', 'mid', 'many', 'g', 'ml', 'pcs');
