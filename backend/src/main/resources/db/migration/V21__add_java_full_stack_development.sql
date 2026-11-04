insert into certifications (id, slug, title, description, duration_minutes, question_count, passing_score, price_paise, category, published)
values ('10000000-0000-0000-0000-000000000005', 'java-full-stack-development', 'Java Full Stack Development', 'Validate practical full-stack knowledge across Java, Spring Boot, React, SQL and secure web APIs.', 10, 10, 6, 19900, 'Programming', true);

with seed(position, prompt, theory, topic, difficulty, answer) as (
  values
  (1, 'What does full-stack development mean in a Java application?', 'It covers the user interface, backend APIs, data storage and deployment concerns.', 'Full-stack overview', 'Level 1', 'Working across the UI, backend APIs and database'),
  (2, 'What is Java commonly used for on a full-stack team?', 'Java commonly implements the backend business logic and APIs.', 'Java basics', 'Level 1', 'Implementing backend business logic and APIs'),
  (3, 'What is Spring Boot designed to simplify?', 'Spring Boot simplifies building stand-alone production-ready Spring applications.', 'Spring Boot basics', 'Level 1', 'Building stand-alone Spring applications quickly'),
  (4, 'What is React commonly responsible for in a Java full-stack application?', 'React renders the interactive browser interface and communicates with backend APIs.', 'Frontend basics', 'Level 1', 'The interactive browser user interface'),
  (5, 'What does a relational database table represent?', 'A table represents a structured collection of rows for one entity or relationship.', 'SQL basics', 'Level 1', 'A structured collection of related rows'),
  (6, 'Which HTTP method normally retrieves a resource without changing it?', 'GET is intended for reading a resource.', 'HTTP', 'Level 1', 'GET'),
  (7, 'What does JSON usually carry between a React client and Spring Boot API?', 'JSON represents structured request and response data.', 'Data exchange', 'Level 1', 'Structured request and response data'),
  (8, 'Why use a Git branch for a feature?', 'A branch isolates feature work from the shared main line until it is ready to merge.', 'Version control', 'Level 1', 'To isolate feature work before merging'),
  (9, 'What does a primary key identify in a database table?', 'A primary key uniquely identifies each row.', 'SQL basics', 'Level 1', 'One unique row'),
  (10, 'Why should secrets be configured outside source code?', 'Secrets should not be committed and need to differ safely by environment.', 'Configuration', 'Level 1', 'To keep credentials out of source control'),
  (11, 'What does @RestController indicate in Spring Boot?', 'It declares a controller whose methods write REST response bodies by default.', 'Spring MVC', 'Level 2', 'A controller that returns REST response bodies'),
  (12, 'What is dependency injection intended to improve?', 'It separates a class from construction of its dependencies, improving testing and design.', 'Spring core', 'Level 2', 'Separation of dependencies from the class that uses them'),
  (13, 'Why use a DTO at an API boundary?', 'DTOs expose only the data the API contract needs and avoid leaking persistence models.', 'API design', 'Level 2', 'To define a focused API contract without exposing entities'),
  (14, 'What does @Valid do on a Spring request DTO?', 'It triggers Bean Validation annotations before the controller continues.', 'Validation', 'Level 2', 'Triggers validation of request constraints'),
  (15, 'What is the main purpose of a database foreign key?', 'A foreign key protects the relationship between related tables.', 'SQL relationships', 'Level 2', 'Maintain integrity between related tables'),
  (16, 'What should a successful REST create operation normally return?', 'A new resource is commonly reported with HTTP 201 Created.', 'REST APIs', 'Level 2', 'HTTP 201 Created'),
  (17, 'What does a React controlled input use as its source of truth?', 'Its displayed value is controlled by React state.', 'React forms', 'Level 2', 'React component state'),
  (18, 'Where should a browser normally send a JWT bearer token?', 'The standard location is the Authorization header.', 'Authentication', 'Level 2', 'The Authorization header'),
  (19, 'Why should a password be hashed before persistence?', 'A one-way hash avoids keeping recoverable password text in the database.', 'Security', 'Level 2', 'To avoid storing recoverable plaintext passwords'),
  (20, 'What is an index used for in a relational database?', 'An index can speed suitable lookups and sorting while adding write and storage cost.', 'SQL performance', 'Level 2', 'Speeding suitable database queries'),
  (21, 'Why can returning a JPA entity directly from a controller cause LazyInitializationException?', 'A lazy relationship may be accessed after the persistence context is no longer active.', 'JPA', 'Level 3', 'A lazy relationship is read after the persistence context closes'),
  (22, 'What is the N+1 query problem?', 'It occurs when loading a list triggers another database query for each item.', 'JPA performance', 'Level 3', 'A list query causes an additional query per item'),
  (23, 'Why use optimistic locking with an @Version field?', 'It detects conflicting concurrent edits rather than silently overwriting another update.', 'Data integrity', 'Level 3', 'Detect conflicting concurrent updates'),
  (24, 'What happens when a Spring @Transactional method throws an unchecked exception by default?', 'Spring normally rolls back the transaction.', 'Transactions', 'Level 3', 'The transaction is rolled back'),
  (25, 'Why is self-invocation of a @Transactional method a trap?', 'A call through this does not normally pass through Spring proxy interception.', 'Transactions', 'Level 3', 'The call bypasses Spring proxy interception'),
  (26, 'Why should CORS be configured narrowly in production?', 'It limits which browser origins are permitted to call the API.', 'Web security', 'Level 3', 'To limit which browser origins can call the API'),
  (27, 'What should a backend do before trusting a user identifier in a request body?', 'It should derive identity from authenticated credentials and authorise the operation.', 'Authorisation', 'Level 3', 'Authenticate and authorise using trusted credentials'),
  (28, 'Why should a React request be cancelled or ignored when a component changes?', 'A stale response can update state for an outdated screen.', 'React async', 'Level 3', 'To prevent stale responses updating outdated state'),
  (29, 'What does a health endpoint help a deployed Java service provide?', 'It lets infrastructure check service availability and critical dependencies.', 'Operations', 'Level 3', 'A way to check service and dependency availability'),
  (30, 'Why should a production API use structured logging with request context?', 'It makes failures traceable across requests without relying on unstructured text.', 'Observability', 'Level 3', 'Traceable diagnostics for requests and failures')
), bank as (
  select gen_random_uuid() as id, '10000000-0000-0000-0000-000000000005'::uuid as certification_id, seed.* from seed
), inserted_questions as (
  insert into questions (id, certification_id, prompt, theory, topic, difficulty, position, published)
  select id, certification_id, prompt, theory, topic, difficulty, position, true from bank
  returning id
)
insert into question_options (id, question_id, label, position, correct)
select gen_random_uuid(), question.id,
  case when option_position = ((question.position - 1) % 4) + 1 then question.answer
       else (select distractor.answer from bank distractor where distractor.position = ((question.position + option_position - 1) % 30) + 1) end,
  option_position, option_position = ((question.position - 1) % 4) + 1
from bank question cross join generate_series(1, 4) as option_position;
