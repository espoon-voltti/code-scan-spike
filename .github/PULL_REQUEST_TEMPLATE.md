#### Summary
<!-- Describe the change, including rationale and design decisions (not just what but also why) -->

#### Dependencies
<!-- Describe the dependencies the change has on other repositories, pull requests etc. -->

#### Testing instructions
<!-- Describe how the change can be tested, e.g., steps and tools to use -->

#### Checklist for pull request creator
<!-- Check that the necessary steps have been done before the PR is created -->

- [ ] The commits and commit messages adhere to [version control conventions](https://voltti.atlassian.net/l/c/6tEZ51P1)
- [ ] The API's adhere to the [REST API conventions](https://voltti.atlassian.net/wiki/spaces/VOLTTI/pages/46170144/REST-rajapintak+yt+nn+t)
- [ ] The code is consistent with the existing code base and follows good coding conventions
- [ ] Tests have been written for the change (JUnit, Camel)
- [ ] All tests pass
- [ ] There is no Maven warnings etc. during lifecycles
- [ ] There is no sensitive information like real names etc.
- [ ] The code is self-documenting or has been documented sufficiently, e.g., in the README
- [ ] The branch has been rebased against master before the PR was created

#### Checklist for pull request reviewer (copy to review text box)
<!-- Check that the necessary steps have been done in the review. Copy the template beneath for the review. -->

```
- [ ] The commits and commit messages adhere to [version control conventions](https://voltti.atlassian.net/l/c/6tEZ51P1)
- [ ] The API's adhere to the [REST API conventions](https://voltti.atlassian.net/wiki/spaces/VOLTTI/pages/46170144/REST-rajapintak+yt+nn+t)
- [ ] The code is consistent with the existing code base and follows good coding conventions
- [ ] All changes in all changed files have been reviewed
- [ ] Tests have been written for the change (JUnit, Camel)
- [ ] All tests pass
- [ ] There is no sensitive information like real names etc.
- [ ] There is no Maven warnings etc. during lifecycles
- [ ] The code is self-documenting or has been documented sufficiently, e.g., in the README
- [ ] The PR branch has been rebased against master and force pushed if necessary before merging
