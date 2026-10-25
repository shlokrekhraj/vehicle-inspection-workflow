# Step 14 - Automated Provisioning and Reliability Validation

Jenkins job **Vehicle-Inspection-Provisioning** (`Jenkinsfile.provision`) runs everything below and archives one log per stage under `ansible-logs/`.

| # | What | Command | Proves |
|---|------|---------|--------|
| 1 | Provision a **clean** node, configure it, deploy v1 | `site.yml -e fresh_node=true` | `vi-node1` recreated from `ubuntu:22.04`; Python, JDK, Nginx, user, folders, service, DB all set up by code |
| 2 | Run the same playbook again | `site.yml` | **Idempotency**: PLAY RECAP shows `changed=0` (the pipeline fails if not) |
| 3 | Health check | `health.yml` + `GET http://localhost:8095/actuator/health` | App and DB report `UP` through Nginx |
| 4 | Deploy v2, then roll back | `deploy.yml` (v2) then `rollback.yml` | **Manual recovery** to the previous stable release |
| 5 | Deploy a broken release | `deploy.yml -e simulate_bad_release=true` | Health check fails -> `rescue` block switches `current` back, restarts, re-verifies: **automatic rollback** |
| 6 | Final health check | `health.yml` | Service recovered |

## Release mechanism

```
/opt/vehicle-inspection/
  releases/1.3.7/app.jar
  releases/1.3.7-hotfix/app.jar
  current -> releases/1.3.7          (symlink switched atomically)
  previous_release                   (path of last good release)
  logs/app.log
```

Rolling back means pointing `current` at another release folder and restarting the service. No rebuild is needed.

## Running it by hand

```powershell
mvn -B package -DskipTests
$env:DB_PASSWORD = "<mysql root password>"
docker compose -f ansible/docker-compose.yml build
powershell -ExecutionPolicy Bypass -File ansible\run-ansible.ps1 -Playbook site.yml -AppVersion 1.3.0 -ExtraArgs "-e fresh_node=true"
powershell -ExecutionPolicy Bypass -File ansible\run-ansible.ps1 -Playbook site.yml -AppVersion 1.3.0   # idempotency
powershell -ExecutionPolicy Bypass -File ansible\run-ansible.ps1 -Playbook rollback.yml
```
