# SkillMatch (Java backend + website)

## Run
Requires JDK 17+. In VS Code: open this folder (the one with pom.xml), open src/main/java/skillmatch/Main.java, press F5.
Or with Maven:  mvn package  then  java -jar target/skillmatch-1.0.jar
Open http://localhost:8080 (use PORT=9090 or a "9090" program argument if 8080 is busy).

Optional: set ANTHROPIC_API_KEY to enable Claude (CLAUDE_MODEL optional). Without it the keyword/template fallback is used.

## Resume fallback
Not one column (2-column, sidebar, text boxes, layout tables), scanned, password-protected, corrupt, .doc, over 5 MB,
or no standard sections -> clear message + download of /templates/skillmatch-resume-template.docx (which passes the same checks).

## API
GET /api/locations | POST /api/analyze (multipart: location, resume) -> results JSON or {error:{code,message,template?}}

## Layout
Java classes: src/main/java/skillmatch. Pages and images: src/main/resources/web (Images/ stays next to the pages).
Sample jobs: src/main/resources/opportunities.json (fictional companies, Batangas cities + Remote).
Checks (no JUnit): compile, then  java -cp target/classes:target/test-classes skillmatch.Tests  from the project root.
