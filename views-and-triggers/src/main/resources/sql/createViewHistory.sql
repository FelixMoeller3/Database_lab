create view history as
    select id,date,purchase.article as article,quantity,purchase.quantity * (
        select price from
         article
         where article=purchase.article
    ) as price
    from purchase
    where customer = (select current_user)
    order by id desc;