update certifications set price_paise = 9900 where slug in ('mern-stack-development', 'java-full-stack-development');

insert into certifications (id, slug, title, description, duration_minutes, question_count, passing_score, price_paise, category, published)
values ('10000000-0000-0000-0000-000000000006', 'python-full-stack-development', 'Python Full Stack Development', 'Validate practical full-stack knowledge across Python, FastAPI, React and PostgreSQL.', 10, 10, 6, 9900, 'Programming', true);

with seed(position, prompt, theory, topic, difficulty, answer) as (
  values
  (1,'What does full-stack development include?','It includes the browser UI, backend services and persistent data.','Overview','Level 1','Frontend, backend and database work'),
  (2,'What is Python commonly used for in a full-stack application?','Python commonly implements backend logic and APIs.','Python basics','Level 1','Building backend logic and APIs'),
  (3,'What is FastAPI designed to build?','FastAPI is a Python framework for building typed web APIs.','FastAPI','Level 1','Web APIs with Python'),
  (4,'What is React responsible for in this stack?','React renders interactive browser interfaces.','React','Level 1','Interactive browser user interfaces'),
  (5,'What does PostgreSQL store?','PostgreSQL stores structured relational data.','PostgreSQL','Level 1','Structured relational data'),
  (6,'Which HTTP method normally creates a resource?','POST commonly creates a resource.','HTTP','Level 1','POST'),
  (7,'What format commonly carries API request data?','JSON carries structured request and response data.','APIs','Level 1','JSON'),
  (8,'Why use a primary key?','It uniquely identifies a table row.','Databases','Level 1','To uniquely identify a row'),
  (9,'Why use a Git branch?','It isolates work before merging.','Git','Level 1','To isolate a feature before merging'),
  (10,'Why keep secrets in environment variables?','They should not be committed to source control.','Configuration','Level 1','To keep secrets out of source control'),
  (11,'What does a FastAPI path operation define?','It maps an HTTP method and path to Python code.','FastAPI','Level 2','An HTTP route handler'),
  (12,'Why use a Pydantic model in FastAPI?','It validates and documents structured input and output.','Validation','Level 2','Validate structured API data'),
  (13,'What does async def enable in FastAPI?','It allows cooperative asynchronous work for supported I/O operations.','Async Python','Level 2','Asynchronous I/O handling'),
  (14,'Why use a database migration tool?','It versions database schema changes safely.','Databases','Level 2','Versioned schema changes'),
  (15,'What is an SQL JOIN used for?','It combines related data from tables.','SQL','Level 2','Combine related table data'),
  (16,'Where should a bearer token normally be sent?','The Authorization header is the standard API location.','Security','Level 2','The Authorization header'),
  (17,'Why hash passwords?','Hashing avoids storing recoverable plaintext passwords.','Security','Level 2','To avoid plaintext password storage'),
  (18,'What does an SQL index improve?','Indexes speed appropriate query and sort operations.','Performance','Level 2','Query and sort performance'),
  (19,'What does CORS control?','It controls browser access across origins.','Web security','Level 2','Which origins may call an API'),
  (20,'Why validate input on the server?','Clients can be bypassed, so servers enforce trusted rules.','Validation','Level 2','The client can be bypassed'),
  (21,'Why should a FastAPI dependency manage a database session lifecycle?','It ensures each request gets a safe session and cleanup.','Data access','Level 3','To safely create and clean up request sessions'),
  (22,'What is the N+1 query problem?','A list query causes an extra database query per item.','Performance','Level 3','One list query triggers a query per item'),
  (23,'Why use pagination with a large API list?','It limits work and response size.','API design','Level 3','To limit response size and database work'),
  (24,'What should happen if a JWT signature is invalid?','The API must reject it as unauthenticated.','Authentication','Level 3','Reject the request as unauthenticated'),
  (25,'Why can localStorage be risky for long-lived tokens?','XSS can read localStorage and steal the token.','Security','Level 3','XSS can steal the token'),
  (26,'Why use a transaction for related writes?','It makes related changes succeed or fail together.','Transactions','Level 3','To keep related changes all-or-nothing'),
  (27,'Why should CORS allow only known origins?','Narrow origins reduce unwanted browser access.','Web security','Level 3','To reduce unwanted browser access'),
  (28,'Why ignore a stale browser API response?','It can update a screen that is no longer relevant.','React async','Level 3','To prevent outdated state updates'),
  (29,'What should a production health endpoint check?','The service and critical dependencies should be checked.','Operations','Level 3','Service and critical dependency availability'),
  (30,'Why use structured logs with request context?','They make production failures traceable.','Observability','Level 3','Traceable diagnostics for failures')
), bank as (
  select gen_random_uuid() as id, '10000000-0000-0000-0000-000000000006'::uuid as certification_id, seed.* from seed
), inserted_questions as (
  insert into questions (id, certification_id, prompt, theory, topic, difficulty, position, published)
  select id, certification_id, prompt, theory, topic, difficulty, position, true from bank returning id
)
insert into question_options (id, question_id, label, position, correct)
select gen_random_uuid(), question.id,
  case when option_position = ((question.position - 1) % 4) + 1 then question.answer else (select distractor.answer from bank distractor where distractor.position = ((question.position + option_position - 1) % 30) + 1) end,
  option_position, option_position = ((question.position - 1) % 4) + 1
from bank question cross join generate_series(1, 4) as option_position;
