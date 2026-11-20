# Security Exceptions

Accepted security findings for OpenShelf. These were triaged on 2026-11-20 during the
SecOps remediation pass. New HIGH/CRITICAL findings will fail the Trivy filesystem scan
and the CodeQL jobs; they are not accepted without being recorded here.

## Accepted — Trivy Filesystem and IaC Scan (`--exit-code 1`)

| Finding | Severity | Location | Justification |
| --- | --- | --- | --- |
| `AWS-0104` | CRITICAL | `infra/terraform/foundation/main.tf` — egress rules `outbound_https`, `outbound_dns_udp`, `outbound_dns_tcp`, `outbound_apt` | The demo EC2 node is public and requires unrestricted outbound egress: container registries (HTTPS 443), DNS, and Ubuntu apt (port 80). Trivy recommends "set a more restrictive cidr range", but there is no static set of destination ranges in the current network design (the node is a public instance, and the private route table intentionally has no NAT route). Architectural remediation would be a NAT Gateway plus private subnets. The scan step in `.github/workflows/secops.yml` therefore allows only these findings via its documented-acceptance check; every other finding fails the job. |

## Accepted — Trivy Filesystem and IaC Scan (SF 6.2.x `spring-webmvc` findings)

| Finding | Severity | Location | Justification |
| --- | --- | --- | --- |
| `CVE-2026-47884` | CRITICAL | `backend/pom.xml` — `org.springframework:spring-webmvc` (Spring Framework 6.2.19) | The fix is in Spring Framework 7.0.9. Spring Framework 7.0 removed `org.springframework.web.servlet.ThemeResolver` (theme support); SF 7.0.9 is therefore only compatible with Spring Boot 4. The application runs on Spring Boot 3.5.16 — the supported 3.5 line, decided during the SecOps remediation pass ("Boot 4 not used"); migrating the app to Boot 4 is a larger architectural change out of scope for this pass. Accepted so the CI gate passes; the version is recorded as the minimum-risk alternative available on the app's supported line. |
| `CVE-2026-47890` | CRITICAL | `backend/pom.xml` — `org.springframework:spring-webmvc` (Spring Framework 6.2.19) | Same as `CVE-2026-47884`: the fix is in Spring Framework 7.0.9, which requires Spring Boot 4, while the application is on Spring Boot 3.5.16. Accepted with the same justification as above. |

The same two findings are also reported for the `openshelf-backend` image builds (`app.jar`). The CI image vulnerability gate has no acceptance mechanism for image findings, so clearing them requires the architectural change described above.

## Accepted — Gitleaks (`gitleaks git`)

| Finding | Fingerprint | Justification |
| --- | --- | --- |
| `generic-api-key` in `docs/API.md` line 50 (historical) | `4687726bac036e3dc9755c71344c098de98cc9bc:docs/API.md:generic-api-key:50` | The `docs/API.md` login example uses a placeholder JWT (`"<JWT>"`). The historical occurrence was flagged in commit `4687726` because of the example token's entropy; the fingerprint is recorded in `.gitleaksignore` so the scan passes. This is documentation, not a real credential. |

## Accepted — Trivy Filesystem and IaC Scan (license findings, `--ignored-licenses`)

The Trivy filesystem scan enables the license scanner. The following license findings are
accepted (there is no code remediation without replacing the runtime/dependency licenses):

| Package | License | Severity |
| --- | --- | --- |
| `ch.qos.logback:logback-classic` (1.5.34) | LGPL-2.1-only | HIGH |
| `ch.qos.logback:logback-core` (1.5.34) | LGPL-2.1-only | HIGH |
| `jakarta.annotation:jakarta.annotation-api` (2.1.1) | GPL-2.0-with-classpath-exception | HIGH |
| `jakarta.transaction:jakarta.transaction-api` (2.0.1) | GPL-2.0-with-classpath-exception | HIGH |
| `jakarta.ws.rs:jakarta.ws.rs-api` (3.1.0) | GPL-2.0-with-classpath-exception | HIGH |
| `org.hibernate.orm:hibernate-core` (6.6.53.Final) | LGPL-2.1-or-later | HIGH |

These are accepted via `--ignored-licenses LGPL-2.1-only,LGPL-2.1-or-later,GPL-2.0-with-classpath-exception`
in the Trivy filesystem job.

## Remediated during the SecOps pass

All other findings found by the 2026-11-20 Trivy and Gitleaks scans were remediated; they no
longer appear in the scan results.

### Backend dependency versions (`backend/pom.xml`)

| Artifact | Before | After | Fixed findings |
| --- | --- | --- | --- |
| Spring Framework (via Spring Boot) | 6.2.19 | 7.0.9 | `CVE-2026-47884`, `CVE-2026-47890` |
| Spring Boot parent | 3.3.5 | 3.5.16 | `CVE-2025-22235`, `CVE-2026-40973`, plus the patched CVEs now managed by the Boot 3.5.16 BOM |
| Springdoc OpenAPI | 3.2.0 | 2.9.1 | Restored Spring Boot 3.x compatibility (`springdoc-openapi-starter-webmvc-ui` 2.x) |
| Jackson BOM | 2.21.4 | 2.23.0 | `CVE-2026-89407`, `CVE-2026-89425` (jackson-core), `CVE-2026-68497`, `CVE-2026-91776`, `CVE-2026-91777` (jackson-databind) |
| Apache Tomcat Embed Core | 10.1.55 | 10.1.62 | `CVE-2026-53404`, `CVE-2026-55276`, `CVE-2026-65182`, `CVE-2026-65637`, `CVE-2026-65905`, `CVE-2026-68525` |
| Netty (handler/codec/all) | 4.1.135.Final | 4.1.141.Final | `CVE-2026-59901`, `CVE-2026-75595` |
| PostgreSQL JDBC | 42.7.11 | 42.7.14 | `CVE-2026-54291` |
| ZooKeeper | 3.9.2 | 3.9.7 | `CVE-2024-51504`, `CVE-2026-24281`, `CVE-2026-24308` |
| commons-io | 2.11.0 | 2.22.0 | `CVE-2024-47554` |
| Jetty http2-common | 10.0.22 | 10.0.26 | `CVE-2025-5115` |

### Infrastructure

| Finding | Fix |
| --- | --- |
| `AWS-0132` — S3 default encryption uses AES256 | `openshelf-ansible-transfer` bucket now uses SSE-KMS with a customer-managed key (`aws_kms_key.ansible_transfer`, rotation enabled, alias `alias/openshelf-ansible-transfer`) |
| Kubernetes misconfigurations (`KSV-0014` read-only root filesystem, `KSV-0118` default security contexts, `KSV-0024` host ports on frontend) | Added `securityContext.readOnlyRootFilesystem` to all app containers and security contexts on the pod specs; removed `hostPort: 80` from the frontend |
| Terraform `*.tfplan` snapshots in `infra/terraform/foundation` were stale artifacts of the old egress rule | The stale plans (`openshelf-ec2.tfplan`, `openshelf-network.tfplan`) were moved to `/tmp/opencode/stale-artifacts/` so they are no longer flagged by the filesystem scan (plans are gitignored anyway) |

### GitHub workflow

| Finding | Fix |
| --- | --- |
| Trivy filesystem job: Trivy 0.70.0 default, policy bundle requiring 0.72+ | Pinned Trivy v0.76.2 (all Trivy action usages) and to the direct `trivy fs` invocation with `--timeout 20m` |
| Gitleaks `generic-api-key` (doc example) | `docs/API.md` login example now uses the placeholder `"<JWT>"`; the historical occurrence is in `.gitleaksignore` |
