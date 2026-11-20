#!/usr/bin/env python3
"""Check the Trivy filesystem SARIF report against the accepted findings list.

Accepted findings are recorded in SECURITY-EXCEPTIONS.md. Any finding not in
this list fails the check (the Trivy job uses ``--exit-code 1``).

Usage:
    python3 scripts/trivy-check-accepted.py [sarif_file]

Exits 0 when every finding is accepted, 1 when there are unexpected ones.
"""

import json
import sys

# (rule ID, artifact URI) pairs accepted for the filesystem/IaC scan.
# Mirrors the "Accepted — Trivy Filesystem and IaC Scan" table in
# SECURITY-EXCEPTIONS.md.
ACCEPTED = {
    ("AWS-0104", "infra/terraform/foundation/main.tf"),
    # Fixed in Spring Framework 7.0.9, which removed theme APIs (ThemeResolver) and is
    # only compatible with Spring Boot 4; the application runs on Spring Boot 3.5.16
    # (the supported 3.5 line — see SECURITY-EXCEPTIONS.md).
    ("CVE-2026-47884", "backend/pom.xml"),
    ("CVE-2026-47890", "backend/pom.xml"),
}


def main() -> int:
    sarif_file = sys.argv[1] if len(sys.argv) > 1 else "trivy-filesystem.sarif"
    data = json.load(open(sarif_file))
    unexpected = []
    for run in data.get("runs", []):
        for result in run.get("results", []) or []:
            location = result["locations"][0]["physicalLocation"]
            finding = (result["ruleId"], location["artifactLocation"]["uri"])
            if finding not in ACCEPTED:
                unexpected.append(finding)

    for rule_id, uri in sorted(unexpected):
        print(f"UNEXPECTED FINDING: {rule_id} in {uri}")
    if unexpected:
        return 1
    print("Trivy filesystem scan: all findings are explicitly accepted (see SECURITY-EXCEPTIONS.md).")
    return 0


if __name__ == "__main__":
    sys.exit(main())
