-- Seed authors
INSERT INTO authors (id, full_name, biography) VALUES
(1, 'Chimamanda Ngozi Adichie', 'Nigerian writer known for novels, essays, and lectures.'),
(2, 'James Clear', 'Author and speaker focused on habits and continuous improvement.'),
(3, 'Donald E. Knuth', 'Computer scientist and author of The Art of Computer Programming.'),
(4, 'Barbara Oakley', 'Professor and author specializing in learning and engineering education.'),
(5, 'Tim Berners-Lee', 'Inventor of the World Wide Web and advocate for open web standards.'),
(6, 'Margaret Atwood', 'Canadian poet and novelist exploring society, technology, and identity.'),
(7, 'Robert C. Martin', 'Software engineer and author known for writings on clean code and craftsmanship.'),
(8, 'Yuval Noah Harari', 'Historian and author examining history, technology, and humanity.'),
(9, 'Andrew S. Tanenbaum', 'Computer scientist known for textbooks on operating systems and networks.'),
(10, 'Mary Shelley', 'English novelist, best known for Frankenstein.'),
(11, 'Eric Evans', 'Software design consultant and author of Domain-Driven Design.'),
(12, 'Marijn Haverbeke', 'Programmer and author of Eloquent JavaScript.'),
(13, 'Jane Austen', 'English novelist known for social commentary and realism.'),
(14, 'Bjarne Stroustrup', 'Creator of C++ and author on programming language design.'),
(15, 'Cathy O''Neil', 'Mathematician and author focused on data, algorithms, and society.'),
(16, 'Gilbert Strang', 'Mathematician and educator known for linear algebra texts.'),
(17, 'Ngũgĩ wa Thiong''o', 'Kenyan writer and academic known for postcolonial literature.'),
(18, 'Martin Kleppmann', 'Researcher and engineer focused on distributed data systems.'),
(19, 'Ursula K. Le Guin', 'Author of science fiction and fantasy exploring society and anthropology.'),
(20, 'Katherine Johnson', 'NASA mathematician whose work supported early spaceflight missions.');

-- Seed subjects
INSERT INTO subjects (id, name, parent_subject_id) VALUES
(1, 'Computer Science', NULL),
(2, 'Software Engineering', 1),
(3, 'Distributed Systems', 1),
(4, 'Web Technologies', 1),
(5, 'Literature', NULL),
(6, 'African Literature', 5),
(7, 'Science Fiction', 5),
(8, 'History', NULL),
(9, 'Technology and Society', 8),
(10, 'Mathematics', NULL),
(11, 'Linear Algebra', 10),
(12, 'Education', NULL),
(13, 'Learning Science', 12),
(14, 'Open Access', NULL),
(15, 'Research Methods', 12);

-- Seed resources (30)
INSERT INTO resources (id, title, description, publisher, publication_date, resource_type, language, isbn_or_identifier, format, source_url, rights, status) VALUES
(1, 'Half of a Yellow Sun', 'A novel set during the Nigerian Civil War, exploring family, love, and conflict.', 'Fourth Estate', '2006-08-11', 'BOOK', 'English', 'ISBN-9780007200283', 'PDF', 'https://example.org/resources/half-of-a-yellow-sun', 'Licensed for academic use', 'ACTIVE'),
(2, 'Americanah', 'A story of migration, identity, and belonging across Nigeria, the UK, and the US.', 'Alfred A. Knopf', '2013-05-14', 'BOOK', 'English', 'ISBN-9780307271082', 'EPUB', 'https://example.org/resources/americanah', 'Licensed for academic use', 'ACTIVE'),
(3, 'Atomic Habits', 'Practical strategies for building good habits and breaking bad ones.', 'Avery', '2018-10-16', 'BOOK', 'English', 'ISBN-9780735211292', 'PDF', 'https://example.org/resources/atomic-habits', 'Campus license', 'ACTIVE'),
(4, 'The Art of Computer Programming, Vol. 1', 'Fundamental algorithms and foundational techniques in computer science.', 'Addison-Wesley', '1997-07-04', 'BOOK', 'English', 'ISBN-9780201896831', 'PDF', 'https://example.org/resources/taocp-vol1', 'Library subscription', 'ACTIVE'),
(5, 'A Mind for Numbers', 'Techniques for effective learning in mathematics and science.', 'TarcherPerigee', '2014-07-31', 'BOOK', 'English', 'ISBN-9780399165245', 'PDF', 'https://example.org/resources/mind-for-numbers', 'Campus license', 'ACTIVE'),
(6, 'Weaving the Web', 'An account of the invention of the World Wide Web and its future.', 'Harper', '1999-09-22', 'BOOK', 'English', 'ISBN-9780062515872', 'PDF', 'https://example.org/resources/weaving-the-web', 'Open educational access', 'ACTIVE'),
(7, 'The Handmaid''s Tale', 'A dystopian novel examining power, gender, and autonomy.', 'McClelland and Stewart', '1985-04-17', 'BOOK', 'English', 'ISBN-9780771008795', 'EPUB', 'https://example.org/resources/handmaids-tale', 'Licensed for academic use', 'ACTIVE'),
(8, 'Clean Code', 'A handbook of agile software craftsmanship and maintainable code practices.', 'Prentice Hall', '2008-08-01', 'BOOK', 'English', 'ISBN-9780132350884', 'PDF', 'https://example.org/resources/clean-code', 'Library subscription', 'ACTIVE'),
(9, 'Sapiens: A Brief History of Humankind', 'An exploration of human history from ancient times to the modern era.', 'Harper', '2015-02-10', 'BOOK', 'English', 'ISBN-9780062316097', 'PDF', 'https://example.org/resources/sapiens', 'Campus license', 'ACTIVE'),
(10, 'Modern Operating Systems', 'Comprehensive coverage of operating system concepts and design.', 'Pearson', '2014-03-20', 'BOOK', 'English', 'ISBN-9780133591620', 'PDF', 'https://example.org/resources/modern-os', 'Library subscription', 'ACTIVE'),
(11, 'Frankenstein', 'A classic novel exploring creation, responsibility, and scientific ambition.', 'Lackington, Hughes, Harding, Mavor & Jones', '1818-01-01', 'BOOK', 'English', 'ISBN-9780141439471', 'PDF', 'https://example.org/resources/frankenstein', 'Public domain', 'ACTIVE'),
(12, 'Domain-Driven Design', 'Tackling complexity in the heart of software through modeling and design.', 'Addison-Wesley', '2003-08-30', 'BOOK', 'English', 'ISBN-9780321125217', 'PDF', 'https://example.org/resources/domain-driven-design', 'Library subscription', 'ACTIVE'),
(13, 'Eloquent JavaScript', 'A modern introduction to programming with JavaScript.', 'No Starch Press', '2018-12-04', 'BOOK', 'English', 'ISBN-9781593279509', 'PDF', 'https://example.org/resources/eloquent-javascript', 'Open access', 'ACTIVE'),
(14, 'Pride and Prejudice', 'A novel of manners examining class, marriage, and morality.', 'T. Egerton', '1813-01-28', 'BOOK', 'English', 'ISBN-9780141439518', 'EPUB', 'https://example.org/resources/pride-and-prejudice', 'Public domain', 'ACTIVE'),
(15, 'The C++ Programming Language', 'Definitive reference and guide to C++ language features and design.', 'Addison-Wesley', '2013-05-19', 'BOOK', 'English', 'ISBN-9780321563842', 'PDF', 'https://example.org/resources/cpp-programming-language', 'Library subscription', 'ACTIVE'),
(16, 'Weapons of Math Destruction', 'An examination of how big data and algorithms can reinforce inequality.', 'Crown', '2016-09-06', 'BOOK', 'English', 'ISBN-9780553418811', 'PDF', 'https://example.org/resources/weapons-of-math-destruction', 'Campus license', 'ACTIVE'),
(17, 'Introduction to Linear Algebra', 'Foundational concepts and applications of linear algebra.', 'Wellesley-Cambridge Press', '2016-08-11', 'BOOK', 'English', 'ISBN-9780980232776', 'PDF', 'https://example.org/resources/intro-linear-algebra', 'Library subscription', 'ACTIVE'),
(18, 'Decolonising the Mind', 'Essays on language, culture, and politics in African literature.', 'James Currey', '1986-01-01', 'BOOK', 'English', 'ISBN-9780852555019', 'PDF', 'https://example.org/resources/decolonising-the-mind', 'Licensed for academic use', 'ACTIVE'),
(19, 'Designing Data-Intensive Applications', 'Principles and practices for reliable, scalable, and maintainable data systems.', 'O''Reilly Media', '2017-04-16', 'BOOK', 'English', 'ISBN-9781449373320', 'PDF', 'https://example.org/resources/ddia', 'Library subscription', 'ACTIVE'),
(20, 'The Left Hand of Darkness', 'Science fiction novel exploring gender, society, and diplomacy.', 'Ace Books', '1969-03-01', 'BOOK', 'English', 'ISBN-9780441478125', 'EPUB', 'https://example.org/resources/left-hand-of-darkness', 'Licensed for academic use', 'ACTIVE'),
(21, 'Open Access in Academic Libraries', 'Research paper on adoption and impact of open access policies in universities.', 'OpenShelf Academic Press', '2021-06-15', 'RESEARCH_PAPER', 'English', 'DOI-10.1000/oa.2021.001', 'PDF', 'https://example.org/resources/open-access-academic-libraries', 'CC BY 4.0', 'ACTIVE'),
(22, 'Metadata Quality for Digital Repositories', 'Journal article examining Dublin Core quality and interoperability.', 'Journal of Digital Curation', '2020-11-02', 'JOURNAL', 'English', 'DOI-10.1000/jdc.2020.114', 'PDF', 'https://example.org/resources/metadata-quality-digital-repositories', 'CC BY-NC 4.0', 'ACTIVE'),
(23, 'Search Relevance in Library Discovery Systems', 'Study of ranking, facets, and user behavior in catalogue search interfaces.', 'Library Technology Review', '2022-03-10', 'RESEARCH_PAPER', 'English', 'DOI-10.1000/ltr.2022.014', 'PDF', 'https://example.org/resources/search-relevance-discovery', 'CC BY 4.0', 'ACTIVE'),
(24, 'Digital Preservation Strategies for Universities', 'Guidance on preserving born-digital and digitized academic materials.', 'University Collections Quarterly', '2019-09-18', 'JOURNAL', 'English', 'DOI-10.1000/ucq.2019.209', 'PDF', 'https://example.org/resources/digital-preservation-strategies', 'Subscription access', 'ACTIVE'),
(25, 'Introductory Guide to Solr for Catalogues', 'Technical guide for implementing faceted search in library platforms.', 'OpenShelf Technical Reports', '2023-01-25', 'THESIS', 'English', 'TR-OS-2023-01', 'PDF', 'https://example.org/resources/solr-catalogue-guide', 'CC BY 4.0', 'ACTIVE'),
(26, 'Computing Curricula and Information Literacy', 'Conference paper linking computing education with library discovery skills.', 'SIGCSE Proceedings', '2024-02-14', 'RESEARCH_PAPER', 'English', 'DOI-10.1000/sigcse.2024.332', 'PDF', 'https://example.org/resources/computing-curricula-information-literacy', 'Publisher license', 'ACTIVE'),
(27, 'Community Archives and Multilingual Metadata', 'Research on metadata practices for multilingual and community collections.', 'International Journal of Archival Science', '2018-07-07', 'JOURNAL', 'English', 'DOI-10.1000/ijas.2018.077', 'PDF', 'https://example.org/resources/multilingual-metadata', 'CC BY-SA 4.0', 'ACTIVE'),
(28, 'Learning Analytics: Foundations and Ethics', 'Overview of learning analytics methods and responsible data use.', 'Education Data Review', '2022-10-05', 'JOURNAL', 'English', 'DOI-10.1000/edr.2022.510', 'PDF', 'https://example.org/resources/learning-analytics-ethics', 'Campus license', 'ACTIVE'),
(29, 'Katherine Johnson: Mathematics and Spaceflight', 'Biographical research article highlighting contributions to orbital mechanics.', 'STEM History Monographs', '2020-04-12', 'RESEARCH_PAPER', 'English', 'DOI-10.1000/shm.2020.044', 'PDF', 'https://example.org/resources/katherine-johnson-spaceflight', 'CC BY 4.0', 'ACTIVE'),
(30, 'Foundations of Web Architecture', 'Lecture notes and reference material on web standards and linked information.', 'Open Web Institute', '2024-08-30', 'BOOK', 'English', 'OWI-2024-WEB-FOUND', 'HTML', 'https://example.org/resources/foundations-web-architecture', 'CC BY 4.0', 'ACTIVE');

-- Resource-author links
INSERT INTO resource_authors (resource_id, author_id) VALUES
(1,1),(2,1),(3,2),(4,3),(5,4),(6,5),(7,6),(8,7),(9,8),(10,9),
(11,10),(12,11),(13,12),(14,13),(15,14),(16,15),(17,16),(18,17),(19,18),(20,19),
(21,5),(22,11),(23,18),(24,4),(25,9),(26,4),(27,17),(28,15),(29,20),(30,5),(30,18);

-- Resource-subject links
INSERT INTO resource_subjects (resource_id, subject_id) VALUES
(1,6),(2,6),(3,13),(4,1),(4,2),(5,13),(5,10),(6,4),(6,14),(7,7),
(8,2),(9,8),(9,9),(10,1),(10,3),(11,5),(12,2),(13,4),(13,2),(14,5),
(15,1),(15,2),(16,9),(16,10),(17,11),(17,10),(18,6),(18,9),(19,3),(19,2),
(20,7),(21,14),(21,15),(22,14),(22,4),(23,4),(23,2),(24,14),(24,15),(25,4),
(25,1),(26,13),(26,1),(27,14),(27,15),(28,13),(28,9),(29,10),(29,8),(30,4),(30,14);

-- Indexing status seed (initially pending; reconcile/reindex makes them searchable)
INSERT INTO indexing_status (resource_id, status, last_indexed_at, last_error)
SELECT id, 'PENDING', NULL, NULL FROM resources;

-- Align sequences for explicit IDs
SELECT setval('authors_id_seq', (SELECT MAX(id) FROM authors));
SELECT setval('subjects_id_seq', (SELECT MAX(id) FROM subjects));
SELECT setval('resources_id_seq', (SELECT MAX(id) FROM resources));
SELECT setval('import_jobs_id_seq', 1, false);
SELECT setval('import_errors_id_seq', 1, false);
SELECT setval('indexing_status_id_seq', (SELECT MAX(id) FROM indexing_status));
SELECT setval('users_id_seq', 1, false);
