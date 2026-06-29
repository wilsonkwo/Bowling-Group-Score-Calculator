# Project skills

Custom skills for this project go here, one subdirectory per skill, each containing a `SKILL.md`:

```
.claude/skills/
  my-skill-name/
    SKILL.md
```

`SKILL.md` needs YAML frontmatter with at least `name` and `description`, followed by the skill's instructions in markdown. Example:

```markdown
---
name: my-skill-name
description: One-line summary of what this skill does and when to use it.
---

Instructions for Claude to follow when this skill is invoked.
```

Skills placed here (as opposed to `~/.claude/skills/`) are scoped to this project and get committed with the repo, so anyone working on this codebase has them available.

Project-specific slash commands work the same way under `.claude/commands/` (a single `.md` file per command, e.g. `.claude/commands/my-command.md`).
