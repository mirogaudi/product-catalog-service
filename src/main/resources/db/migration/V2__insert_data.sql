/* Categories */
insert into category (id, name, parent_id)
values (0, 'Computers', null);

insert into category (id, name, parent_id)
values (1, 'Notebooks', 0);

insert into category (id, name, parent_id)
values (2, 'Tablets', 1);

insert into category (id, name, parent_id)
values (3, 'Apple', null);

/* Products */
insert into product (id, name, price, currency, original_price, original_currency)
values (1, 'MacBook', 1677.55, 'EUR', 1999, 'USD');

insert into product (id, name, price, currency, original_price, original_currency)
values (2, 'iPad', 838.99, 'EUR', 999.99, 'USD');

/* Product Categories */
insert into product_category (product_id, category_id)
values ((select id from product where name = 'MacBook'), (select id from category where name = 'Notebooks'));
insert into product_category (product_id, category_id)
values ((select id from product where name = 'MacBook'), (select id from category where name = 'Apple'));

insert into product_category (product_id, category_id)
values ((select id from product where name = 'iPad'), (select id from category where name = 'Tablets'));
insert into product_category (product_id, category_id)
values ((select id from product where name = 'iPad'), (select id from category where name = 'Apple'));