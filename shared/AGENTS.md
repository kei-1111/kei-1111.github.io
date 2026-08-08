# AGENTS.md — shared/

Rules for `shared/model`; its `@Serializable` types form the client/server JSON contract. The root
`AGENTS.md` still applies.

- Wire compatibility and its required validation are canonical in
  `.claude/rules/shared-model.md`; read it before changing an `@Serializable` model or its
  serializer.
