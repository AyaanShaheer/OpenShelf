#!/bin/bash

set -euxo pipefail

exec > >(tee -a /var/log/openshelf-bootstrap.log | logger -t openshelf-bootstrap -s 2>/dev/console) 2>&1

echo "Starting OpenShelf EC2 bootstrap"

# Ensure snapd is available
if ! command -v snap >/dev/null 2>&1; then
    apt-get update
    apt-get install -y snapd
fi

# Install SSM Agent if not already installed
if ! snap list amazon-ssm-agent >/dev/null 2>&1; then
    snap install amazon-ssm-agent --classic
fi

# Start and enable SSM Agent
systemctl enable snap.amazon-ssm-agent.amazon-ssm-agent.service

systemctl restart snap.amazon-ssm-agent.amazon-ssm-agent.service

echo "OpenShelf EC2 bootstrap completed"
