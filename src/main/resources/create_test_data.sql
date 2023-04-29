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

insert into director (name)
values('Director1'),
      ('Director2'),
      ('Director3');

insert into film_x_director (film_id, director_id)
values (1, 1),
       (2, 2),
       (3, 3),
       (3, 2);

insert into review (creator_user_id, reviewed_film_id, content, is_positive) --1 (score: 2)
values (1, 1, 'Ничо так, могли бы и лучше.', true);
insert into review (creator_user_id, reviewed_film_id, content, is_positive) --2 (score: 4)
values (1, 2, 'Хорошее кино, режиссер молодец.', true);
insert into review (creator_user_id, reviewed_film_id, content, is_positive) --3 (score: -2)
values (2, 3, 'Зря потратил время, фильм ужасен.', false);
insert into review (creator_user_id, reviewed_film_id, content, is_positive) --4 (score: 1)
values (3, 3, 'Несерьезное кино, для детей', true);
insert into review (creator_user_id, reviewed_film_id, content, is_positive) --5 (score: -1)
values (4, 1, 'Проходное кино', true);
insert into review (creator_user_id, reviewed_film_id, content, is_positive) --6 (score: 3)
values (2, 2, 'Проникся, спасибо', true);

insert into review_like (review_id, user_id, score) values (1, 1, 1);
insert into review_like (review_id, user_id, score) values (1, 2, 1);
insert into review_like (review_id, user_id, score) values (1, 3, 1);
insert into review_like (review_id, user_id, score) values (1, 4, -1);

insert into review_like (review_id, user_id, score) values (2, 1, 1);
insert into review_like (review_id, user_id, score) values (2, 2, 1);
insert into review_like (review_id, user_id, score) values (2, 3, 1);
insert into review_like (review_id, user_id, score) values (2, 4, 1);

insert into review_like (review_id, user_id, score) values (3, 1, -1);
insert into review_like (review_id, user_id, score) values (3, 2, -1);
insert into review_like (review_id, user_id, score) values (3, 3, -1);
insert into review_like (review_id, user_id, score) values (3, 4, 1);

insert into review_like (review_id, user_id, score) values (4, 1, -1);
insert into review_like (review_id, user_id, score) values (4, 2, 1);
insert into review_like (review_id, user_id, score) values (4, 3, 1);

insert into review_like (review_id, user_id, score) values (5, 2, 1);
insert into review_like (review_id, user_id, score) values (5, 3, -1);
insert into review_like (review_id, user_id, score) values (5, 4, -1);

insert into review_like (review_id, user_id, score) values (6, 1, 1);
insert into review_like (review_id, user_id, score) values (6, 2, 1);
insert into review_like (review_id, user_id, score) values (6, 3, 1);