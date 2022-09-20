grant delete on history to public;
drop table if exists temp_purchase;
create table temp_purchase (price int, date date);
create rule deleteFromHistory as on delete to history do instead (
    delete from temp_purchase;

    insert into temp_purchase values(old.price, old.date);

    update customer set balance = balance + (
        select sum(price)
        from temp_purchase
        where date = current_date)
    where name = current_user;

    delete from purchase
    where article=old.article
        and date=current_date
        and customer=current_user;
    );