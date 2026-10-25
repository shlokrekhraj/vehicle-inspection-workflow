# Ansible control node, run as a throw-away container so Windows needs no Ansible/WSL.
FROM python:3.12-slim

# "ansible" bundles ansible-core 2.17 plus community.docker and other collections
RUN pip install --no-cache-dir "ansible==10.7.0" "docker==7.1.0" "requests==2.32.3"

ENV ANSIBLE_CONFIG=/workspace/ansible/ansible.cfg \
    ANSIBLE_FORCE_COLOR=1 \
    PYTHONUNBUFFERED=1

WORKDIR /workspace/ansible
CMD ["ansible", "--version"]
