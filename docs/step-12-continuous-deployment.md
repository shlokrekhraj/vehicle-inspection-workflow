# Step 12 - Jenkins-Docker Continuous Deployment

## Commit-to-container flow

```
git push -> Jenkins (pollSCM) -> mvn test -> mvn package -> docker build vehicle-inspection-workflow:1.2.<build>
        -> start test container vi-app-test -> Selenium suite -> docker push localhost:5000/...:1.2.<build> + :latest
        -> deploy vi-app-<env> (auto rollback if unhealthy) -> Nginx vi-nginx-<env> -> smoke test
```

| Item | Value |
|------|-------|
| Image version | `1.2.<BUILD_NUMBER>`, plus `latest`; label `org.opencontainers.image.revision` = git commit |
| Registry | Local Docker registry `vi-registry` on `localhost:5000` (`REGISTRY` parameter can point elsewhere) |
| Registry evidence | `registry-tags.json` build artifact (`/v2/vehicle-inspection-workflow/tags/list`) |
| Test container | `vi-app-test` on port 8091, database `vehicle_inspection_ci` |
| Deployed container | `vi-app-<env>` on `APP_PORT` (default 8085), database `vehicle_inspection_<env>` |
| Public URL | `http://localhost:<NGINX_PORT>/` (default 8088) via `vi-nginx-<env>` |
| Database | `vi-mysql` (MySQL 8.4) on Docker network `vi-net`, volume `vi-mysql-data` |
| Rollback | `deploy-container.ps1` keeps `<env>-current-image.txt`; if the new container is not healthy it redeploys the previous image |

A failed unit or Selenium test stops the pipeline before **Publish Image**, so untested images never reach the registry.
