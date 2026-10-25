# Step 13 - Configuration Management (Ansible)

## Why Ansible in a container

Ansible does not run natively on Windows. The control node is a small image (`ansible/Dockerfile`: Python 3.12 + Ansible 10 / ansible-core 2.17 + Docker SDK). It mounts the repository and the Docker socket, so the same playbooks run from PowerShell or Jenkins with nothing else installed.

The target node is `vi-node1`, a container started from a **clean `ubuntu:22.04` image**. Ansible reaches it through the `community.docker.docker_api` connection, so there are no SSH keys to manage.

## Configuration specification (server prerequisites)

| Category | Item | Managed by |
|----------|------|-----------|
| Packages | `python3` (bootstrap), `openjdk-17-jre-headless`, `nginx`, `curl`, `ca-certificates` | `bootstrap`, `common` |
| Users / groups | system group `vehicleapp`; system user `vehicleapp` (shell `/usr/sbin/nologin`, home `/opt/vehicle-inspection`) | `common` |
| Folders | `/opt/vehicle-inspection` (root, 0755), `releases/` (root, 0755), `logs/` (vehicleapp, 0750), `/etc/vehicle-inspection` (root:vehicleapp, 0750) | `common` |
| Files | `/etc/vehicle-inspection/app.env` (0640, DB URL/credentials, port, JVM options); `/etc/init.d/vehicle-inspection` (service script); `/etc/nginx/sites-available/vehicle-inspection.conf` (+ enabled symlink); default site removed | `common`, `nginx` |
| Ports | 80/tcp Nginx (published on the PC as **8095**); 8080/tcp app (internal only); 3306/tcp MySQL on `vi-node-db` (internal network only) | `nginx`, `provision` |
| Services | `nginx` started + enabled; `vehicle-inspection` service enabled (started when a release is deployed) | `nginx`, `common` |
| Infrastructure | Docker network `vi-ansible-net`; MySQL 8.4 container `vi-node-db` with volume `vi-node-db-data` | `provision` |

## Layout

```
ansible/
  Dockerfile, docker-compose.yml, ansible.cfg, run-ansible.ps1
  inventory/hosts.yml          control (localhost) + app_nodes (vi-node1)
  group_vars/all.yml           all variables in one place
  playbooks/provision.yml      network, database, clean node
  playbooks/configure.yml      bootstrap + common + nginx roles
  roles/{provision,bootstrap,common,nginx}
```

## First execution

```powershell
mvn -B package -DskipTests
$env:DB_PASSWORD = "<mysql root password>"
docker compose -f ansible/docker-compose.yml build
powershell -ExecutionPolicy Bypass -File ansible\run-ansible.ps1 -Playbook provision.yml -LogFile docs\evidence\ansible-step13-provision.log
powershell -ExecutionPolicy Bypass -File ansible\run-ansible.ps1 -Playbook configure.yml -LogFile docs\evidence\ansible-step13-configure.log
```

Jenkins runs the same commands in the `Vehicle-Inspection-Provisioning` job (`Jenkinsfile.provision`).
