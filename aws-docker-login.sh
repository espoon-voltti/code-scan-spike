#!/bin/bash
aws-vault exec voltti-sst -- aws ecr get-login-password --region eu-west-1 | docker login --username AWS --password-stdin 307238562370.dkr.ecr.eu-west-1.amazonaws.com
