/* Categories */
insert into category (name, parent_id)
values ('Computers', null);
insert into category (name, parent_id)
values ('Portable computers', (select id from category where name = 'Computers'));
insert into category (name, parent_id)
values ('Tablets', (select id from category where name = 'Portable computers'));
insert into category (name, parent_id)
values ('Apple', null),
       ('Lenovo', null);

/* Products */
insert into product (name, price, currency, original_price, original_currency)
values ('MacBook', 1786.89, 'EUR', 1999.99, 'USD'),
       ('iPad', 893.36, 'EUR', 999.99, 'USD'),
       ('ThinkPad', 1339.45, 'EUR', 1499.99, 'USD');

/* Product Categories */
insert into product_category (product_id, category_id)
values ((select id from product where name = 'MacBook'), (select id from category where name = 'Portable computers')),
       ((select id from product where name = 'MacBook'), (select id from category where name = 'Apple')),
       ((select id from product where name = 'iPad'), (select id from category where name = 'Tablets')),
       ((select id from product where name = 'iPad'), (select id from category where name = 'Apple')),
       ((select id from product where name = 'ThinkPad'), (select id from category where name = 'Portable computers')),
       ((select id from product where name = 'ThinkPad'), (select id from category where name = 'Lenovo'));
