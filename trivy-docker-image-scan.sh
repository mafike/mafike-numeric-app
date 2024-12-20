#!/bin/bash

# Extract the Docker image name from the Dockerfile
dockerImageName=$(awk 'NR==1 {print $2}' Dockerfile)
echo "Docker Image Name: $dockerImageName"

# Define the Trivy cache directory
CACHE_DIR="$WORKSPACE/trivy"

# Ensure the Trivy cache directory exists and is writable
if [[ -d "$CACHE_DIR" ]]; then
    echo "Trivy cache directory already exists."
else
    echo "Creating Trivy cache directory."
    mkdir -p "$CACHE_DIR"
fi

# Ensure the cache directory and its contents are owned by Jenkins
sudo chown -R jenkins:jenkins "$CACHE_DIR"

# Run Trivy scans with the TRIVY_CACHE_DIR environment variable
echo "Running Trivy scan for HIGH severity vulnerabilities..."
docker run --rm --user $(id -u):$(id -g) -e TRIVY_CACHE_DIR=/root/.cache/ -v "$CACHE_DIR:/root/.cache/" aquasec/trivy:0.17.2 -q image --exit-code 0 --severity HIGH --light "$dockerImageName"

echo "Running Trivy scan for CRITICAL severity vulnerabilities..."
docker run --rm --user $(id -u):$(id -g) -e TRIVY_CACHE_DIR=/root/.cache/ -v "$CACHE_DIR:/root/.cache/" aquasec/trivy:0.17.2 -q image --exit-code 1 --severity CRITICAL --light "$dockerImageName"

# Trivy scan result processing
exit_code=$?
echo "Exit Code: $exit_code"

# Check scan results
if [[ "${exit_code}" == 1 ]]; then
    echo "Image scanning failed. Vulnerabilities found."
    # Fix permissions on the cache directory before exiting
    sudo chown -R jenkins:jenkins "$CACHE_DIR"
    exit 1
else
    echo "Image scanning passed. No CRITICAL vulnerabilities found."
fi

# Ensure all files and subdirectories inside the cache directory are owned by Jenkins after the scan
echo "Ensuring proper ownership of the cache directory and its contents..."
sudo chown -R jenkins:jenkins "$CACHE_DIR"

# Cleanup the cache directory
echo "Cleaning up Trivy cache directory..."
sudo rm -rf "$CACHE_DIR"
