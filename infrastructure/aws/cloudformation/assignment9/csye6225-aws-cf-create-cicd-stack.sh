#!/bin/bash

echo "Enter the name for CICD stack"
read cicd
aws cloudformation create-stack --stack-name $cicd --template-body file://csye6225-cf-cicd.json --parameters file://cicdStackParameter.json --capabilities CAPABILITY_NAMED_IAM

aws cloudformation wait stack-create-complete --stack-name $cicd

aws cloudformation describe-stacks --stack-name $cicd --query "Stacks[*].StackStatus" --output text

exit 0