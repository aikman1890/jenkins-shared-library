# jenkins-shared-library

A Jenkins Shared Library of battle-tested pipeline steps for containerized
apps: build/push, test, security scan, gated Kubernetes deploy, and Slack
notifications. Used with the declarative `examples/Jenkinsfile`.

## Using the library

Register the library in Jenkins: **Manage Jenkins → System → Global Pipeline
Libraries**, name it `sre-shared-lib`, point it at this repo, default branch
`main`.

Then reference it in your `Jenkinsfile`:

```groovy
@Library('sre-shared-lib') _
```

(See `examples/Jenkinsfile` for a full build → test → scan → deploy pipeline.)

## Steps

| Step              | Call signature                                                                 | What it does                                                        |
|-------------------|--------------------------------------------------------------------------------|---------------------------------------------------------------------|
| `buildApp`        | `buildApp(registry: "...", image: "...", tag: "...")`                          | `docker build` from the workspace and `docker push` to the registry |
| `runTests`        | `runTests(testCommand: "pytest", junitPattern: "**/junit.xml")`                | Runs the suite, publishes JUnit results, fails the stage on failure |
| `securityScan`    | `securityScan(image: "registry/app:tag")`                                      | Trivy image scan (fails on HIGH/CRITICAL) + SonarQube analysis      |
| `deployK8s`       | `deployK8s(env: "staging", release: "...", chart: "...", namespace: "...")`     | Helm upgrade; for `env: "prod"` requires manual approval first      |
| `notifySlack`     | `notifySlack("#deploys", "Build #${env.BUILD_NUMBER} ${currentBuild.result}")`  | Posts the build status to a Slack channel via the Slack plugin      |

## File layout

```
jenkins-shared-library/
├── vars/
│   ├── buildApp.groovy       # docker build + push step
│   ├── runTests.groovy       # run tests, publish JUnit results
│   ├── securityScan.groovy   # Trivy image scan + SonarQube analysis
│   ├── deployK8s.groovy      # helm/kubectl deploy, manual approval gate for prod
│   └── notifySlack.groovy    # build status notification to Slack
├── examples/
│   └── Jenkinsfile           # declarative pipeline: build -> test -> scan -> deploy
├── LICENSE
└── README.md
```

## Conventions

- Steps take a named-argument `Map` so call sites stay readable.
- Credentials are pulled from Jenkins credentials bindings (`dockerRegistryCreds`,
  `slackToken`, `kubeconfigFile`) — never hardcoded.
- `deployK8s` treats `env: "prod"` specially: it pauses for a manual
  `input` approval (12h timeout) before touching the cluster.
- SonarQube analysis assumes a server named `sonarqube` is configured in
  Jenkins (`withSonarQubeEnv('sonarqube')`).
