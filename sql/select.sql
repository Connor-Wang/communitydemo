insert into mycommunity.message(from_id, to_id, conversation_id, content, status, create_time) values(111,112,"111_112","213234",)

select count(*) from discuss_post order by score desc;

explain select p.create_time 
from discuss_post as p 
inner join `user` as u
on p.user_id = u.id 
order by p.id desc;