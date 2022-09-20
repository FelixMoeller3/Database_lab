select *
from history
where date = (select current_date);