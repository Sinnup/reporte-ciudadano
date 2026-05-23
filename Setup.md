---
name: initial-ai-setup
description: These are set up instructions in order to start creating the KMP project.
---

## Setup Claude settings
- Create Claude settings for this project, allow editing and creating all files in the project, including .md, settings, .kt, .gradle, .properties, etc, any extension.
- Also don't ask permissions for running anything in bash, nor calls, nor curl, nor gradle.

## Tech stack

- Kotlin Multi platform for Android, iOS and Web.

## Architectural patterns

- MVI + Clean Architecture.

## Features

- There must be a features.md file, where each feature is a user story written by the Business
  Analyst and the UX/UI must analyze it and make a design proposal, before starting to develop
  something.

## Agents

- For the current KMP project create the following agents related to Kotlin Multi Platform (KMP)
- Versioning (with good practices for versioning of this project, indicating small, atomic or
  context commits, branching, etc.)
- KMP Architect (following MVI + Clean architecture pattern for KMP, scalable, modular, the main app
  should run the MainActivity, but the other features must be thought as Android Libraries, to be
  included in separated artifacts and useful for other apps that need to download any module)
- KMP Developer (following MVI + Clean architecture good practices for KMP, code must be designed to
  unit test)
- KMP QA (creator or Unit tests and in charge of executing them per new feature branch completed,
  this must update the unit tests whenever new code is created)
- Android expert developer (in charge of specific Android topics)
- iOS expert developer (in charge of specific iOS topics)
- Web expert developer (in charge of specific Web topics)
- UX/UI Designer, in charge of providing ideas for every new feature described in console or
  features.md.
- Business Analyst, inn charge of getting requirements from Claude code and write them down as user
  story. Later it must have a conversation with the UX/UI designer iterating over each proposal
  until ready and the shared to the developers.

## Changelog

There must be a changelog.md in order to keep track of changes, as well as saving tokens used with
AI. This changelog must be updated per feature completed, before asking merge to main branch and
push to remote.

## AI token saving driven

- Implement the best AI token saving practices or agents, in order to reduce token consumption per
  new change.

## Skills

- Android skills must be present in the global .claude folder in PC, as well as other skills related
  to architecture or good practices.
  If required follow the official documentation of KMP
  at https://kotlinlang.org/docs/multiplatform/get-started.html.

## Suggestions to the AI

- Please in case ouf doubts always ask me if required, is it's something you ca decide based on
  previous patterns, then do it.