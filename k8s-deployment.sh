#!/bin/bash

# Deployment Script: k8s-deployment.sh

set -e  # Exit immediately if a command exits with a non-zero status

# Ensure required variables are set
if [[ -z "${SimageName}" || -z "${deploymentName}" ]]; then
  echo "Error: Required environment variables (SimageName, deploymentName) are not set."
  exit 1
fi

# Replace the placeholder in the deployment YAML file with the actual image name
sed -i "s#replace#${SimageName}#g" k8s_deployment_service.yaml

# Check if the deployment exists
if ! kubectl -n "${namespace}" get deployment "${deploymentName}" > /dev/null 2>&1; then
  echo "Deployment ${deploymentName} does not exist. Creating a new deployment..."
  kubectl -n "${namespace}" apply -f mysql-manifest.yaml
  kubectl -n "${namespace}" apply -f k8s_deployment_service.yaml
else
  echo "Deployment ${deploymentName} exists. Updating the container image..."
  kubectl -n "${namespace}" set image deployment/${deploymentName} ${containerName}=${SimageName} --record=true
fi

echo "Deployment command executed successfully."



kubectl -n default apply -f k8s_deployment_service.yaml
######################### update existing k8s-deployment.sh ######################### 