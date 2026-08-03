# Fetching PR review comments

Command recipes for the triage-pr-reviews skill. Resolve `owner` / `repo` first:

```bash
gh repo view --json owner,name -q '.owner.login + "/" + .name'
```

## REST — the three comment sources

Comments live in three separate REST endpoints — fetch all of them (`--paginate` handles paging):

| Source | Command | Content |
|--------|---------|---------|
| **Inline review comments** | `gh api repos/{owner}/{repo}/pulls/{number}/comments --paginate` | Comments anchored to a diff line (`path` / `line` / `diff_hunk` available) |
| **Review summary** | `gh api repos/{owner}/{repo}/pulls/{number}/reviews --paginate` | Top-level review `body` (Approve / RequestChanges / Comment) |
| **Issue comments** | `gh api repos/{owner}/{repo}/issues/{number}/comments --paginate` | General PR conversation comments |

## GraphQL — thread resolution state

When thread resolution state (`isResolved`) is needed:

```bash
gh api graphql -F owner=<owner> -F repo=<repo> -F num=<number> -f query='
  query($owner:String!,$repo:String!,$num:Int!){
    repository(owner:$owner,name:$repo){
      pullRequest(number:$num){
        reviewThreads(first:100){
          nodes{
            isResolved
            comments(first:50){
              nodes{ id author{login} body path line }
            }
          }
        }
      }
    }
  }'
```

`reviewThreads(first:100)` is enough in practice; if it isn't, page manually with
`pageInfo { hasNextPage endCursor }`.
