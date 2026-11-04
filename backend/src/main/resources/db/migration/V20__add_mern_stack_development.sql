insert into certifications (id, slug, title, description, duration_minutes, question_count, passing_score, price_paise, category, published)
values ('10000000-0000-0000-0000-000000000004', 'mern-stack-development', 'MERN Stack Development', 'Validate practical full-stack knowledge across MongoDB, Express, React and Node.js.', 10, 10, 6, 19900, 'Programming', true);

-- Thirty questions: every attempt randomly receives 3 beginner, 4 medium and 3 advanced questions.
with seed(position, prompt, theory, topic, difficulty, answer) as (
  values
  (1, 'Which technologies make up the MERN stack?', 'MERN combines MongoDB, Express.js, React and Node.js.', 'MERN overview', 'Level 1', 'MongoDB, Express.js, React and Node.js'),
  (2, 'What is React mainly used for in a MERN application?', 'React builds the interactive browser user interface from reusable components.', 'React basics', 'Level 1', 'Building the browser user interface with components'),
  (3, 'What is Express.js commonly used for?', 'Express provides routing and middleware for Node.js web APIs.', 'Express basics', 'Level 1', 'Building HTTP APIs and middleware on Node.js'),
  (4, 'Which database model does MongoDB use?', 'MongoDB stores flexible BSON documents in collections.', 'MongoDB basics', 'Level 1', 'Document-oriented collections of BSON documents'),
  (5, 'What does Node.js allow JavaScript developers to do?', 'Node.js runs JavaScript outside the browser, commonly for servers and tools.', 'Node.js basics', 'Level 1', 'Run JavaScript on a server or command line'),
  (6, 'What is the purpose of a REST API endpoint?', 'An endpoint exposes a resource or operation through a defined HTTP route and method.', 'API basics', 'Level 1', 'Expose a resource or operation through HTTP'),
  (7, 'Which HTTP method is normally used to create a new resource?', 'POST is typically used to create a resource under a collection endpoint.', 'HTTP', 'Level 1', 'POST'),
  (8, 'Why should a React list item have a key prop?', 'A stable key lets React identify list items correctly when rendering changes.', 'React rendering', 'Level 1', 'To give React a stable identity for each list item'),
  (9, 'What does JSON represent in a typical MERN API?', 'JSON is the common text format used to exchange structured request and response data.', 'Data exchange', 'Level 1', 'Structured request and response data'),
  (10, 'Why are environment variables used for API secrets?', 'They keep credentials out of source control and allow values to differ by environment.', 'Configuration', 'Level 1', 'Keep secrets out of source code and vary configuration safely'),
  (11, 'What is middleware in Express?', 'Middleware runs between request receipt and the final route handler, often for parsing, auth or logging.', 'Express middleware', 'Level 2', 'A function in the request-response pipeline'),
  (12, 'What should an Express error-handling middleware include to receive errors?', 'Express recognises error middleware by its four parameters: err, req, res and next.', 'Error handling', 'Level 2', 'The four parameters err, req, res and next'),
  (13, 'What is the main benefit of a controlled React input?', 'The input value is held in React state, making validation and UI updates predictable.', 'React forms', 'Level 2', 'React state controls the displayed input value'),
  (14, 'When should useEffect commonly be used?', 'useEffect is for synchronising with external systems such as fetching data, subscriptions or browser APIs.', 'React hooks', 'Level 2', 'Synchronising a component with an external system'),
  (15, 'What is the purpose of a JWT after a successful login?', 'A signed token carries authenticated identity claims that the client can send with later requests.', 'Authentication', 'Level 2', 'Represent authenticated identity for later requests'),
  (16, 'Where should a browser client normally send a bearer token?', 'The standard API convention is the Authorization header with the Bearer scheme.', 'Authentication', 'Level 2', 'The Authorization header using the Bearer scheme'),
  (17, 'Why should passwords be hashed before storing them?', 'A one-way password hash limits damage if the database is exposed.', 'Security', 'Level 2', 'To avoid storing recoverable plaintext passwords'),
  (18, 'What does a MongoDB index primarily improve?', 'Indexes make matching and sorting queries faster at the cost of additional storage and write work.', 'MongoDB performance', 'Level 2', 'Query and sort performance'),
  (19, 'What is CORS intended to control?', 'CORS controls whether browsers allow a page from one origin to call another origin.', 'Web security', 'Level 2', 'Which browser origins may call an API'),
  (20, 'Why should API validation run on the server even when the client validates fields?', 'A client can be bypassed, so the server must enforce the trusted validation rules.', 'Validation', 'Level 2', 'Clients can be bypassed and the server is the trusted boundary'),
  (21, 'Why can updating React state with a stale captured value be unsafe?', 'Multiple queued updates can overwrite one another; a functional update uses the latest state.', 'React state', 'Level 3', 'Queued updates can overwrite each other without a functional update'),
  (22, 'What problem can arise when an async request finishes after its React component unmounts?', 'The old request may try to update irrelevant state; cancellation or ignoring stale results prevents this.', 'React async', 'Level 3', 'A stale response can update state after the component is no longer relevant'),
  (23, 'Why should MongoDB pagination avoid large skip values for large collections?', 'Large skip values make MongoDB scan discarded documents; cursor-based pagination scales better.', 'MongoDB performance', 'Level 3', 'It scans discarded documents and becomes slow at high offsets'),
  (24, 'What is an N+1-style API problem in a MERN backend?', 'It occurs when a list request triggers an additional database query for each returned item.', 'API performance', 'Level 3', 'One list query triggers another query for every returned item'),
  (25, 'What should happen if a JWT signature is invalid?', 'The API must reject the request as unauthenticated and never trust claims from that token.', 'Authentication', 'Level 3', 'Reject the request as unauthenticated'),
  (26, 'Why is storing a long-lived JWT in localStorage a security trade-off?', 'JavaScript running on the site can read localStorage, so an XSS flaw can expose the token.', 'Security', 'Level 3', 'An XSS vulnerability can read and steal the token'),
  (27, 'What is the advantage of a database transaction for a multi-document business operation?', 'A transaction preserves all-or-nothing consistency when related changes must succeed together.', 'MongoDB transactions', 'Level 3', 'It keeps related changes all-or-nothing'),
  (28, 'Why should an API use rate limiting on login routes?', 'Rate limits reduce password-guessing and abusive automated traffic.', 'API security', 'Level 3', 'To reduce brute-force and abusive automated requests'),
  (29, 'What is the safest way to build a MongoDB query from user input?', 'Validate and constrain expected fields and values instead of passing untrusted operators directly.', 'MongoDB security', 'Level 3', 'Validate and constrain input before building the query'),
  (30, 'What is a useful production health check for a MERN service?', 'A health endpoint should confirm the service is running and, when appropriate, that critical dependencies are reachable.', 'Operations', 'Level 3', 'A health endpoint that checks the service and critical dependencies')
), bank as (
  select gen_random_uuid() as id, '10000000-0000-0000-0000-000000000004'::uuid as certification_id, seed.* from seed
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
