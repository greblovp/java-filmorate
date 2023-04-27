insert into genre (name) values ('Комедия');
insert into genre (name) values ('Драма');
insert into genre (name) values ('Мультфильм');
insert into genre (name) values ('Триллер');
insert into genre (name) values ('Документальный');
insert into genre (name) values ('Боевик');

insert into rating (name) values ('G');
insert into rating (name) values ('PG');
insert into rating (name) values ('PG-13');
insert into rating (name) values ('R');
insert into rating (name) values ('NC-17');


insert into film (name, description, release_dt, duration, rating_id)
values ('1 film', '1 film desc', '2022-01-01', 180, 1);
insert into film (name, description, release_dt, duration, rating_id)
values ('2 film', '2 film desc', '2021-01-01', 120, 2);
insert into film (name, description, release_dt, duration, rating_id)
values ('3 film', '3 film desc', '2020-01-01', 150, 3);

insert into film_x_genre (film_id, genre_id)
values (1, 3);
insert into film_x_genre (film_id, genre_id)
values (2, 1);
insert into film_x_genre (film_id, genre_id)
values (3, 2);
insert into film_x_genre (film_id, genre_id)
values (3, 4);
insert into film_x_genre (film_id, genre_id)
values (3, 5);

insert into "user" (email, login, name, birth_dt)
values ('user1@yandex.ru', 'user1', 'first user', '2000-01-01');
insert into "user" (email, login, name, birth_dt)
values ('user2@yandex.ru', 'user2', 'second user', '2001-01-01');
insert into "user" (email, login, name, birth_dt)
values ('user3@yandex.ru', 'user3', 'third user', '2002-01-01');
insert into "user" (email, login, name, birth_dt)
values ('user4@yandex.ru', 'user4', 'forth user', '2003-01-01');

insert into friend (user_id, friend_user_id, status)
values (1,2,'accepted');
insert into friend (user_id, friend_user_id, status)
values (2,1,'accepted');
insert into friend (user_id, friend_user_id, status)
values (1,3,'requested');
insert into friend (user_id, friend_user_id, status)
values (2,3,'accepted');
insert into friend (user_id, friend_user_id, status)
values (3,2,'accepted');

insert into film_like (film_id, user_id)
values (1,1);
insert into film_like (film_id, user_id)
values (1,2);
insert into film_like (film_id, user_id)
values (1,3);
insert into film_like (film_id, user_id)
values (1,4);
insert into film_like (film_id, user_id)
values (2,1);
insert into film_like (film_id, user_id)
values (2,2);
insert into film_like (film_id, user_id)
values (2,4);
insert into film_like (film_id, user_id)
values (3,3);

insert into event (user_id, event_type, action_type, entity_id, event_dttm)
values (1,'LIKE','ADD',1,now());
insert into event (user_id, event_type, action_type, entity_id, event_dttm)
values (1,'FRIEND','REMOVE',2,now());