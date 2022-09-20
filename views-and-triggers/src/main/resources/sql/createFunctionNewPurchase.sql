create or replace function new_purchase(_article text,_amount integer) returns boolean as $$
declare success boolean;
        _price integer;
        customerBalance integer;
        _maxId integer;
        _currentDate date;
begin
_price :=  (select price from article where article=_article) * _amount;
IF _price > (select balance from customer where name=(select session_user)) THEN
success := false;
return success;
END IF;
select balance into customerBalance from customer where name = (select session_user);
update customer set balance = (customerBalance - _price) where name= (select session_user);
select max(id) into _maxId from purchase;
_maxId := _maxId + 1;
insert into purchase values(_maxId,(select session_user),(select current_date),_article, _amount);
success := true;
return success;
end;
$$ Language plpgSQL SECURITY DEFINER;