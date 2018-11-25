#!/bin/bash

echo "Enter the name for Serverless stack"
read server
aws cloudformation create-stack --stack-name $server --template-body file://serverless.json --parameters file://lambdaparameters.json --capabilities CAPABILITY_NAMED_IAM

aws cloudformation wait stack-create-complete --stack-name $server

aws cloudformation describe-stacks --stack-name $server --query "Stacks[*].StackStatus" --output text

exit 0
