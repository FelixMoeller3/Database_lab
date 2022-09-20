create table customer (
    name text not null,
    balance int not null,
    primary key (name)
);
create table article (
    article text not null,
    price int not null,
    primary key (article)
);
create table purchase (
    id int not null,
    customer text not null,
    date date not null,
    article text not null,
    quantity int not null,
    primary key (id),
    foreign key (customer) references customer(name),
    foreign key (article) references article(article)
);