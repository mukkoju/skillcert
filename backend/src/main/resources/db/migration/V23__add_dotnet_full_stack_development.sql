insert into certifications (id, slug, title, description, duration_minutes, question_count, passing_score, price_paise, category, published)
values ('10000000-0000-0000-0000-000000000007', 'dotnet-full-stack-development', '.NET Full Stack Development', 'Validate practical full-stack knowledge across C#, ASP.NET Core, React and SQL Server.', 10, 10, 6, 9900, 'Programming', true);

with seed(position, prompt, theory, topic, difficulty, answer) as (
  values
  (1,'What does full-stack development include?','It covers browser UI, backend services and persistent data.','Overview','Level 1','Frontend, backend and database work'),
  (2,'What language is most commonly used with .NET backend services?','C# is the primary language for modern .NET application development.','C# basics','Level 1','C#'),
  (3,'What does ASP.NET Core commonly build?','ASP.NET Core builds web applications and HTTP APIs.','ASP.NET Core','Level 1','Web applications and APIs'),
  (4,'What is React responsible for in this stack?','React renders the interactive browser interface.','Frontend','Level 1','Interactive browser user interfaces'),
  (5,'What does SQL Server store?','SQL Server stores structured relational data.','SQL Server','Level 1','Structured relational data'),
  (6,'Which HTTP method normally reads a resource?','GET is used to retrieve a resource.','HTTP','Level 1','GET'),
  (7,'What format commonly carries API request data?','JSON represents structured request and response data.','APIs','Level 1','JSON'),
  (8,'Why use a primary key?','It uniquely identifies a row in a table.','Databases','Level 1','To uniquely identify a row'),
  (9,'Why use Git branches?','They isolate work before it merges into the main line.','Git','Level 1','To isolate feature work before merging'),
  (10,'Why keep connection strings outside source code?','Secrets must not be committed and vary safely per environment.','Configuration','Level 1','To keep secrets out of source control'),
  (11,'What does an ASP.NET Core controller action do?','It handles an HTTP request and returns an HTTP response.','ASP.NET Core','Level 2','Handles an HTTP request and response'),
  (12,'Why use a request DTO?','A DTO defines a focused API contract and validates expected input.','API design','Level 2','Define a focused API request contract'),
  (13,'What does model validation protect?','It prevents invalid request data reaching business logic.','Validation','Level 2','Invalid input reaching business logic'),
  (14,'What is Entity Framework Core used for?','EF Core maps .NET objects to database operations.','EF Core','Level 2','Mapping .NET objects to database operations'),
  (15,'What is a foreign key for?','It maintains integrity between related tables.','SQL','Level 2','Maintaining related table integrity'),
  (16,'What should a successful create endpoint normally return?','A created resource normally returns HTTP 201 Created.','REST APIs','Level 2','HTTP 201 Created'),
  (17,'What controls a React form input?','A controlled input uses React state as its value source.','React','Level 2','React component state'),
  (18,'Where should a bearer token be sent?','The standard API location is the Authorization header.','Authentication','Level 2','The Authorization header'),
  (19,'Why hash passwords before storing them?','Hashing avoids storing recoverable plaintext passwords.','Security','Level 2','To avoid plaintext password storage'),
  (20,'What does a database index improve?','Indexes speed suitable queries and sorting.','Performance','Level 2','Query and sort performance'),
  (21,'What is the N+1 query problem?','It is when one list query causes a separate query for each item.','EF Core performance','Level 3','A list query triggers another query per item'),
  (22,'Why use AsNoTracking for a read-only EF Core query?','It avoids change-tracking overhead when no updates are needed.','EF Core performance','Level 3','To avoid unnecessary change-tracking overhead'),
  (23,'What is optimistic concurrency intended to detect?','It detects conflicting edits before an update silently overwrites data.','Data integrity','Level 3','Conflicting concurrent updates'),
  (24,'Why use a transaction for related writes?','It ensures related changes succeed or fail together.','Transactions','Level 3','Keep related changes all-or-nothing'),
  (25,'What should happen when a JWT signature is invalid?','The API must reject the request as unauthenticated.','Authentication','Level 3','Reject the request as unauthenticated'),
  (26,'Why should CORS origins be configured narrowly?','It limits which browser applications can call the API.','Web security','Level 3','Limit which browser origins can call the API'),
  (27,'Why should an API derive user identity from claims instead of request input?','Request input is untrusted; authenticated claims support proper authorisation.','Authorisation','Level 3','Authenticated claims are the trusted identity source'),
  (28,'Why ignore a stale React API response?','It can update a screen that is no longer relevant.','React async','Level 3','To prevent outdated state updates'),
  (29,'What should a production health check verify?','It should verify service availability and important dependencies.','Operations','Level 3','Service and critical dependency availability'),
  (30,'Why use structured logs with correlation data?','They make production failures traceable across requests.','Observability','Level 3','Traceable diagnostics for requests and failures')
), bank as (
  select gen_random_uuid() as id, '10000000-0000-0000-0000-000000000007'::uuid as certification_id, seed.* from seed
), inserted_questions as (
  insert into questions (id, certification_id, prompt, theory, topic, difficulty, position, published)
  select id, certification_id, prompt, theory, topic, difficulty, position, true from bank returning id
)
insert into question_options (id, question_id, label, position, correct)
select gen_random_uuid(), question.id,
  case when option_position = ((question.position - 1) % 4) + 1 then question.answer else (select distractor.answer from bank distractor where distractor.position = ((question.position + option_position - 1) % 30) + 1) end,
  option_position, option_position = ((question.position - 1) % 4) + 1
from bank question cross join generate_series(1, 4) as option_position;
