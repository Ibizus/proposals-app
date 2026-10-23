# ADR 0001: PDF generation in the JVM

## Status
Accepted

## Context
JSReport was an external service rendering proposal PDFs. We replace it with in-JVM generation.

## Decision
Use OpenHTMLtoPDF with Thymeleaf templates, PDF/A-2u with embedded open-license fonts. Compute salary breakdown in backend. Generate in memory, no disk.

## Consequences
Removes external dependency; PDFs available in four languages; testable rendering.
