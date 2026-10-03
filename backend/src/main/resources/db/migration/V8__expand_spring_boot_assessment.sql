alter table questions add column topic varchar(80) not null default 'Core concepts';
alter table questions add column difficulty varchar(24) not null default 'Level 1';
alter table questions add column asked_by_company varchar(120);
alter table questions add column code_snippet text;

update certifications set question_count = 10 where slug = 'spring-boot-foundations';
update questions set topic = 'Application setup', difficulty = 'Level 1' where id = '20000000-0000-0000-0000-000000000101';
update questions set topic = 'Web endpoints', difficulty = 'Level 1' where id = '20000000-0000-0000-0000-000000000102';
update questions set topic = 'Configuration', difficulty = 'Level 1' where id = '20000000-0000-0000-0000-000000000103';
update questions set topic = 'REST APIs', difficulty = 'Level 1' where id = '20000000-0000-0000-0000-000000000104';
update questions set topic = 'Spring beans', difficulty = 'Level 1' where id = '20000000-0000-0000-0000-000000000105';
update questions set code_snippet = '@SpringBootApplication\npublic class SkillCertApplication {\n  public static void main(String[] args) {\n    SpringApplication.run(SkillCertApplication.class, args);\n  }\n}' where id = '20000000-0000-0000-0000-000000000101';
update questions set code_snippet = '@RestController\nclass HealthController {\n  @GetMapping("/health")\n  String health() { return "OK"; }\n}' where id = '20000000-0000-0000-0000-000000000108';

insert into questions (id, certification_id, prompt, theory, topic, difficulty, position, published) values
('20000000-0000-0000-0000-000000000106','10000000-0000-0000-0000-000000000002','Which annotation is commonly used to inject a dependency through a constructor?','Spring can inject dependencies through a constructor. When there is one constructor, @Autowired is optional in modern Spring versions.','Dependency injection','Level 1',6,true),
('20000000-0000-0000-0000-000000000107','10000000-0000-0000-0000-000000000002','Which file commonly holds Spring Boot configuration properties?','application.properties or application.yml is used for externalised configuration in a Spring Boot application.','Configuration','Level 1',7,true),
('20000000-0000-0000-0000-000000000108','10000000-0000-0000-0000-000000000002','What does @RestController indicate?','@RestController combines @Controller and @ResponseBody, so returned values are written directly to the response body.','REST APIs','Level 1',8,true),
('20000000-0000-0000-0000-000000000109','10000000-0000-0000-0000-000000000002','Which embedded server is included by default with spring-boot-starter-web?','Spring Boot uses embedded Tomcat by default for applications built with spring-boot-starter-web.','Web server','Level 1',9,true),
('20000000-0000-0000-0000-000000000110','10000000-0000-0000-0000-000000000002','What is the purpose of @RequestBody in a REST controller?','@RequestBody binds an HTTP request body, often JSON, to a Java method parameter.','REST APIs','Level 1',10,true);

update questions set code_snippet = '@RestController\nclass HealthController {\n  @GetMapping("/health")\n  String health() { return "OK"; }\n}' where id = '20000000-0000-0000-0000-000000000108';

insert into question_options (id, question_id, label, position, correct) values
('30000000-0000-0000-0000-000000000151','20000000-0000-0000-0000-000000000106','@Autowired',1,true),('30000000-0000-0000-0000-000000000152','20000000-0000-0000-0000-000000000106','@RequestParam',2,false),('30000000-0000-0000-0000-000000000153','20000000-0000-0000-0000-000000000106','@ResponseStatus',3,false),('30000000-0000-0000-0000-000000000154','20000000-0000-0000-0000-000000000106','@ValueSource',4,false),
('30000000-0000-0000-0000-000000000161','20000000-0000-0000-0000-000000000107','application.properties',1,true),('30000000-0000-0000-0000-000000000162','20000000-0000-0000-0000-000000000107','pom.lock',2,false),('30000000-0000-0000-0000-000000000163','20000000-0000-0000-0000-000000000107','server.class',3,false),('30000000-0000-0000-0000-000000000164','20000000-0000-0000-0000-000000000107','routes.json only',4,false),
('30000000-0000-0000-0000-000000000171','20000000-0000-0000-0000-000000000108','Returned values are written to the response body',1,true),('30000000-0000-0000-0000-000000000172','20000000-0000-0000-0000-000000000108','It creates a database table',2,false),('30000000-0000-0000-0000-000000000173','20000000-0000-0000-0000-000000000108','It starts the application',3,false),('30000000-0000-0000-0000-000000000174','20000000-0000-0000-0000-000000000108','It only handles static files',4,false),
('30000000-0000-0000-0000-000000000181','20000000-0000-0000-0000-000000000109','Tomcat',1,true),('30000000-0000-0000-0000-000000000182','20000000-0000-0000-0000-000000000109','Nginx',2,false),('30000000-0000-0000-0000-000000000183','20000000-0000-0000-0000-000000000109','Apache HTTP Server',3,false),('30000000-0000-0000-0000-000000000184','20000000-0000-0000-0000-000000000109','IIS',4,false),
('30000000-0000-0000-0000-000000000191','20000000-0000-0000-0000-000000000110','Bind the request body to a method parameter',1,true),('30000000-0000-0000-0000-000000000192','20000000-0000-0000-0000-000000000110','Set the response status to 200 only',2,false),('30000000-0000-0000-0000-000000000193','20000000-0000-0000-0000-000000000110','Create a scheduled task',3,false),('30000000-0000-0000-0000-000000000194','20000000-0000-0000-0000-000000000110','Enable component scanning',4,false);
